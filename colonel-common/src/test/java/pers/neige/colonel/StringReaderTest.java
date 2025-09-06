package pers.neige.colonel;

import lombok.val;
import lombok.var;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import pers.neige.colonel.reader.StringReader;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class StringReaderTest {
    @Test
    @Order(1)
    public void readString() {
        var reader = StringReader.of("hello");
        assertEquals("hello", reader.readString());

        reader = StringReader.of("hello hello");
        assertEquals("hello", reader.readString());

        reader = StringReader.of("hello\\ hello");
        assertEquals("hello hello", reader.readString());

        reader = StringReader.of("hello\\\\ hello");
        assertEquals("hello\\", reader.readString());

        reader = StringReader.of("hello\\hello");
        assertEquals("hello\\hello", reader.readString());

        reader = StringReader.of("hello\\");
        assertEquals("hello\\", reader.readString());

        reader = StringReader.of("hello\\ hello hello");
        assertEquals("hello hello", reader.readString());

        reader = StringReader.of("");
        assertEquals("", reader.readString());

        reader = StringReader.of("123 456");
        assertEquals('1', reader.current());
        assertEquals("123", reader.peek(3));
        assertEquals(0, reader.getOffset());
        assertTrue(reader.canRead());
        assertEquals("123", reader.read(3));
        assertEquals(3, reader.getOffset());
        assertEquals("123", reader.peekPrevious(3));
        assertEquals(3, reader.getOffset());
        assertEquals("123", reader.readPrevious(3));
        assertEquals(0, reader.getOffset());
        assertEquals("123 456", reader.peekRemaining());
        assertEquals(0, reader.getOffset());
        assertEquals("123 456", reader.readRemaining());
        assertEquals(7, reader.getOffset());
        assertFalse(reader.canRead());
        assertEquals("123 456", reader.peekPrevious());
        assertEquals(7, reader.getOffset());
        assertEquals("123 456", reader.readPrevious());
        assertEquals(0, reader.getOffset());
    }

    @Test
    @Order(2)
    public void readIntegerString() {
        var reader = StringReader.of("123 hello");
        assertEquals("123", reader.readIntegerString());
        assertEquals(3, reader.getOffset());

        reader = StringReader.of("+123 hello");
        assertEquals("+123", reader.readIntegerString());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("-123 hello");
        assertEquals("-123", reader.readIntegerString());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("123");
        assertEquals("123", reader.readIntegerString());
        assertEquals(3, reader.getOffset());

        reader = StringReader.of("+123");
        assertEquals("+123", reader.readIntegerString());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("-123");
        assertEquals("-123", reader.readIntegerString());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("hello hello");
        assertNull(reader.readIntegerString());
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("123.3 hello");
        assertNull(reader.readIntegerString());
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("123hello hello");
        assertNull(reader.readIntegerString());
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("+");
        assertNull(reader.readIntegerString());
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("-");
        assertNull(reader.readIntegerString());
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("");
        assertNull(reader.readIntegerString());
        assertEquals(0, reader.getOffset());
    }

    @Test
    @Order(3)
    public void readInteger() {
        var reader = StringReader.of("123 hello");
        assertEquals(123, reader.readInteger());
        assertEquals(3, reader.getOffset());

        reader = StringReader.of("+123 hello");
        assertEquals(123, reader.readInteger());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("-123 hello");
        assertEquals(-123, reader.readInteger());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("123");
        assertEquals(123, reader.readInteger());
        assertEquals(3, reader.getOffset());

        reader = StringReader.of("+123");
        assertEquals(123, reader.readInteger());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("-123");
        assertEquals(-123, reader.readInteger());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of(String.valueOf(Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, reader.readInteger());

        reader = StringReader.of(String.valueOf(Integer.MAX_VALUE - 1));
        assertEquals(Integer.MAX_VALUE - 1, reader.readInteger());

        reader = StringReader.of(Integer.MAX_VALUE + "1");
        assertEquals(Integer.MAX_VALUE, reader.readInteger());

        reader = StringReader.of(Integer.MAX_VALUE + "1a");
        assertNull(reader.readInteger());
        assertEquals(0, reader.getOffset());
        assertEquals(114514, reader.readInteger(114514));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of(String.valueOf(Integer.MIN_VALUE));
        assertEquals(Integer.MIN_VALUE, reader.readInteger());

        reader = StringReader.of(String.valueOf(Integer.MIN_VALUE + 1));
        assertEquals(Integer.MIN_VALUE + 1, reader.readInteger());

        reader = StringReader.of(Integer.MIN_VALUE + "1");
        assertEquals(Integer.MIN_VALUE, reader.readInteger());

        reader = StringReader.of(Integer.MIN_VALUE + "1a");
        assertNull(reader.readInteger());
        assertEquals(0, reader.getOffset());
        assertEquals(114514, reader.readInteger(114514));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("hello hello");
        assertNull(reader.readInteger());
        assertEquals(0, reader.getOffset());
        assertEquals(114514, reader.readInteger(114514));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("123.3 hello");
        assertNull(reader.readInteger());
        assertEquals(0, reader.getOffset());
        assertEquals(114514, reader.readInteger(114514));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("123hello hello");
        assertNull(reader.readInteger());
        assertEquals(0, reader.getOffset());
        assertEquals(114514, reader.readInteger(114514));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("+");
        assertNull(reader.readInteger());
        assertEquals(0, reader.getOffset());
        assertEquals(114514, reader.readInteger(114514));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("-");
        assertNull(reader.readInteger());
        assertEquals(0, reader.getOffset());
        assertEquals(114514, reader.readInteger(114514));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("");
        assertNull(reader.readInteger());
        assertEquals(0, reader.getOffset());
        assertEquals(114514, reader.readInteger(114514));
        assertEquals(0, reader.getOffset());
    }

    @Test
    @Order(4)
    public void readLong() {
        var reader = StringReader.of("123 hello");
        assertEquals(123, reader.readLong());
        assertEquals(3, reader.getOffset());

        reader = StringReader.of("+123 hello");
        assertEquals(123, reader.readLong());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("-123 hello");
        assertEquals(-123, reader.readLong());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("123");
        assertEquals(123, reader.readLong());
        assertEquals(3, reader.getOffset());

        reader = StringReader.of("+123");
        assertEquals(123, reader.readLong());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("-123");
        assertEquals(-123, reader.readLong());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of(String.valueOf(Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, reader.readLong());

        reader = StringReader.of(String.valueOf(Long.MAX_VALUE - 1));
        assertEquals(Long.MAX_VALUE - 1, reader.readLong());

        reader = StringReader.of(Long.MAX_VALUE + "1");
        assertEquals(Long.MAX_VALUE, reader.readLong());

        reader = StringReader.of(Long.MAX_VALUE + "1a");
        assertNull(reader.readLong());
        assertEquals(0, reader.getOffset());
        assertEquals(114514, reader.readLong(114514));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of(String.valueOf(Long.MIN_VALUE));
        assertEquals(Long.MIN_VALUE, reader.readLong());

        reader = StringReader.of(String.valueOf(Long.MIN_VALUE + 1));
        assertEquals(Long.MIN_VALUE + 1, reader.readLong());

        reader = StringReader.of(Long.MIN_VALUE + "1");
        assertEquals(Long.MIN_VALUE, reader.readLong());

        reader = StringReader.of(Long.MIN_VALUE + "1a");
        assertNull(reader.readLong());
        assertEquals(0, reader.getOffset());
        assertEquals(114514, reader.readLong(114514));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("hello hello");
        assertNull(reader.readLong());
        assertEquals(0, reader.getOffset());
        assertEquals(114514, reader.readLong(114514));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("123.3 hello");
        assertNull(reader.readLong());
        assertEquals(0, reader.getOffset());
        assertEquals(114514, reader.readLong(114514));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("123hello hello");
        assertNull(reader.readLong());
        assertEquals(0, reader.getOffset());
        assertEquals(114514, reader.readLong(114514));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("+");
        assertNull(reader.readLong());
        assertEquals(0, reader.getOffset());
        assertEquals(114514, reader.readLong(114514));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("-");
        assertNull(reader.readLong());
        assertEquals(0, reader.getOffset());
        assertEquals(114514, reader.readLong(114514));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("");
        assertNull(reader.readLong());
        assertEquals(0, reader.getOffset());
        assertEquals(114514, reader.readLong(114514));
        assertEquals(0, reader.getOffset());
    }

    @Test
    @Order(5)
    public void readBigInteger() {
        var reader = StringReader.of("123 hello");
        assertEquals(BigInteger.valueOf(123), reader.readBigInteger());
        assertEquals(3, reader.getOffset());

        reader = StringReader.of("+123 hello");
        assertEquals(BigInteger.valueOf(123), reader.readBigInteger());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("-123 hello");
        assertEquals(BigInteger.valueOf(-123), reader.readBigInteger());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("123");
        assertEquals(BigInteger.valueOf(123), reader.readBigInteger());
        assertEquals(3, reader.getOffset());

        reader = StringReader.of("+123");
        assertEquals(BigInteger.valueOf(123), reader.readBigInteger());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("-123");
        assertEquals(BigInteger.valueOf(-123), reader.readBigInteger());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of(String.valueOf(Integer.MAX_VALUE));
        assertEquals(BigInteger.valueOf(Integer.MAX_VALUE), reader.readBigInteger());

        reader = StringReader.of(String.valueOf(Integer.MIN_VALUE));
        assertEquals(BigInteger.valueOf(Integer.MIN_VALUE), reader.readBigInteger());

        reader = StringReader.of("hello hello");
        assertNull(reader.readBigInteger());
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("123.3 hello");
        assertNull(reader.readBigInteger());
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("123-hello hello");
        assertNull(reader.readBigInteger());
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("+");
        assertNull(reader.readBigInteger());
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("-");
        assertNull(reader.readBigInteger());
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("");
        assertNull(reader.readBigInteger());
        assertEquals(0, reader.getOffset());
    }

    @Test
    @Order(6)
    public void readDecimalString() {
        var reader = StringReader.of("123.456 hello");
        assertEquals("123.456", reader.readDecimalString());
        assertEquals(7, reader.getOffset());

        reader = StringReader.of("+123.456 hello");
        assertEquals("+123.456", reader.readDecimalString());
        assertEquals(8, reader.getOffset());

        reader = StringReader.of("-123.456 hello");
        assertEquals("-123.456", reader.readDecimalString());
        assertEquals(8, reader.getOffset());

        reader = StringReader.of(".456 hello");
        assertEquals(".456", reader.readDecimalString());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("+.456 hello");
        assertEquals("+.456", reader.readDecimalString());
        assertEquals(5, reader.getOffset());

        reader = StringReader.of("-.456 hello");
        assertEquals("-.456", reader.readDecimalString());
        assertEquals(5, reader.getOffset());

        reader = StringReader.of("123. hello");
        assertEquals("123.", reader.readDecimalString());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("+123. hello");
        assertEquals("+123.", reader.readDecimalString());
        assertEquals(5, reader.getOffset());

        reader = StringReader.of("-123. hello");
        assertEquals("-123.", reader.readDecimalString());
        assertEquals(5, reader.getOffset());

        reader = StringReader.of("123 hello");
        assertEquals("123", reader.readDecimalString());
        assertEquals(3, reader.getOffset());

        reader = StringReader.of("+123 hello");
        assertEquals("+123", reader.readDecimalString());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("-123 hello");
        assertEquals("-123", reader.readDecimalString());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("123.456");
        assertEquals("123.456", reader.readDecimalString());
        assertEquals(7, reader.getOffset());

        reader = StringReader.of("+123.456");
        assertEquals("+123.456", reader.readDecimalString());
        assertEquals(8, reader.getOffset());

        reader = StringReader.of("-123.456");
        assertEquals("-123.456", reader.readDecimalString());
        assertEquals(8, reader.getOffset());

        reader = StringReader.of(".456");
        assertEquals(".456", reader.readDecimalString());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("+.456");
        assertEquals("+.456", reader.readDecimalString());
        assertEquals(5, reader.getOffset());

        reader = StringReader.of("-.456");
        assertEquals("-.456", reader.readDecimalString());
        assertEquals(5, reader.getOffset());

        reader = StringReader.of("123.");
        assertEquals("123.", reader.readDecimalString());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("+123.");
        assertEquals("+123.", reader.readDecimalString());
        assertEquals(5, reader.getOffset());

        reader = StringReader.of("-123.");
        assertEquals("-123.", reader.readDecimalString());
        assertEquals(5, reader.getOffset());

        reader = StringReader.of("123");
        assertEquals("123", reader.readDecimalString());
        assertEquals(3, reader.getOffset());

        reader = StringReader.of("+123");
        assertEquals("+123", reader.readDecimalString());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("-123");
        assertEquals("-123", reader.readDecimalString());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("hello hello");
        assertNull(reader.readDecimalString());
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("123.456hello hello");
        assertNull(reader.readDecimalString());
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("123..456 hello");
        assertNull(reader.readDecimalString());
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("+");
        assertNull(reader.readDecimalString());
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("-");
        assertNull(reader.readDecimalString());
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("+.");
        assertNull(reader.readDecimalString());
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("-.");
        assertNull(reader.readDecimalString());
        assertEquals(0, reader.getOffset());

        reader = StringReader.of(".");
        assertNull(reader.readDecimalString());
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("");
        assertNull(reader.readDecimalString());
        assertEquals(0, reader.getOffset());
    }

    @Test
    @Order(7)
    public void readDouble() {
        var reader = StringReader.of("123.456 hello");
        assertEquals(123.456, reader.readDouble());
        assertEquals(7, reader.getOffset());

        reader = StringReader.of("+123.456 hello");
        assertEquals(123.456, reader.readDouble());
        assertEquals(8, reader.getOffset());

        reader = StringReader.of("-123.456 hello");
        assertEquals(-123.456, reader.readDouble());
        assertEquals(8, reader.getOffset());

        reader = StringReader.of(".456 hello");
        assertEquals(0.456, reader.readDouble());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("+.456 hello");
        assertEquals(0.456, reader.readDouble());
        assertEquals(5, reader.getOffset());

        reader = StringReader.of("-.456 hello");
        assertEquals(-0.456, reader.readDouble());
        assertEquals(5, reader.getOffset());

        reader = StringReader.of("123. hello");
        assertEquals(123.0, reader.readDouble());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("+123. hello");
        assertEquals(123.0, reader.readDouble());
        assertEquals(5, reader.getOffset());

        reader = StringReader.of("-123. hello");
        assertEquals(-123.0, reader.readDouble());
        assertEquals(5, reader.getOffset());

        reader = StringReader.of("123 hello");
        assertEquals(123.0, reader.readDouble());
        assertEquals(3, reader.getOffset());

        reader = StringReader.of("+123 hello");
        assertEquals(123.0, reader.readDouble());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("-123 hello");
        assertEquals(-123.0, reader.readDouble());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("123.456");
        assertEquals(123.456, reader.readDouble());
        assertEquals(7, reader.getOffset());

        reader = StringReader.of("+123.456");
        assertEquals(123.456, reader.readDouble());
        assertEquals(8, reader.getOffset());

        reader = StringReader.of("-123.456");
        assertEquals(-123.456, reader.readDouble());
        assertEquals(8, reader.getOffset());

        reader = StringReader.of(".456");
        assertEquals(0.456, reader.readDouble());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("+.456");
        assertEquals(0.456, reader.readDouble());
        assertEquals(5, reader.getOffset());

        reader = StringReader.of("-.456");
        assertEquals(-0.456, reader.readDouble());
        assertEquals(5, reader.getOffset());

        reader = StringReader.of("123.");
        assertEquals(123.0, reader.readDouble());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("+123.");
        assertEquals(123.0, reader.readDouble());
        assertEquals(5, reader.getOffset());

        reader = StringReader.of("-123.");
        assertEquals(-123.0, reader.readDouble());
        assertEquals(5, reader.getOffset());

        reader = StringReader.of("123");
        assertEquals(123.0, reader.readDouble());
        assertEquals(3, reader.getOffset());

        reader = StringReader.of("+123");
        assertEquals(123.0, reader.readDouble());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("-123");
        assertEquals(-123.0, reader.readDouble());
        assertEquals(4, reader.getOffset());

        reader = StringReader.of("hello hello");
        assertNull(reader.readDouble());
        assertEquals(0, reader.getOffset());
        assertEquals(114514.0, reader.readDouble(114514.0));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("123.456hello hello");
        assertNull(reader.readDouble());
        assertEquals(0, reader.getOffset());
        assertEquals(114514.0, reader.readDouble(114514.0));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("123..456 hello");
        assertNull(reader.readDouble());
        assertEquals(0, reader.getOffset());
        assertEquals(114514.0, reader.readDouble(114514.0));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("+");
        assertNull(reader.readDouble());
        assertEquals(0, reader.getOffset());
        assertEquals(114514.0, reader.readDouble(114514.0));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("-");
        assertNull(reader.readDouble());
        assertEquals(0, reader.getOffset());
        assertEquals(114514.0, reader.readDouble(114514.0));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("+.");
        assertNull(reader.readDouble());
        assertEquals(0, reader.getOffset());
        assertEquals(114514.0, reader.readDouble(114514.0));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("-.");
        assertNull(reader.readDouble());
        assertEquals(0, reader.getOffset());
        assertEquals(114514.0, reader.readDouble(114514.0));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of(".");
        assertNull(reader.readDouble());
        assertEquals(0, reader.getOffset());
        assertEquals(114514.0, reader.readDouble(114514.0));
        assertEquals(0, reader.getOffset());

        reader = StringReader.of("");
        assertNull(reader.readDouble());
        assertEquals(0, reader.getOffset());
        assertEquals(114514.0, reader.readDouble(114514.0));
        assertEquals(0, reader.getOffset());
    }

    @Test
    @Order(8)
    public void toReader() {
        try (val reader = StringReader.of("123 ").toReader()) {
            val chars = new char[3];
            assertEquals(3, reader.read(chars, 0, 3));
            assertEquals('1', chars[0]);
            assertEquals('2', chars[1]);
            assertEquals('3', chars[2]);
            assertEquals(1, reader.read(chars, 0, 3));
            assertEquals(' ', chars[0]);
            assertEquals(-1, reader.read(chars, 0, 3));
        }
    }
}
