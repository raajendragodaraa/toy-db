# toy-db

A lightweight, educational database storage engine and B+Tree indexing implementation built from scratch in Java.

## Features

- **On-Disk B+Tree Index (`OnDiskBPlusTree.java`):**
  - Parameterized tree degree ($M = 4$, max 3 keys per node).
  - Fixed-size **80-byte node alignment** with 13-byte disk pointers for constant-time page access.
  - On-demand page loading and recursive node splitting directly on physical disk storage.
  - Sibling pointer chaining for $O(K)$ leaf-level range scans.

- **LRU Buffer Pool Cache (`LRUCache.java`):**
  - In-memory Least-Recently-Used cache for 80-byte nodes built on `java.util.LinkedHashMap`.
  - Dramatically reduces disk I/O operations by keeping frequently accessed routing nodes in RAM.
  - Built-in metrics tracking cache hits, misses, and hit ratio.

- **Record Storage Engine (`TableStorage.java`):**
  - Variable-length record storage for serialized row data (JSON, strings) on disk.
  - Clean separation between index metadata (`index.db`) and table heap records (`data.db`).

- **Unified Database Engine (`DatabaseEngine.java`):**
  - Clean client API supporting `insert(id, record)`, point lookups `get(id)`, and sequential scans `rangeScan(minId, maxId)`.
  - Full persistence across database restarts verified via persistent header tracking.

- **Binary Serialization & Layout (`BinaryNode.java`, `NullableInt.java`, `IndexHeader.java`, `EngineConfig.java`):**
  - 80-byte binary page layout and 13-byte disk pointer encoding using `java.nio.ByteBuffer`.
  - 5-byte nullable integer encoding resolving the binary zero-vs-null ambiguity.
  - Header metadata tracking the moving root node offset across tree splits.

## Project Structure

```text
src/main/java/toydb/
├── DatabaseEngine.java     # Top-level database API (insert, get, rangeScan)
├── DatabaseShowcase.java   # End-to-end demo and verification
├── OnDiskBPlusTree.java    # On-disk B+Tree indexing with on-demand paging
├── LRUCache.java           # LRU Buffer Pool caching hot disk nodes in RAM
├── TableStorage.java       # Heap file storage for table record data
├── DiskStorageManager.java # Physical disk file I/O operations and 80-byte node paging
├── BinaryNode.java         # 80-byte aligned binary page layout and serialization
├── IndexHeader.java        # Index metadata and moving root node tracking
├── NullableInt.java        # 5-byte integer wrapper distinguishing 0 from empty
├── EngineConfig.java       # Database runtime configuration settings
└── SimpleBPlusTree.java    # Pure in-memory reference B+Tree implementation
```

## Quick Start

Compile and run the complete database showcase:

```bash
javac -d bin src/main/java/toydb/*.java
java -cp bin toydb.DatabaseShowcase
```
