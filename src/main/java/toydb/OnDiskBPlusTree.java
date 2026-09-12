package toydb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OnDiskBPlusTree {
    private static final int M = 4;
    private static final int MAX_KEYS = M - 1;

    private final DiskStorageManager storage;
    private final LRUCache<Long, byte[]> cache;

    public OnDiskBPlusTree(DiskStorageManager storage, int cacheCapacity) throws IOException {
        this.storage = storage;
        this.cache = new LRUCache<>(cacheCapacity);

        if (storage.getHeader().getTotalNodes() == 0) {
            BinaryNode.LeafNodeData rootLeaf = new BinaryNode.LeafNodeData(
                    true,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    null,
                    null
            );
            byte[] serialized = rootLeaf.serialize();
            BinaryNode.Pointer rootPtr = storage.writeNewNode(serialized);
            cache.putCached(rootPtr.position, serialized);
            storage.updateRootPointer(rootPtr);
        }
    }

    public BinaryNode.Pointer search(int key) throws IOException {
        BinaryNode.Pointer currPtr = storage.getHeader().getRootPointer();

        while (true) {
            byte[] nodeBytes = loadNodeBytes(currPtr);
            byte flag = nodeBytes[0];

            if (BinaryNode.NodeFlags.isLeaf(flag)) {
                BinaryNode.LeafNodeData leaf = BinaryNode.LeafNodeData.deserialize(nodeBytes);
                for (int i = 0; i < leaf.keys.size(); i++) {
                    if (leaf.keys.get(i) == key) {
                        return leaf.dataPointers.get(i);
                    }
                }
                return null;
            } else {
                BinaryNode.InternalNodeData internal = BinaryNode.InternalNodeData.deserialize(nodeBytes);
                int idx = 0;
                while (idx < internal.keys.size() && key >= internal.keys.get(idx)) {
                    idx++;
                }
                currPtr = internal.childPointers.get(idx);
            }
        }
    }

    public List<BinaryNode.Pointer> rangeScan(int minKey, int maxKey) throws IOException {
        List<BinaryNode.Pointer> results = new ArrayList<>();
        BinaryNode.Pointer leafPtr = findLeafPointer(minKey);

        while (leafPtr != null) {
            byte[] nodeBytes = loadNodeBytes(leafPtr);
            BinaryNode.LeafNodeData leaf = BinaryNode.LeafNodeData.deserialize(nodeBytes);

            for (int i = 0; i < leaf.keys.size(); i++) {
                int k = leaf.keys.get(i);
                if (k >= minKey && k <= maxKey) {
                    results.add(leaf.dataPointers.get(i));
                }
                if (k > maxKey) {
                    return results;
                }
            }

            leafPtr = leaf.nextSibling;
        }

        return results;
    }

    public synchronized void insert(int key, BinaryNode.Pointer dataPointer) throws IOException {
        List<BinaryNode.Pointer> path = new ArrayList<>();
        BinaryNode.Pointer currPtr = storage.getHeader().getRootPointer();

        while (true) {
            byte[] nodeBytes = loadNodeBytes(currPtr);
            byte flag = nodeBytes[0];

            if (BinaryNode.NodeFlags.isLeaf(flag)) {
                break;
            }

            path.add(currPtr);
            BinaryNode.InternalNodeData internal = BinaryNode.InternalNodeData.deserialize(nodeBytes);
            int idx = 0;
            while (idx < internal.keys.size() && key >= internal.keys.get(idx)) {
                idx++;
            }
            currPtr = internal.childPointers.get(idx);
        }

        BinaryNode.Pointer leafPtr = currPtr;
        byte[] leafBytes = loadNodeBytes(leafPtr);
        BinaryNode.LeafNodeData leaf = BinaryNode.LeafNodeData.deserialize(leafBytes);

        List<Integer> keys = new ArrayList<>(leaf.keys);
        List<BinaryNode.Pointer> dataPtrs = new ArrayList<>(leaf.dataPointers);

        int insertIdx = 0;
        while (insertIdx < keys.size() && keys.get(insertIdx) < key) {
            insertIdx++;
        }
        keys.add(insertIdx, key);
        dataPtrs.add(insertIdx, dataPointer);

        if (keys.size() <= MAX_KEYS) {
            BinaryNode.LeafNodeData updatedLeaf = new BinaryNode.LeafNodeData(
                    leaf.isRoot, keys, dataPtrs, leaf.prevSibling, leaf.nextSibling
            );
            persistNode(leafPtr, updatedLeaf.serialize());
            return;
        }

        int mid = keys.size() / 2;
        List<Integer> leftKeys = new ArrayList<>(keys.subList(0, mid));
        List<BinaryNode.Pointer> leftDataPtrs = new ArrayList<>(dataPtrs.subList(0, mid));

        List<Integer> rightKeys = new ArrayList<>(keys.subList(mid, keys.size()));
        List<BinaryNode.Pointer> rightDataPtrs = new ArrayList<>(dataPtrs.subList(mid, dataPtrs.size()));

        BinaryNode.LeafNodeData rightLeaf = new BinaryNode.LeafNodeData(
                false, rightKeys, rightDataPtrs, leafPtr, leaf.nextSibling
        );
        byte[] rightBytes = rightLeaf.serialize();
        BinaryNode.Pointer rightPtr = storage.writeNewNode(rightBytes);
        cache.putCached(rightPtr.position, rightBytes);

        if (leaf.nextSibling != null) {
            byte[] oldNextBytes = loadNodeBytes(leaf.nextSibling);
            BinaryNode.LeafNodeData oldNext = BinaryNode.LeafNodeData.deserialize(oldNextBytes);
            BinaryNode.LeafNodeData updatedOldNext = new BinaryNode.LeafNodeData(
                    oldNext.isRoot, oldNext.keys, oldNext.dataPointers, rightPtr, oldNext.nextSibling
            );
            persistNode(leaf.nextSibling, updatedOldNext.serialize());
        }

        boolean leafWasRoot = leaf.isRoot;
        BinaryNode.LeafNodeData updatedLeftLeaf = new BinaryNode.LeafNodeData(
                false, leftKeys, leftDataPtrs, leaf.prevSibling, rightPtr
        );
        persistNode(leafPtr, updatedLeftLeaf.serialize());

        int keyToPromote = rightKeys.get(0);
        BinaryNode.Pointer childToAttach = rightPtr;

        if (leafWasRoot) {
            List<Integer> rootKeys = new ArrayList<>();
            rootKeys.add(keyToPromote);

            List<BinaryNode.Pointer> rootChildren = new ArrayList<>();
            rootChildren.add(leafPtr);
            rootChildren.add(childToAttach);

            BinaryNode.InternalNodeData newRoot = new BinaryNode.InternalNodeData(true, rootKeys, rootChildren);
            byte[] newRootBytes = newRoot.serialize();
            BinaryNode.Pointer newRootPtr = storage.writeNewNode(newRootBytes);
            cache.putCached(newRootPtr.position, newRootBytes);

            storage.updateRootPointer(newRootPtr);
            return;
        }

        for (int i = path.size() - 1; i >= 0; i--) {
            BinaryNode.Pointer parentPtr = path.get(i);
            byte[] parentBytes = loadNodeBytes(parentPtr);
            BinaryNode.InternalNodeData parent = BinaryNode.InternalNodeData.deserialize(parentBytes);

            List<Integer> pKeys = new ArrayList<>(parent.keys);
            List<BinaryNode.Pointer> pChildren = new ArrayList<>(parent.childPointers);

            int pIdx = 0;
            while (pIdx < pKeys.size() && pKeys.get(pIdx) < keyToPromote) {
                pIdx++;
            }
            pKeys.add(pIdx, keyToPromote);
            pChildren.add(pIdx + 1, childToAttach);

            if (pKeys.size() <= MAX_KEYS) {
                BinaryNode.InternalNodeData updatedParent = new BinaryNode.InternalNodeData(parent.isRoot, pKeys, pChildren);
                persistNode(parentPtr, updatedParent.serialize());
                return;
            }

            int pMid = pKeys.size() / 2;
            int promotedKey = pKeys.get(pMid);

            List<Integer> leftPKeys = new ArrayList<>(pKeys.subList(0, pMid));
            List<BinaryNode.Pointer> leftPChildren = new ArrayList<>(pChildren.subList(0, pMid + 1));

            List<Integer> rightPKeys = new ArrayList<>(pKeys.subList(pMid + 1, pKeys.size()));
            List<BinaryNode.Pointer> rightPChildren = new ArrayList<>(pChildren.subList(pMid + 1, pChildren.size()));

            BinaryNode.InternalNodeData rightInternal = new BinaryNode.InternalNodeData(false, rightPKeys, rightPChildren);
            byte[] rightInternalBytes = rightInternal.serialize();
            BinaryNode.Pointer rightInternalPtr = storage.writeNewNode(rightInternalBytes);
            cache.putCached(rightInternalPtr.position, rightInternalBytes);

            boolean parentWasRoot = parent.isRoot;
            BinaryNode.InternalNodeData updatedLeftParent = new BinaryNode.InternalNodeData(false, leftPKeys, leftPChildren);
            persistNode(parentPtr, updatedLeftParent.serialize());

            keyToPromote = promotedKey;
            childToAttach = rightInternalPtr;

            if (parentWasRoot) {
                List<Integer> newRootKeys = new ArrayList<>();
                newRootKeys.add(keyToPromote);

                List<BinaryNode.Pointer> newRootChildren = new ArrayList<>();
                newRootChildren.add(parentPtr);
                newRootChildren.add(childToAttach);

                BinaryNode.InternalNodeData newRoot = new BinaryNode.InternalNodeData(true, newRootKeys, newRootChildren);
                byte[] newRootBytes = newRoot.serialize();
                BinaryNode.Pointer newRootPtr = storage.writeNewNode(newRootBytes);
                cache.putCached(newRootPtr.position, newRootBytes);

                storage.updateRootPointer(newRootPtr);
                return;
            }
        }
    }

    private BinaryNode.Pointer findLeafPointer(int key) throws IOException {
        BinaryNode.Pointer currPtr = storage.getHeader().getRootPointer();
        while (true) {
            byte[] nodeBytes = loadNodeBytes(currPtr);
            byte flag = nodeBytes[0];
            if (BinaryNode.NodeFlags.isLeaf(flag)) {
                return currPtr;
            }
            BinaryNode.InternalNodeData internal = BinaryNode.InternalNodeData.deserialize(nodeBytes);
            int idx = 0;
            while (idx < internal.keys.size() && key >= internal.keys.get(idx)) {
                idx++;
            }
            currPtr = internal.childPointers.get(idx);
        }
    }

    private byte[] loadNodeBytes(BinaryNode.Pointer ptr) throws IOException {
        byte[] cached = cache.getCached(ptr.position);
        if (cached != null) {
            return cached;
        }
        byte[] diskBytes = storage.readNode(ptr);
        cache.putCached(ptr.position, diskBytes);
        return diskBytes;
    }

    private void persistNode(BinaryNode.Pointer ptr, byte[] data) throws IOException {
        storage.updateNode(ptr, data);
        cache.putCached(ptr.position, data);
    }

    public LRUCache<Long, byte[]> getCache() {
        return cache;
    }

    public DiskStorageManager getStorage() {
        return storage;
    }
}
