package toydb;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class EngineConfig {
    public static final int DEFAULT_DEGREE = 4;
    public static final int DEFAULT_NODE_SIZE = 80;
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
        this.bTreeDegree = DEFAULT_DEGREE;
        this.nodeSize = DEFAULT_NODE_SIZE;
        this.bTreeGrowthNodeAllocationCount = DEFAULT_ALLOCATION_BATCH;
        this.bTreeMaxFileSize = UNLIMITED_FILE_SIZE;
        this.fileAcquireTimeout = 10;
        this.fileAcquireUnit = TimeUnit.SECONDS;
        this.fileHandlerPoolMaxFiles = 20;
        this.baseDbPath = new File(".");
    }

    public EngineConfig(File baseDbPath, int bTreeDegree, int nodeSize) {
        this();
        this.baseDbPath = baseDbPath;
        this.bTreeDegree = bTreeDegree;
        this.nodeSize = nodeSize;
    }

    public int getBTreeDegree() {
        return bTreeDegree;
    }

    public void setBTreeDegree(int bTreeDegree) {
        this.bTreeDegree = bTreeDegree;
    }

    public int getNodeSize() {
        return nodeSize;
    }

    public void setNodeSize(int nodeSize) {
        this.nodeSize = nodeSize;
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
