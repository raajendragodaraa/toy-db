package toydb;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class EngineConfig {
    public static final int DEFAULT_DEGREE = 4;
    public static final int DEFAULT_ALLOCATION_BATCH = 20;
    public static final long UNLIMITED_FILE_SIZE = -1L;

    private int bTreeDegree;
    private int nodeSize;
    private int bTreeGrowthNodeAllocationCount;
    private long bTreeMaxFileSize;
    private int fileAcquireTimeout;
    private TimeUnit fileAcquireUnit;
    private int fileHandlerPoolMaxFiles;
    private File baseDbPath;

    public EngineConfig() {
        this(DEFAULT_DEGREE);
    }

    public EngineConfig(int degree) {
        this.bTreeDegree = degree;
        this.nodeSize = calculateNodeSize(degree);
        this.bTreeGrowthNodeAllocationCount = DEFAULT_ALLOCATION_BATCH;
        this.bTreeMaxFileSize = UNLIMITED_FILE_SIZE;
        this.fileAcquireTimeout = 10;
        this.fileAcquireUnit = TimeUnit.SECONDS;
        this.fileHandlerPoolMaxFiles = 20;
        this.baseDbPath = new File(".");
    }

    public EngineConfig(File baseDbPath, int degree) {
        this(degree);
        this.baseDbPath = baseDbPath;
    }

    public static int calculateLeafPayload(int degree) {
        int maxKeys = degree - 1;
        return 1 + (maxKeys * (NullableInt.SIZE + BinaryNode.Pointer.SIZE)) + (2 * BinaryNode.Pointer.SIZE);
    }

    public static int calculateInternalPayload(int degree) {
        int maxKeys = degree - 1;
        return 1 + BinaryNode.Pointer.SIZE + (maxKeys * (NullableInt.SIZE + BinaryNode.Pointer.SIZE));
    }

    public static int calculateNodeSize(int degree) {
        int maxPayload = Math.max(calculateLeafPayload(degree), calculateInternalPayload(degree));
        return ((maxPayload + 7) / 8) * 8;
    }

    public int getBTreeDegree() {
        return bTreeDegree;
    }

    public void setBTreeDegree(int bTreeDegree) {
        this.bTreeDegree = bTreeDegree;
        this.nodeSize = calculateNodeSize(bTreeDegree);
    }

    public int getNodeSize() {
        return nodeSize;
    }

    public int getBTreeGrowthNodeAllocationCount() {
        return bTreeGrowthNodeAllocationCount;
    }

    public void setBTreeGrowthNodeAllocationCount(int count) {
        this.bTreeGrowthNodeAllocationCount = count;
    }

    public long getBTreeMaxFileSize() {
        return bTreeMaxFileSize;
    }

    public void setBTreeMaxFileSize(long maxFileSize) {
        this.bTreeMaxFileSize = maxFileSize;
    }

    public int getFileAcquireTimeout() {
        return fileAcquireTimeout;
    }

    public void setFileAcquireTimeout(int timeout, TimeUnit unit) {
        this.fileAcquireTimeout = timeout;
        this.fileAcquireUnit = unit;
    }

    public TimeUnit getFileAcquireUnit() {
        return fileAcquireUnit;
    }

    public int getFileHandlerPoolMaxFiles() {
        return fileHandlerPoolMaxFiles;
    }

    public void setFileHandlerPoolMaxFiles(int maxFiles) {
        this.fileHandlerPoolMaxFiles = maxFiles;
    }

    public File getBaseDbPath() {
        return baseDbPath;
    }

    public void setBaseDbPath(File baseDbPath) {
        this.baseDbPath = baseDbPath;
    }
}
