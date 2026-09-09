package toydb;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class DiskStorageManager implements Closeable {
    public static final int HEADER_OFFSET = 0;
    public static final int FIRST_NODE_OFFSET = BinaryNode.NODE_SIZE;

    private final File dbFile;
    private final RandomAccessFile file;
    private final FileChannel channel;
    private IndexHeader header;

    public DiskStorageManager(File dbFile) throws IOException {
        this.dbFile = dbFile;
        this.file = new RandomAccessFile(dbFile, "rw");
        this.channel = file.getChannel();

        if (file.length() >= IndexHeader.HEADER_SIZE) {
            this.header = loadHeader();
        } else {
            BinaryNode.Pointer initialRoot = new BinaryNode.Pointer(BinaryNode.Pointer.TYPE_NODE, FIRST_NODE_OFFSET, 0);
            this.header = new IndexHeader(4, BinaryNode.NODE_SIZE, 0, initialRoot);
            saveHeader(this.header);
            file.setLength(FIRST_NODE_OFFSET);
        }
    }

    public synchronized BinaryNode.Pointer writeNewNode(byte[] nodeBytes) throws IOException {
        if (nodeBytes.length != BinaryNode.NODE_SIZE) {
            throw new IllegalArgumentException("Node size must be exactly " + BinaryNode.NODE_SIZE + " bytes");
        }

        long position = file.length();
        file.seek(position);
        file.write(nodeBytes);

        header.incrementTotalNodes();
        saveHeader(header);

        return new BinaryNode.Pointer(BinaryNode.Pointer.TYPE_NODE, position, 0);
    }

    public synchronized byte[] readNode(BinaryNode.Pointer pointer) throws IOException {
        if (pointer.position + BinaryNode.NODE_SIZE > file.length()) {
            throw new IOException("Offset out of bounds: " + pointer.position);
        }

        file.seek(pointer.position);
        byte[] buffer = new byte[BinaryNode.NODE_SIZE];
        file.readFully(buffer);
        return buffer;
    }

    public synchronized void updateNode(BinaryNode.Pointer pointer, byte[] nodeBytes) throws IOException {
        if (nodeBytes.length != BinaryNode.NODE_SIZE) {
            throw new IllegalArgumentException("Node size must be exactly " + BinaryNode.NODE_SIZE + " bytes");
        }

        file.seek(pointer.position);
        file.write(nodeBytes);
    }

    public synchronized void updateRootPointer(BinaryNode.Pointer newRootPointer) throws IOException {
        header.setRootPointer(newRootPointer);
        saveHeader(header);
    }

    public synchronized IndexHeader loadHeader() throws IOException {
        file.seek(HEADER_OFFSET);
        byte[] headerBytes = new byte[IndexHeader.HEADER_SIZE];
        file.readFully(headerBytes);
        this.header = IndexHeader.deserialize(headerBytes);
        return this.header;
    }

    public synchronized void saveHeader(IndexHeader header) throws IOException {
        this.header = header;
        file.seek(HEADER_OFFSET);
        file.write(header.serialize());
    }

    public IndexHeader getHeader() {
        return header;
    }

    public File getDbFile() {
        return dbFile;
    }

    @Override
    public synchronized void close() throws IOException {
        if (channel.isOpen()) {
            channel.close();
        }
        file.close();
    }
}
