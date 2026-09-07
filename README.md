# toy-db

A lightweight, educational database storage engine and B+Tree indexing implementation built from scratch in Java.

## Features

- **In-Memory B+Tree Index (`SimpleBPlusTree.java`):**
  - Parameterized tree degree ($M = 4$, max 3 keys per node).
  - Point lookup (`search`) in $O(\log N)$.
  - Range scan queries (`rangeSearch`) in $O(K)$ using doubly-linked leaf nodes.
  - Dynamic node splitting for leaves and internal routing nodes.

- **Binary Node Memory Layout (`BinaryNode.java`):**
  - Fixed-size binary representation for disk paging.
  - 13-byte disk pointers (1 byte type flag, 8 bytes position/offset, 4 bytes chunk ID).
  - Bitwise flag encoding for node role identification (Root, Internal, Leaf).
  - High-performance binary serialization and deserialization using `java.nio.ByteBuffer`.

## Project Structure

```text
src/main/java/toydb/
├── SimpleBPlusTree.java   # B+Tree data structure and operations
└── BinaryNode.java        # Binary page layout, pointer structure, and serialization
```
