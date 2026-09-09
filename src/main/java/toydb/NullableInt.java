package toydb;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * NullableInt represents an integer value that can be null.
 * This is useful for databases or systems that need to distinguish between
 * the absence of a value (null) and a value of 0.
 * Each instance occupies exactly 5 bytes when serialized (1 byte flag + 4 bytes int).
 */
public class NullableInt implements Comparable<NullableInt> {
    // Fixed size for serialized representation: 1 byte flag + 4 bytes for int value
    public static final int SIZE = 5;
    
    // Flag byte indicating that the value is null (0x00 = 0)
    public static final byte FLAG_NULL = 0x00;
    
    // Flag byte indicating that the value is present/not null (0x01 = 1)
    public static final byte FLAG_PRESENT = 0x01;

    // Tracks whether this NullableInt represents a null value
    private final boolean isNull;
    
    // The actual integer value (only meaningful when isNull is false)
    private final int value;

    /**
     * Default constructor: creates a NullableInt with a null value.
     */
    public NullableInt() {
        this.isNull = true;
        this.value = 0;  // Value is ignored when isNull is true
    }

    /**
     * Constructor: creates a NullableInt with a specific integer value.
     * @param value the non-null integer value to store
     */
    public NullableInt(int value) {
        this.isNull = false;
        this.value = value;
    }

    /**
     * Checks whether this NullableInt represents a null value.
     * @return true if this value is null, false otherwise
     */
    public boolean isNull() {
        return isNull;
    }

    /**
     * Retrieves the integer value.
     * @return the stored integer value
     * @throws IllegalStateException if the value is null; always call isNull() first
     */
    public int getValue() {
        if (isNull) {
            throw new IllegalStateException("Value is null");
        }
        return value;
    }

    /**
     * Serializes this NullableInt into a ByteBuffer.
     * Format: 1 byte flag (0x00 for null, 0x01 for present) + 4 bytes integer value
     * @param buffer the ByteBuffer to write to; must have at least 5 bytes remaining
     */
    public void writeToBuffer(ByteBuffer buffer) {
        if (isNull) {
            // Write FLAG_NULL followed by a 0 as placeholder (not used for null values)
            buffer.put(FLAG_NULL);
            buffer.putInt(0);
        } else {
            // Write FLAG_PRESENT followed by the actual integer value
            buffer.put(FLAG_PRESENT);
            buffer.putInt(value);
        }
    }

    /**
     * Deserializes a NullableInt from a ByteBuffer.
     * Reads 5 bytes: 1 flag byte and 4 bytes for the integer value.
     * @param buffer the ByteBuffer to read from; must have at least 5 bytes remaining
     * @return a new NullableInt instance: null if flag is 0x00, or with the value if flag is 0x01
     */
    public static NullableInt readFromBuffer(ByteBuffer buffer) {
        byte flag = buffer.get();  // Read the flag byte
        int val = buffer.getInt();  // Read the integer value
        
        // If flag indicates null, return a null NullableInt (ignoring the int value)
        if (flag == FLAG_NULL) {
            return new NullableInt();
        }
        // Otherwise, return a NullableInt with the value
        return new NullableInt(val);
    }

    /**
     * Compares this NullableInt with another NullableInt.
     * Null values are considered less than any non-null value.
     * Two null values are considered equal.
     * @param o the other NullableInt to compare with
     * @return negative if this < o, zero if this == o, positive if this > o
     */
    @Override
    public int compareTo(NullableInt o) {
        // Both values are null: they are equal
        if (this.isNull && o.isNull) return 0;
        // This value is null but other is not: this is less
        if (this.isNull) return -1;
        // This value is not null but other is: this is greater
        if (o.isNull) return 1;
        // Both are non-null: compare their integer values
        return Integer.compare(this.value, o.value);
    }

    /**
     * Checks equality with another object.
     * Two NullableInt objects are equal if they have the same null state and value.
     * @param o the object to compare with
     * @return true if both objects are NullableInt with the same null state and value
     */
    @Override
    public boolean equals(Object o) {
        // Fast path: same object reference
        if (this == o) return true;
        // Null or different class: not equal
        if (o == null || getClass() != o.getClass()) return false;
        // Cast and compare both the null state and value
        NullableInt that = (NullableInt) o;
        return isNull == that.isNull && value == that.value;
    }

    /**
     * Computes the hash code for this NullableInt.
     * Combines the hash of both the null state and value to ensure
     * that equal objects produce the same hash code.
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(isNull, value);
    }

    /**
     * Returns a string representation of this NullableInt.
     * @return "null" if the value is null, otherwise the string representation of the value
     */
    @Override
    public String toString() {
        return isNull ? "null" : String.valueOf(value);
    }
}
