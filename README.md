# toy-db

A lightweight, educational database storage engine and B+Tree indexing implementation built from scratch in Java.

## Features

- **In-Memory B+Tree Index (`SimpleBPlusTree.java`):**
  - Parameterized tree degree ($M = 4$, max 3 keys per node).
  - Point lookup (`search`) in $O(\log N)$.
  - Range scan queries (`rangeSearch`) in $O(K)$ using doubly-linked leaf nodes.
  - Dynamic node splitting for leaves and internal routing nodes.

- **Binary Node Memory Layout (`BinaryNode.java`):**
  - Fixed-size **80-byte node alignment** for direct $O(1)$ disk page offsets.
  - 13-byte disk pointers (1 byte type flag, 8 bytes position/offset, 4 bytes chunk ID).
  - Bitwise flag encoding for node role identification (Root, Internal, Leaf).
  - High-performance binary serialization and deserialization using `java.nio.ByteBuffer`.

- **Index Header & Root Tracking (`IndexHeader.java`):**
  - Tracks the moving Root pointer on disk as the tree splits and grows.
  - Stores metadata including degree, node size, and total allocated nodes.

- **Nullable Integer Support (`NullableInt.java`):**
  - 5-byte representation (`1-byte presence flag + 4-byte int`) resolving the binary "Zero vs. Null" ambiguity.

## Project Structure

```text
src/main/java/toydb/
├── SimpleBPlusTree.java   # B+Tree data structure and operations
├── BinaryNode.java        # 80-byte aligned binary page layout and serialization
├── IndexHeader.java       # Index metadata and root node tracking
└── NullableInt.java       # 5-byte integer wrapper distinguishing 0 from empty
```
