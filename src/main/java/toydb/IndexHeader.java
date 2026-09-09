package toydb;

import java.nio.ByteBuffer;

public class IndexHeader {
    public static final int HEADER_SIZE = 4 + 4 + 4 + BinaryNode.Pointer.SIZE;

    private int degree;
    private int nodeSize;
    private int totalNodes;
    private BinaryNode.Pointer rootPointer;

    public IndexHeader(int degree, int nodeSize, int totalNodes, BinaryNode.Pointer rootPointer) {
        this.degree = degree;
        this.nodeSize = nodeSize;
        this.totalNodes = totalNodes;
        this.rootPointer = rootPointer;
    }

    public int getDegree() {
        return degree;
    }

    public int getNodeSize() {
        return nodeSize;
    }

    public int getTotalNodes() {
        return totalNodes;
    }

    public void incrementTotalNodes() {
        this.totalNodes++;
    }

    public BinaryNode.Pointer getRootPointer() {
        return rootPointer;
    }

    public void setRootPointer(BinaryNode.Pointer rootPointer) {
        this.rootPointer = rootPointer;
    }

    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.putInt(degree);
        buffer.putInt(nodeSize);
        buffer.putInt(totalNodes);
        rootPointer.writeToBuffer(buffer);
        return buffer.array();
    }

    public static IndexHeader deserialize(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int degree = buffer.getInt();
        int nodeSize = buffer.getInt();
        int totalNodes = buffer.getInt();
        BinaryNode.Pointer rootPointer = BinaryNode.Pointer.readFromBuffer(buffer);
        return new IndexHeader(degree, nodeSize, totalNodes, rootPointer);
    }
}
