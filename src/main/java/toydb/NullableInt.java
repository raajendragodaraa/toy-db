package toydb;

import java.nio.ByteBuffer;
import java.util.Objects;

public class NullableInt implements Comparable<NullableInt> {
    public static final int SIZE = 5;
    public static final byte FLAG_NULL = 0x00;
    public static final byte FLAG_PRESENT = 0x01;

    private final boolean isNull;
    private final int value;

    public NullableInt() {
        this.isNull = true;
        this.value = 0;
    }

    public NullableInt(int value) {
        this.isNull = false;
        this.value = value;
    }

    public boolean isNull() {
        return isNull;
    }

    public int getValue() {
        if (isNull) {
            throw new IllegalStateException("Value is null");
        }
        return value;
    }

    public void writeToBuffer(ByteBuffer buffer) {
        if (isNull) {
            buffer.put(FLAG_NULL);
            buffer.putInt(0);
        } else {
            buffer.put(FLAG_PRESENT);
            buffer.putInt(value);
        }
    }

    public static NullableInt readFromBuffer(ByteBuffer buffer) {
        byte flag = buffer.get();
        int val = buffer.getInt();
        if (flag == FLAG_NULL) {
            return new NullableInt();
        }
        return new NullableInt(val);
    }

    @Override
    public int compareTo(NullableInt o) {
        if (this.isNull && o.isNull) return 0;
        if (this.isNull) return -1;
        if (o.isNull) return 1;
        return Integer.compare(this.value, o.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NullableInt that = (NullableInt) o;
        return isNull == that.isNull && value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isNull, value);
    }

    @Override
    public String toString() {
        return isNull ? "null" : String.valueOf(value);
    }
}
