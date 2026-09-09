package toydb;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DiskStorageDemo {
    public static void main(String[] args) throws IOException {
        File dbFile = new File("test_index.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }

        try (DiskStorageManager storage = new DiskStorageManager(dbFile)) {
            List<Integer> keys = List.of(10, 20);
            List<BinaryNode.Pointer> dataPointers = List.of(
                    new BinaryNode.Pointer(BinaryNode.Pointer.TYPE_DATA, 1024, 0),
                    new BinaryNode.Pointer(BinaryNode.Pointer.TYPE_DATA, 2048, 0)
            );

            BinaryNode.LeafNodeData leaf = new BinaryNode.LeafNodeData(true, keys, dataPointers, null, null);
            byte[] serializedLeaf = leaf.serialize();

            BinaryNode.Pointer leafPointer = storage.writeNewNode(serializedLeaf);
            storage.updateRootPointer(leafPointer);

            byte[] readBytes = storage.readNode(leafPointer);
            BinaryNode.LeafNodeData deserializedLeaf = BinaryNode.LeafNodeData.deserialize(readBytes);

            System.out.println("Node successfully read from disk at offset: " + leafPointer.position);
            System.out.println("Deserialized Keys: " + deserializedLeaf.keys);
            System.out.println("Is Root: " + deserializedLeaf.isRoot);
            System.out.println("Total nodes in file: " + storage.getHeader().getTotalNodes());
        } finally {
            if (dbFile.exists()) {
                dbFile.delete();
            }
        }
    }
}
