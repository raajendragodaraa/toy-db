package toydb;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseEngine implements Closeable {
    private final TableStorage tableStorage;
    private final DiskStorageManager indexStorage;
    private final OnDiskBPlusTree index;

    public DatabaseEngine(File dataFile, File indexFile, int cacheCapacity) throws IOException {
        this.tableStorage = new TableStorage(dataFile);
        this.indexStorage = new DiskStorageManager(indexFile);
        this.index = new OnDiskBPlusTree(this.indexStorage, cacheCapacity);
    }

    public synchronized void insert(int id, String record) throws IOException {
        BinaryNode.Pointer dataPointer = tableStorage.insertRecord(record);
        index.insert(id, dataPointer);
    }

    public synchronized String get(int id) throws IOException {
        BinaryNode.Pointer dataPointer = index.search(id);
        if (dataPointer == null) {
            return null;
        }
        return tableStorage.readRecord(dataPointer);
    }

    public synchronized List<String> rangeScan(int minId, int maxId) throws IOException {
        List<BinaryNode.Pointer> pointers = index.rangeScan(minId, maxId);
        List<String> records = new ArrayList<>(pointers.size());
        for (BinaryNode.Pointer ptr : pointers) {
            records.add(tableStorage.readRecord(ptr));
        }
        return records;
    }

    public OnDiskBPlusTree getIndex() {
        return index;
    }

    public TableStorage getTableStorage() {
        return tableStorage;
    }

    public DiskStorageManager getIndexStorage() {
        return indexStorage;
    }

    @Override
    public synchronized void close() throws IOException {
        tableStorage.close();
        indexStorage.close();
    }
}
