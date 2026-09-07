package toydb;

import java.util.ArrayList;
import java.util.List;

public class SimpleBPlusTree {
    private static final int M = 4;
    private Node root;

    public SimpleBPlusTree() {
        this.root = new Node(true);
    }

    public static class Node {
        boolean isLeaf;
        List<Integer> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();
        Node next;
        Node prev;
        List<Node> children = new ArrayList<>();

        public Node(boolean isLeaf) {
            this.isLeaf = isLeaf;
        }
    }

    public String search(int key) {
        Node leaf = findLeaf(key);
        for (int i = 0; i < leaf.keys.size(); i++) {
            if (leaf.keys.get(i) == key) {
                return leaf.values.get(i);
            }
        }
        return null;
    }

    public List<String> rangeSearch(int minKey, int maxKey) {
        List<String> results = new ArrayList<>();
        Node curr = findLeaf(minKey);

        while (curr != null) {
            for (int i = 0; i < curr.keys.size(); i++) {
                int k = curr.keys.get(i);
                if (k >= minKey && k <= maxKey) {
                    results.add(curr.values.get(i));
                }
                if (k > maxKey) {
                    return results;
                }
            }
            curr = curr.next;
        }
        return results;
    }

    public void insert(int key, String value) {
        List<Node> path = new ArrayList<>();
        Node curr = root;

        while (!curr.isLeaf) {
            path.add(curr);
            int i = 0;
            while (i < curr.keys.size() && key >= curr.keys.get(i)) {
                i++;
            }
            curr = curr.children.get(i);
        }

        Node leaf = curr;
        int insertIdx = 0;
        while (insertIdx < leaf.keys.size() && leaf.keys.get(insertIdx) < key) {
            insertIdx++;
        }
        leaf.keys.add(insertIdx, key);
        leaf.values.add(insertIdx, value);

        if (leaf.keys.size() < M) {
            return;
        }

        Node newLeafSibling = splitLeaf(leaf);
        int keyToPromote = newLeafSibling.keys.get(0);
        Node childToAttach = newLeafSibling;

        for (int i = path.size() - 1; i >= 0; i--) {
            Node parent = path.get(i);
            int pIdx = 0;
            while (pIdx < parent.keys.size() && parent.keys.get(pIdx) < keyToPromote) {
                pIdx++;
            }
            parent.keys.add(pIdx, keyToPromote);
            parent.children.add(pIdx + 1, childToAttach);

            if (parent.keys.size() < M) {
                return;
            }

            SplitResult splitResult = splitInternal(parent);
            keyToPromote = splitResult.promotedKey;
            childToAttach = splitResult.newSibling;
        }

        Node newRoot = new Node(false);
        newRoot.keys.add(keyToPromote);
        newRoot.children.add(root);
        newRoot.children.add(childToAttach);
        root = newRoot;
    }

    private Node findLeaf(int key) {
        Node curr = root;
        while (!curr.isLeaf) {
            int i = 0;
            while (i < curr.keys.size() && key >= curr.keys.get(i)) {
                i++;
            }
            curr = curr.children.get(i);
        }
        return curr;
    }

    private Node splitLeaf(Node leaf) {
        Node sibling = new Node(true);
        int mid = leaf.keys.size() / 2;

        sibling.keys.addAll(leaf.keys.subList(mid, leaf.keys.size()));
        sibling.values.addAll(leaf.values.subList(mid, leaf.values.size()));

        leaf.keys.subList(mid, leaf.keys.size()).clear();
        leaf.values.subList(mid, leaf.values.size()).clear();

        sibling.next = leaf.next;
        sibling.prev = leaf;
        if (leaf.next != null) {
            leaf.next.prev = sibling;
        }
        leaf.next = sibling;

        return sibling;
    }

    private SplitResult splitInternal(Node internal) {
        Node sibling = new Node(false);
        int mid = internal.keys.size() / 2;
        int promotedKey = internal.keys.get(mid);

        sibling.keys.addAll(internal.keys.subList(mid + 1, internal.keys.size()));
        sibling.children.addAll(internal.children.subList(mid + 1, internal.children.size()));

        internal.keys.subList(mid, internal.keys.size()).clear();
        internal.children.subList(mid + 1, internal.children.size()).clear();

        return new SplitResult(promotedKey, sibling);
    }

    private static class SplitResult {
        final int promotedKey;
        final Node newSibling;

        SplitResult(int promotedKey, Node newSibling) {
            this.promotedKey = promotedKey;
            this.newSibling = newSibling;
        }
    }
}
