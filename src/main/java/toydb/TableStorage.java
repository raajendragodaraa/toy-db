package toydb;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class TableStorage implements Closeable {
    private final File dataFile;
    private final RandomAccessFile file;

    public TableStorage(File dataFile) throws IOException {
        this.dataFile = dataFile;
        this.file = new RandomAccessFile(dataFile, "rw");
    }

    public synchronized BinaryNode.Pointer insertRecord(String record) throws IOException {
        byte[] bytes = record.getBytes(StandardCharsets.UTF_8);
        long position = file.length();

        file.seek(position);
        file.writeInt(bytes.length);
        file.write(bytes);

        return new BinaryNode.Pointer(BinaryNode.Pointer.TYPE_DATA, position, 0);
    }

    public synchronized String readRecord(BinaryNode.Pointer pointer) throws IOException {
        if (pointer.type != BinaryNode.Pointer.TYPE_DATA) {
            throw new IllegalArgumentException("Pointer must be of TYPE_DATA");
        }

        file.seek(pointer.position);
        int length = file.readInt();
        byte[] bytes = new byte[length];
        file.readFully(bytes);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    public File getDataFile() {
        return dataFile;
    }

    @Override
    public synchronized void close() throws IOException {
        file.close();
    }
}
