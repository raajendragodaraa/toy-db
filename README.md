# toy-db

A lightweight, educational database storage engine and B+Tree indexing implementation built from scratch in Java.

## Features

- **On-Disk B+Tree Index (`OnDiskBPlusTree.java`):**
  - Fully configurable tree degree ($M$).
  - Dynamic **node size and padding calculation** aligned to 8-byte boundaries.
  - 13-byte disk pointers for constant-time page access.
  - On-demand page loading and recursive node splitting directly on physical disk storage.
  - Sibling pointer chaining for $O(K)$ leaf-level range scans.

- **Dynamic Node Alignment Formula (`EngineConfig.java`):**
  - Node sizes are dynamically computed for any degree $M$:
    $$\text{Payload}(M) = 1\text{ (flag)} + ((M - 1) \times 18) + 26\text{ (sibling ptrs)}$$
    $$\text{NodeSize}(M) = \left\lceil \frac{\text{Payload}(M)}{8} \right\rceil \times 8$$
  - Example computations:
    - Degree $M=3 \rightarrow \text{Payload}=63\text{B} \rightarrow \text{NodeSize}=64\text{B}$
    - Degree $M=4 \rightarrow \text{Payload}=81\text{B} \rightarrow \text{NodeSize}=88\text{B}$
    - Degree $M=6 \rightarrow \text{Payload}=117\text{B} \rightarrow \text{NodeSize}=120\text{B}$
    - Degree $M=10 \rightarrow \text{Payload}=189\text{B} \rightarrow \text{NodeSize}=192\text{B}$

- **LRU Buffer Pool Cache (`LRUCache.java`):**
  - In-memory Least-Recently-Used cache for dynamically sized nodes built on `java.util.LinkedHashMap`.
  - Dramatically reduces disk I/O operations by keeping frequently accessed routing nodes in RAM.
  - Built-in metrics tracking cache hits, misses, and hit ratio.

- **Record Storage Engine (`TableStorage.java`):**
  - Variable-length record storage for serialized row data (JSON, strings) on disk.
  - Clean separation between index metadata (`index.db`) and table heap records (`data.db`).

- **Unified Database Engine (`DatabaseEngine.java`):**
  - Clean client API supporting `insert(id, record)`, point lookups `get(id)`, and sequential scans `rangeScan(minId, maxId)`.
  - Full persistence across database restarts verified via persistent header tracking.

- **Binary Serialization & Layout (`BinaryNode.java`, `NullableInt.java`, `IndexHeader.java`):**
  - Dynamic binary page serialization using `java.nio.ByteBuffer`.
  - 5-byte nullable integer encoding (`1-byte presence flag + 4-byte int`) resolving binary zero-vs-null ambiguity and allowing key `0` to be indexed.
  - Header metadata tracking the moving root node offset across tree splits.

## Project Structure

```text
src/main/java/toydb/
├── DatabaseEngine.java     # Top-level database API (insert, get, rangeScan)
├── DatabaseShowcase.java   # End-to-end demo and verification
├── OnDiskBPlusTree.java    # On-disk B+Tree indexing with on-demand paging
├── LRUCache.java           # LRU Buffer Pool caching hot disk nodes in RAM
├── TableStorage.java       # Heap file storage for table record data
├── DiskStorageManager.java # Physical disk file I/O operations and dynamic node paging
├── BinaryNode.java         # Degree-dynamic binary page layout and serialization
├── IndexHeader.java        # Index metadata and moving root node tracking
├── NullableInt.java        # 5-byte integer wrapper distinguishing 0 from empty
├── EngineConfig.java       # Database runtime configuration settings and dynamic sizing formula
└── SimpleBPlusTree.java    # Pure in-memory reference B+Tree implementation
```

## Quick Start

Compile and run the complete database showcase:

```bash
javac -d bin src/main/java/toydb/*.java
java -cp bin toydb.DatabaseShowcase
```
