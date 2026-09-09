package toydb;

import java.nio.ByteBuffer;

/**
 * IndexHeader represents the metadata header for a B-tree index structure.
 * It stores information about the B-tree configuration and the root node pointer.
 * This header is typically persisted at the beginning of an index file.
 */
public class IndexHeader {
    // HEADER_SIZE defines the total byte size of a serialized IndexHeader
    // Breakdown: 4 bytes for degree + 4 bytes for nodeSize + 4 bytes for totalNodes + BinaryNode.Pointer.SIZE for rootPointer
    public static final int HEADER_SIZE = 4 + 4 + 4 + BinaryNode.Pointer.SIZE;

    // degree: The maximum number of children a B-tree node can have (determines tree structure)
    private int degree;
    // nodeSize: The size in bytes of each individual node (used for storage/memory allocation)
    private int nodeSize;
    // totalNodes: The total count of nodes currently in the B-tree index
    private int totalNodes;
    // rootPointer: A pointer to the root node of the B-tree (contains offset/location info)
    private BinaryNode.Pointer rootPointer;

    /**
     * Constructor to initialize an IndexHeader with all necessary B-tree configuration parameters.
     * @param degree the B-tree degree (max children per node)
     * @param nodeSize the size of each node in bytes
     * @param totalNodes the current number of nodes in the tree
     * @param rootPointer the pointer to the root node
     */
    public IndexHeader(int degree, int nodeSize, int totalNodes, BinaryNode.Pointer rootPointer) {
        // Initialize all fields with the provided parameters
        this.degree = degree;
        this.nodeSize = nodeSize;
        this.totalNodes = totalNodes;
        this.rootPointer = rootPointer;
    }

    /**
     * Returns the degree of the B-tree (maximum number of children per node).
     * This determines how balanced the tree structure is.
     */
    public int getDegree() {
        return degree;
    }

    /**
     * Returns the size of each individual node in bytes.
     * This is used for memory allocation and file I/O operations.
     */
    public int getNodeSize() {
        return nodeSize;
    }

    /**
     * Returns the total number of nodes currently in the B-tree.
     * This count includes all internal and leaf nodes.
     */
    public int getTotalNodes() {
        return totalNodes;
    }

    /**
     * Increments the total node count by 1.
     * This is typically called when a new node is added to the B-tree.
     */
    public void incrementTotalNodes() {
        this.totalNodes++;
    }

    /**
     * Returns a pointer to the root node of the B-tree.
     * The root pointer contains the location/offset information needed to access the root.
     */
    public BinaryNode.Pointer getRootPointer() {
        return rootPointer;
    }

    /**  
     * 
     * Updates the root pointer to point to a new root node.
     * This is typically called after tree restructuring operations (e.g., when the tree grows).
     */
    public void setRootPointer(BinaryNode.Pointer rootPointer) {
        this.rootPointer = rootPointer;
    }

    /**
     * Serializes the IndexHeader to a byte array for storage/persistence.
     * The byte order is: degree (4 bytes) | nodeSize (4 bytes) | totalNodes (4 bytes) | rootPointer (variable)
     * This method uses ByteBuffer to ensure consistent byte ordering (big-endian by default).
     * @return a byte array containing the serialized header data
     */
    public byte[] serialize() {
        // Allocate a ByteBuffer with exactly the size needed for this header
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        // Write each field sequentially: degree as a 32-bit integer
        buffer.putInt(degree);
        // Write nodeSize as a 32-bit integer
        buffer.putInt(nodeSize);
        // Write totalNodes as a 32-bit integer
        buffer.putInt(totalNodes);
        // Write the root pointer using its own serialization method
        rootPointer.writeToBuffer(buffer);
        // Convert the ByteBuffer to a byte array and return
        return buffer.array();
    }

    /**
     * Deserializes a byte array back into an IndexHeader object.
     * This is the inverse operation of serialize() and reconstructs all fields from bytes.
     * @param data the byte array containing serialized header data
     * @return a new IndexHeader instance with values restored from the byte array
     */
    public static IndexHeader deserialize(byte[] data) {
        // Wrap the byte array in a ByteBuffer for easy reading
        ByteBuffer buffer = ByteBuffer.wrap(data);
        // Read degree: a 32-bit integer representing the B-tree degree
        int degree = buffer.getInt();
        // Read nodeSize: a 32-bit integer representing individual node size in bytes
        int nodeSize = buffer.getInt();
        // Read totalNodes: a 32-bit integer representing the current node count
        int totalNodes = buffer.getInt();
        // Read rootPointer: deserialize the pointer from the remaining buffer bytes
        BinaryNode.Pointer rootPointer = BinaryNode.Pointer.readFromBuffer(buffer);
        // Construct and return a new IndexHeader with the deserialized values
        return new IndexHeader(degree, nodeSize, totalNodes, rootPointer);
    }
}
