package toydb;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BinaryNode {
    public static final int NODE_SIZE = 80;

    public static class Pointer {
        public static final byte TYPE_DATA = 0x01;
        public static final byte TYPE_NODE = 0x02;
        public static final int SIZE = 1 + 8 + 4;

        public final byte type;
        public final long position;
        public final int chunk;

        public Pointer(byte type, long position, int chunk) {
            this.type = type;
            this.position = position;
            this.chunk = chunk;
        }

        public void writeToBuffer(ByteBuffer buffer) {
            buffer.put(type);
            buffer.putLong(position);
            buffer.putInt(chunk);
        }

        public static Pointer readFromBuffer(ByteBuffer buffer) {
            byte type = buffer.get();
            long position = buffer.getLong();
            int chunk = buffer.getInt();
            return new Pointer(type, position, chunk);
        }
    }

    public static class NodeFlags {
        public static final byte INTERNAL_BIT = 0x01;
        public static final byte LEAF_BIT     = 0x02;
        public static final byte ROOT_BIT     = 0x04;

        public static boolean isLeaf(byte flag) {
            return (flag & LEAF_BIT) != 0;
        }

        public static boolean isInternal(byte flag) {
            return (flag & INTERNAL_BIT) != 0;
        }

        public static boolean isRoot(byte flag) {
            return (flag & ROOT_BIT) != 0;
        }

        public static byte createFlag(boolean isLeaf, boolean isRoot) {
            byte flag = 0;
            if (isLeaf) {
                flag |= LEAF_BIT;
            } else {
                flag |= INTERNAL_BIT;
            }
            if (isRoot) {
                flag |= ROOT_BIT;
            }
            return flag;
        }
    }

    public static class InternalNodeData {
        public static final int DEGREE = 4;
        public static final int MAX_KEYS = DEGREE - 1;
        public static final int PAYLOAD_SIZE = 1 + Pointer.SIZE + (MAX_KEYS * (4 + Pointer.SIZE));
        public static final int PADDING = NODE_SIZE - PAYLOAD_SIZE;

        public final boolean isRoot;
        public final List<Integer> keys;
        public final List<Pointer> childPointers;

        public InternalNodeData(boolean isRoot, List<Integer> keys, List<Pointer> childPointers) {
            this.isRoot = isRoot;
            this.keys = keys;
            this.childPointers = childPointers;
        }

        public byte[] serialize() {
            ByteBuffer buffer = ByteBuffer.allocate(NODE_SIZE);
            buffer.put(NodeFlags.createFlag(false, isRoot));

            if (!childPointers.isEmpty()) {
                childPointers.get(0).writeToBuffer(buffer);
            } else {
                new Pointer((byte) 0, 0, 0).writeToBuffer(buffer);
            }

            for (int i = 0; i < MAX_KEYS; i++) {
                if (i < keys.size()) {
                    buffer.putInt(keys.get(i));
                    childPointers.get(i + 1).writeToBuffer(buffer);
                } else {
                    buffer.putInt(0);
                    new Pointer((byte) 0, 0, 0).writeToBuffer(buffer);
                }
            }

            buffer.put(new byte[PADDING]);
            return buffer.array();
        }

        public static InternalNodeData deserialize(byte[] data) {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            byte flag = buffer.get();
            boolean isRoot = NodeFlags.isRoot(flag);

            List<Pointer> childPointers = new ArrayList<>();
            Pointer leftChild = Pointer.readFromBuffer(buffer);
            childPointers.add(leftChild);

            List<Integer> keys = new ArrayList<>();
            for (int i = 0; i < MAX_KEYS; i++) {
                int key = buffer.getInt();
                Pointer child = Pointer.readFromBuffer(buffer);
                if (child.type != 0) {
                    keys.add(key);
                    childPointers.add(child);
                }
            }

            return new InternalNodeData(isRoot, keys, childPointers);
        }
    }

    public static class LeafNodeData {
        public static final int DEGREE = 4;
        public static final int MAX_KEYS = DEGREE - 1;
        public static final int PAYLOAD_SIZE = 1 + (MAX_KEYS * (4 + Pointer.SIZE)) + (2 * Pointer.SIZE);
        public static final int PADDING = NODE_SIZE - PAYLOAD_SIZE;

        public final boolean isRoot;
        public final List<Integer> keys;
        public final List<Pointer> dataPointers;
        public final Pointer prevSibling;
        public final Pointer nextSibling;

        public LeafNodeData(boolean isRoot, List<Integer> keys, List<Pointer> dataPointers, Pointer prevSibling, Pointer nextSibling) {
            this.isRoot = isRoot;
            this.keys = keys;
            this.dataPointers = dataPointers;
            this.prevSibling = prevSibling;
            this.nextSibling = nextSibling;
        }

        public byte[] serialize() {
            ByteBuffer buffer = ByteBuffer.allocate(NODE_SIZE);
            buffer.put(NodeFlags.createFlag(true, isRoot));

            for (int i = 0; i < MAX_KEYS; i++) {
                if (i < keys.size()) {
                    buffer.putInt(keys.get(i));
                    dataPointers.get(i).writeToBuffer(buffer);
                } else {
                    buffer.putInt(0);
                    new Pointer((byte) 0, 0, 0).writeToBuffer(buffer);
                }
            }

            (prevSibling != null ? prevSibling : new Pointer((byte) 0, 0, 0)).writeToBuffer(buffer);
            (nextSibling != null ? nextSibling : new Pointer((byte) 0, 0, 0)).writeToBuffer(buffer);

            buffer.put(new byte[PADDING]);
            return buffer.array();
        }

        public static LeafNodeData deserialize(byte[] data) {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            byte flag = buffer.get();
            boolean isRoot = NodeFlags.isRoot(flag);

            List<Integer> keys = new ArrayList<>();
            List<Pointer> dataPointers = new ArrayList<>();

            for (int i = 0; i < MAX_KEYS; i++) {
                int key = buffer.getInt();
                Pointer dataPtr = Pointer.readFromBuffer(buffer);
                if (dataPtr.type != 0) {
                    keys.add(key);
                    dataPointers.add(dataPtr);
                }
            }

            Pointer prev = Pointer.readFromBuffer(buffer);
            Pointer next = Pointer.readFromBuffer(buffer);

            return new LeafNodeData(
                    isRoot,
                    keys,
                    dataPointers,
                    prev.type != 0 ? prev : null,
                    next.type != 0 ? next : null
            );
        }
    }
}
