package pers.neige.colonel.reader;

import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

/**
 * 字符串解析器
 */
@Getter
@Setter
@SuppressWarnings("unused")
public class StringReader {
    /**
     * 默认分隔符
     */
    public static final char DEFAULT_SEPARATOR = ' ';
    /**
     * 默认转义符
     */
    public static final char DEFAULT_ESCAPE = '\\';

    private static final int INTEGER_POSITIVE_LIMIT = -Integer.MAX_VALUE;
    private static final int INTEGER_NEGATIVE_LIMIT = Integer.MIN_VALUE;
    private static final int INTEGER_MULTMIN = INTEGER_POSITIVE_LIMIT / 10;

    private static final long LONG_POSITIVE_LIMIT = -Long.MAX_VALUE;
    private static final long LONG_NEGATIVE_LIMIT = Long.MIN_VALUE;
    private static final long LONG_MULTMIN = LONG_POSITIVE_LIMIT / 10;

    /**
     * 待读取字符串
     */
    private final @NonNull String string;
    /**
     * 分隔符
     */
    private final char separator;
    /**
     * 转义符
     */
    private final char escape;
    /**
     * 当前偏移量
     */
    private int offset;

    /**
     * 分隔符取默认值 {@link StringReader#DEFAULT_SEPARATOR}<br>
     * 分隔符取默认值 {@link StringReader#DEFAULT_ESCAPE}
     *
     * @param string 待读取字符串
     */
    public StringReader(@NonNull String string) {
        this(string, DEFAULT_SEPARATOR, DEFAULT_ESCAPE);
    }

    /**
     * 偏移量取 0
     *
     * @param string    待读取字符串
     * @param separator 分隔符
     * @param escape    转义符
     */
    public StringReader(@NonNull String string, char separator, char escape) {
        this(string, separator, escape, 0);
    }

    /**
     * @param string    待读取字符串
     * @param separator 分隔符
     * @param escape    转义符
     * @param offset    偏移量
     */
    public StringReader(@NonNull String string, char separator, char escape, int offset) {
        this.string = string;
        this.offset = offset;
        this.separator = separator;
        this.escape = escape;
    }

    /**
     * 根据当前状态复制一个 StringReader
     *
     * @return StringReader 副本
     */
    public StringReader copy() {
        return new StringReader(string, separator, escape, offset);
    }

    /**
     * 当前偏移位置是否存在字符
     *
     * @return 当前偏移位置是否存在字符
     */
    public boolean canRead() {
        return offset < string.length();
    }

    /**
     * 返回当前偏移量对应的字符
     *
     * @return 当前偏移量对应的字符
     */
    public char current() {
        return string.charAt(offset);
    }

    /**
     * 偏移量 + 1
     */
    public void skip() {
        this.offset++;
    }

    /**
     * 如果给定字符集包含当前字符，则偏移量 + 1
     *
     * @param chars 给定字符集
     */
    public void skipIfContains(Set<Character> chars) {
        if (canRead() && chars.contains(current())) {
            this.offset++;
        }
    }

    /**
     * 偏移量 + offset
     *
     * @param offset 移动的偏移量
     */
    public void skip(int offset) {
        this.offset += offset;
    }

    /**
     * 跳过分隔符
     */
    public boolean skipSeparator() {
        boolean skipped = false;
        while (canRead() && current() == separator) {
            skipped = true;
            skip();
        }
        return skipped;
    }

    /**
     * 重置偏移量
     */
    public void reset() {
        offset = 0;
    }

    /**
     * 查看后方指定长度的字符串, 超长则返回剩余全部字符串(不移动偏移量)
     *
     * @param length 查看的长度
     * @return 后方指定长度的字符串
     */
    public @NonNull String peek(int length) {
        return string.substring(offset, Math.min(string.length(), offset + length));
    }

    /**
     * 查看后方指定长度的字符串, 超长则返回剩余全部字符串(移动偏移量)
     *
     * @param length 查看的长度
     * @return 后方指定长度的字符串
     */
    public @NonNull String read(int length) {
        val result = string.substring(offset, Math.min(string.length(), offset + length));
        offset += result.length();
        return result;
    }

    /**
     * 查看已读取的所有字符(不移动偏移量)
     *
     * @return 已读取的所有字符
     */
    public @NonNull String peekPrevious() {
        return string.substring(0, Math.min(string.length(), offset));
    }

    /**
     * 查看已读取的所有字符(移动偏移量)
     *
     * @return 已读取的所有字符
     */
    public @NonNull String readPrevious() {
        val end = Math.min(string.length(), offset);
        offset = 0;
        return string.substring(0, end);
    }

    /**
     * 查看前方指定长度的字符, 超长则返回已读取的所有字符(不移动偏移量)
     *
     * @param length 查看的长度
     * @return 已读取的所有字符
     */
    public @NonNull String peekPrevious(int length) {
        val end = Math.min(string.length(), offset);
        return string.substring(Math.max(0, end - length), end);
    }

    /**
     * 查看前方指定长度的字符, 超长则返回已读取的所有字符(移动偏移量)
     *
     * @param length 查看的长度
     * @return 已读取的所有字符
     */
    public @NonNull String readPrevious(int length) {
        val end = Math.min(string.length(), offset);
        val start = Math.max(0, end - length);
        offset = start;
        return string.substring(start, end);
    }

    /**
     * 查看剩余的所有字符(不移动偏移量)
     *
     * @return 剩余的所有字符
     */
    public @NonNull String peekRemaining() {
        if (canRead()) {
            return string.substring(offset);
        } else {
            return "";
        }
    }

    /**
     * 查看剩余的所有字符(移动偏移量)
     *
     * @return 剩余的所有字符
     */
    public @NonNull String readRemaining() {
        String result;
        if (canRead()) {
            result = string.substring(offset);
            offset = string.length();
        } else {
            result = "";
        }
        return result;
    }

    /**
     * 从当前偏移量开始读取一段转义后的文本片段
     * <p>
     * 该方法遵循以下解析规则：
     * <ol>
     *   <li>当<b>未遇到转义符且未找到分隔符</b>时，读取剩余全部内容</li>
     *   <li>当<b>遇到未转义的分隔符</b>时，读取至分隔符前的内容</li>
     *   <li>当<b>转义符后紧跟分隔符</b>时，将分隔符作为普通字符读取</li>
     *   <li>当<b>转义符后紧跟转义符</b>时，解析为单个转义符字符</li>
     *   <li>当<b>转义符后跟非特殊字符</b>时，保留转义符和字符原义</li>
     *   <li>当<b>转义符出现在末尾</b>时，解析为单个转义符字符</li>
     *   <li>当<b>输入为空字符串</b>时，返回空字符串</li>
     * </ol>
     * <p>
     * <b>示例（假设分隔符为空格，转义符为反斜杠）：</b>
     * <pre>
     * 输入 "hello"                → 返回 "hello"（偏移量移动到分隔符处）
     * 输入 "hello hello"          → 返回 "hello"（偏移量移动到分隔符处）
     * 输入 "hello\\ hello"        → 返回 "hello hello"（偏移量移动到分隔符处）
     * 输入 "hello\\\\ hello"      → 返回 "hello\\"（偏移量移动到分隔符处）
     * 输入 "hello\\hello"         → 返回 "hello\\hello"（偏移量移动到分隔符处）
     * 输入 "hello\\"              → 返回 "hello\\"（偏移量移动到分隔符处）
     * 输入 "hello\ hello hello"   → 返回 "hello hello"（偏移量移动到分隔符处）
     * 输入 ""                     → 返回 ""（偏移量移动到分隔符处）
     * </pre>
     *
     * @return 一段参数文本，若无法识别则返回空字符串 {@code ""}
     */
    public @NonNull String readString() {
        val result = new StringBuilder();
        var escaping = false;

        while (canRead()) {
            char current = current();

            if (!escaping) {
                if (current == escape) {
                    escaping = true;
                    skip();
                    continue;
                }

                if (current == separator) {
                    break;
                }
            } else if (current != escape && current != separator) {
                result.append(escape);
            }

            result.append(current);
            escaping = false;
            skip();
        }

        if (escaping) {
            result.append(escape);
        }

        return result.toString();
    }

    /**
     * {@code return readString().toLowerCase();}
     *
     * @return 一段转小写的参数文本，若无法识别则返回空字符串
     * @see StringReader#readString()
     */
    public @NonNull String readLowerCaseString() {
        return readString().toLowerCase();
    }

    /**
     * 从当前偏移量开始尝试读取一个十进制整数字符串
     * <p>
     * 该方法遵循以下解析规则：
     * <ol>
     *   <li>允许以 + 或 - 符号开头</li>
     *   <li>符号后必须紧跟至少一个数字字符（0-9）</li>
     *   <li>当遇到分隔符或已达字符串尽头时停止读取</li>
     *   <li>当遇到非分隔符的非数字字符时读取失败</li>
     *   <li>若读取成功，偏移量将移动到终止位置（即分隔符索引位置，或文本长度），并返回对应的整数字符串</li>
     *   <li>若读取失败，偏移量将回滚至起始位置，并返回 null</li>
     * </ol>
     * <p>
     * <b>示例（假设分隔符为空格）：</b>
     * <pre>
     * 输入 "123 hello"        → 返回 "123"（偏移量移动到分隔符处）
     * 输入 "+123 hello"       → 返回 "+123"（偏移量移动到分隔符处）
     * 输入 "-123 hello"       → 返回 "-123"（偏移量移动到分隔符处）
     * 输入 "123"              → 返回 "123"（偏移量移动到末尾）
     * 输入 "+123"             → 返回 "+123"（偏移量移动到末尾）
     * 输入 "-123"             → 返回 "-123"（偏移量移动到末尾）
     * 输入 "hello hello"      → 返回 null（偏移量回滚）
     * 输入 "123.3 hello"      → 返回 null（偏移量回滚）
     * 输入 "123hello hello"   → 返回 null（偏移量回滚）
     * 输入 "+"                → 返回 null（偏移量回滚）
     * 输入 "-"                → 返回 null（偏移量回滚）
     * 输入 ""                 → 返回 null（偏移量回滚）
     * </pre>
     *
     * @return 解析成功的整数字符串（含符号），若无法识别则返回 {@code null}
     */
    public @Nullable String readIntegerString() {
        if (!canRead()) {
            return null;
        }

        val start = offset;

        val firstChar = current();
        if (firstChar == '+' || firstChar == '-') {
            skip();
        }

        val digitsStart = offset;

        while (canRead()) {
            val current = current();
            if (current >= '0' && current <= '9') {
                skip();
            } else {
                break;
            }
        }

        if (digitsStart == offset || (canRead() && current() != separator)) {
            offset = start;
            return null;
        }

        return string.substring(start, offset);
    }

    /**
     * 从当前偏移量开始尝试读取一个十进制小数字符串
     * <p>
     * 该方法遵循以下解析规则：
     * <ol>
     *   <li>允许以 + 或 - 符号开头</li>
     *   <li>允许 数字.数字 的格式</li>
     *   <li>允许 数字. 的格式</li>
     *   <li>允许 .数字 的格式</li>
     *   <li>当遇到分隔符或已达字符串尽头时停止读取</li>
     *   <li>当遇到非分隔符的非数字字符时读取失败</li>
     *   <li>若读取成功，偏移量将移动到终止位置（即分隔符索引位置，或文本长度），并返回对应的整数字符串</li>
     *   <li>若读取失败，偏移量将回滚至起始位置，并返回 null</li>
     * </ol>
     * <p>
     * <b>示例（假设分隔符为空格）：</b>
     * <pre>
     * 输入 "123.456 hello"        → 返回 "123.456"（偏移量移动到分隔符处）
     * 输入 "+123.456 hello"       → 返回 "+123.456"（偏移量移动到分隔符处）
     * 输入 "-123.456 hello"       → 返回 "-123.456"（偏移量移动到分隔符处）
     * 输入 ".456 hello"           → 返回 ".456"（偏移量移动到分隔符处）
     * 输入 "+.456 hello"          → 返回 "+.456"（偏移量移动到分隔符处）
     * 输入 "-.456 hello"          → 返回 "-.456"（偏移量移动到分隔符处）
     * 输入 "123. hello"           → 返回 "123."（偏移量移动到分隔符处）
     * 输入 "+123. hello"          → 返回 "+123."（偏移量移动到分隔符处）
     * 输入 "-123. hello"          → 返回 "-123."（偏移量移动到分隔符处）
     * 输入 "123 hello"            → 返回 "123"（偏移量移动到分隔符处）
     * 输入 "+123 hello"           → 返回 "+123"（偏移量移动到分隔符处）
     * 输入 "-123 hello"           → 返回 "-123"（偏移量移动到分隔符处）
     * 输入 "123.456"              → 返回 "123.456"（偏移量移动到末尾）
     * 输入 "+123.456"             → 返回 "+123.456"（偏移量移动到末尾）
     * 输入 "-123.456"             → 返回 "-123.456"（偏移量移动到末尾）
     * 输入 ".456"                 → 返回 ".456"（偏移量移动到末尾）
     * 输入 "+.456"                → 返回 "+.456"（偏移量移动到末尾）
     * 输入 "-.456"                → 返回 "-.456"（偏移量移动到末尾）
     * 输入 "123."                 → 返回 "123."（偏移量移动到末尾）
     * 输入 "+123."                → 返回 "+123."（偏移量移动到末尾）
     * 输入 "-123."                → 返回 "-123."（偏移量移动到末尾）
     * 输入 "123"                  → 返回 "123."（偏移量移动到末尾）
     * 输入 "+123"                 → 返回 "+123."（偏移量移动到末尾）
     * 输入 "-123"                 → 返回 "-123."（偏移量移动到末尾）
     * 输入 "hello hello"          → 返回 null（偏移量回滚）
     * 输入 "123.456hello hello"   → 返回 null（偏移量回滚）
     * 输入 "123..456 hello"       → 返回 null（偏移量回滚）
     * 输入 "+"                    → 返回 null（偏移量回滚）
     * 输入 "-"                    → 返回 null（偏移量回滚）
     * 输入 "+."                    → 返回 null（偏移量回滚）
     * 输入 "-."                    → 返回 null（偏移量回滚）
     * 输入 "."                    → 返回 null（偏移量回滚）
     * 输入 ""                     → 返回 null（偏移量回滚）
     * </pre>
     *
     * @return 解析成功的整数字符串（含符号），若无法识别则返回 {@code null}
     */
    public @Nullable String readDecimalString() {
        if (!canRead()) {
            return null;
        }

        val start = offset;
        var hasDot = false;

        val firstChar = current();
        if (firstChar == '+' || firstChar == '-') {
            skip();
        }

        val digitsStart = offset;

        while (canRead()) {
            val current = current();
            if (current >= '0' && current <= '9') {
                skip();
            } else {
                break;
            }
        }

        if (canRead() && current() == '.') {
            hasDot = true;
            skip();

            while (canRead()) {
                val current = current();
                if (current >= '0' && current <= '9') {
                    skip();
                } else {
                    break;
                }
            }
        }

        if (digitsStart == offset || (canRead() && current() != separator) || (hasDot && offset == digitsStart + 1)) {
            offset = start;
            return null;
        }

        return string.substring(start, offset);
    }

    /**
     * 从当前偏移量开始以十进制尝试读取一个 Byte
     * <p>
     * 该方法解析逻辑类似于 {@link StringReader#readIntegerString()}<br>
     * 大于 Byte.MAX_VALUE ，将返回 Byte.MAX_VALUE<br>
     * 小于 Byte.MIN_VALUE ，将返回 Byte.MIN_VALUE
     *
     * @return 解析成功的 Byte，若无法识别则返回 {@code null}
     * @see StringReader#readIntegerString()
     */
    public @Nullable Byte readByte() {
        val integer = readInteger();
        if (integer == null) return null;
        if (integer < Byte.MIN_VALUE) return Byte.MIN_VALUE;
        if (integer > Byte.MAX_VALUE) return Byte.MAX_VALUE;
        return integer.byteValue();
    }

    /**
     * 从当前偏移量开始以十进制尝试读取一个 Byte
     * <p>
     * 该方法解析逻辑类似于 {@link StringReader#readIntegerString()}<br>
     * 大于 Byte.MAX_VALUE ，将返回 Byte.MAX_VALUE<br>
     * 小于 Byte.MIN_VALUE ，将返回 Byte.MIN_VALUE
     *
     * @param def 默认值
     * @return 解析成功的 Byte，若无法识别则返回 {@code def}
     * @see StringReader#readIntegerString()
     */
    public byte readByte(byte def) {
        val integer = readInteger(def);
        if (integer < Byte.MIN_VALUE) return Byte.MIN_VALUE;
        if (integer > Byte.MAX_VALUE) return Byte.MAX_VALUE;
        return (byte) integer;
    }

    /**
     * 从当前偏移量开始以十进制尝试读取一个 Short
     * <p>
     * 该方法解析逻辑类似于 {@link StringReader#readIntegerString()}<br>
     * 大于 Short.MAX_VALUE ，将返回 Short.MAX_VALUE<br>
     * 小于 Short.MIN_VALUE ，将返回 Short.MIN_VALUE
     *
     * @return 解析成功的 Short，若无法识别则返回 {@code null}
     * @see StringReader#readIntegerString()
     */
    public @Nullable Short readShort() {
        val integer = readInteger();
        if (integer == null) return null;
        if (integer < Short.MIN_VALUE) return Short.MIN_VALUE;
        if (integer > Short.MAX_VALUE) return Short.MAX_VALUE;
        return integer.shortValue();
    }

    /**
     * 从当前偏移量开始以十进制尝试读取一个 Short
     * <p>
     * 该方法解析逻辑类似于 {@link StringReader#readIntegerString()}<br>
     * 大于 Short.MAX_VALUE ，将返回 Short.MAX_VALUE<br>
     * 小于 Short.MIN_VALUE ，将返回 Short.MIN_VALUE
     *
     * @param def 默认值
     * @return 解析成功的 Short，若无法识别则返回 {@code def}
     * @see StringReader#readIntegerString()
     */
    public short readShort(short def) {
        val integer = readInteger(def);
        if (integer < Short.MIN_VALUE) return Short.MIN_VALUE;
        if (integer > Short.MAX_VALUE) return Short.MAX_VALUE;
        return (short) integer;
    }

    /**
     * 从当前偏移量开始以十进制尝试读取一个 Integer
     * <p>
     * 该方法解析逻辑类似于 {@link StringReader#readIntegerString()}<br>
     * 大于 Integer.MAX_VALUE ，将返回 Integer.MAX_VALUE<br>
     * 小于 Integer.MIN_VALUE ，将返回 Integer.MIN_VALUE
     *
     * @return 解析成功的 Integer，若无法识别则返回 {@code null}
     * @see StringReader#readIntegerString()
     */
    public @Nullable Integer readInteger() {
        if (!canRead()) {
            return null;
        }

        val start = offset;
        var negative = false;

        var limit = INTEGER_POSITIVE_LIMIT;

        val firstChar = current();
        if (firstChar == '+') {
            skip();
        } else if (firstChar == '-') {
            limit = INTEGER_NEGATIVE_LIMIT;
            negative = true;
            skip();
        }

        val digitsStart = offset;
        var result = 0;
        var overflow = false;

        while (canRead()) {
            val current = current();
            if (current >= '0' && current <= '9') {
                if (result < INTEGER_MULTMIN) {
                    result = limit;
                    overflow = true;
                    skip();
                    break;
                }
                result *= 10;
                val digit = current - 48;
                if (result < limit + digit) {
                    result = limit;
                    overflow = true;
                    skip();
                    break;
                }
                result -= digit;
                skip();
            } else {
                break;
            }
        }

        if (overflow) {
            while (canRead()) {
                val current = current();
                if (current >= '0' && current <= '9') {
                    skip();
                } else {
                    break;
                }
            }
        }

        if (digitsStart == offset || (canRead() && current() != separator)) {
            offset = start;
            return null;
        }

        return negative ? result : -result;
    }

    /**
     * 从当前偏移量开始以十进制尝试读取一个 Integer
     * <p>
     * 该方法解析逻辑类似于 {@link StringReader#readIntegerString()}<br>
     * 大于 Integer.MAX_VALUE ，将返回 Integer.MAX_VALUE<br>
     * 小于 Integer.MIN_VALUE ，将返回 Integer.MIN_VALUE
     *
     * @param def 默认值
     * @return 解析成功的 Integer，若无法识别则返回 {@code def}
     * @see StringReader#readIntegerString()
     */
    public int readInteger(int def) {
        if (!canRead()) {
            return def;
        }

        val start = offset;
        var negative = false;

        var limit = INTEGER_POSITIVE_LIMIT;

        val firstChar = current();
        if (firstChar == '+') {
            skip();
        } else if (firstChar == '-') {
            limit = INTEGER_NEGATIVE_LIMIT;
            negative = true;
            skip();
        }

        val digitsStart = offset;
        var result = 0;
        var overflow = false;

        while (canRead()) {
            val current = current();
            if (current >= '0' && current <= '9') {
                if (result < INTEGER_MULTMIN) {
                    result = limit;
                    overflow = true;
                    skip();
                    break;
                }
                result *= 10;
                val digit = current - 48;
                if (result < limit + digit) {
                    result = limit;
                    overflow = true;
                    skip();
                    break;
                }
                result -= digit;
                skip();
            } else {
                break;
            }
        }

        if (overflow) {
            while (canRead()) {
                val current = current();
                if (current >= '0' && current <= '9') {
                    skip();
                } else {
                    break;
                }
            }
        }

        if (digitsStart == offset || (canRead() && current() != separator)) {
            offset = start;
            return def;
        }

        return negative ? result : -result;
    }

    /**
     * 从当前偏移量开始以十进制尝试读取一个 Long
     * <p>
     * 该方法解析逻辑类似于 {@link StringReader#readIntegerString()}<br>
     * 大于 Long.MAX_VALUE ，将返回 Long.MAX_VALUE<br>
     * 小于 Long.MIN_VALUE ，将返回 Long.MIN_VALUE
     *
     * @return 解析成功的 Long，若无法识别则返回 {@code null}
     * @see StringReader#readIntegerString()
     */
    public @Nullable Long readLong() {
        if (!canRead()) {
            return null;
        }

        val start = offset;
        var negative = false;

        var limit = LONG_POSITIVE_LIMIT;

        val firstChar = current();
        if (firstChar == '+') {
            skip();
        } else if (firstChar == '-') {
            limit = LONG_NEGATIVE_LIMIT;
            negative = true;
            skip();
        }

        val digitsStart = offset;
        var result = 0L;
        var overflow = false;

        while (canRead()) {
            val current = current();
            if (current >= '0' && current <= '9') {
                if (result < LONG_MULTMIN) {
                    result = limit;
                    overflow = true;
                    skip();
                    break;
                }
                result *= 10;
                val digit = current - 48;
                if (result < limit + digit) {
                    result = limit;
                    overflow = true;
                    skip();
                    break;
                }
                result -= digit;
                skip();
            } else {
                break;
            }
        }

        if (overflow) {
            while (canRead()) {
                val current = current();
                if (current >= '0' && current <= '9') {
                    skip();
                } else {
                    break;
                }
            }
        }

        if (digitsStart == offset || (canRead() && current() != separator)) {
            offset = start;
            return null;
        }

        return negative ? result : -result;
    }

    /**
     * 从当前偏移量开始以十进制尝试读取一个 Long
     * <p>
     * 该方法解析逻辑类似于 {@link StringReader#readIntegerString()}<br>
     * 大于 Long.MAX_VALUE ，将返回 Long.MAX_VALUE<br>
     * 小于 Long.MIN_VALUE ，将返回 Long.MIN_VALUE
     *
     * @param def 默认值
     * @return 解析成功的 Long，若无法识别则返回 {@code def}
     * @see StringReader#readIntegerString()
     */
    public long readLong(long def) {
        if (!canRead()) {
            return def;
        }

        val start = offset;
        var negative = false;

        var limit = LONG_POSITIVE_LIMIT;

        val firstChar = current();
        if (firstChar == '+') {
            skip();
        } else if (firstChar == '-') {
            limit = LONG_NEGATIVE_LIMIT;
            negative = true;
            skip();
        }

        val digitsStart = offset;
        var result = 0L;
        var overflow = false;

        while (canRead()) {
            val current = current();
            if (current >= '0' && current <= '9') {
                if (result < LONG_MULTMIN) {
                    result = limit;
                    overflow = true;
                    skip();
                    break;
                }
                result *= 10;
                val digit = current - 48;
                if (result < limit + digit) {
                    result = limit;
                    overflow = true;
                    skip();
                    break;
                }
                result -= digit;
                skip();
            } else {
                break;
            }
        }

        if (overflow) {
            while (canRead()) {
                val current = current();
                if (current >= '0' && current <= '9') {
                    skip();
                } else {
                    break;
                }
            }
        }

        if (digitsStart == offset || (canRead() && current() != separator)) {
            offset = start;
            return def;
        }

        return negative ? result : -result;
    }

    /**
     * 从当前偏移量开始以十进制尝试读取一个 BigInteger
     * <p>
     * 该方法解析逻辑基于 {@link StringReader#readIntegerString()}<br>
     * 实质上就是执行了一个
     * <pre>
     * String result = readIntegerString();
     * if (result == null) return null;
     * return new BigInteger(result);
     * </pre>
     *
     * @return 解析成功的 BigInteger，若无法识别则返回 {@code null}
     * @see StringReader#readIntegerString()
     */
    public @Nullable BigInteger readBigInteger() {
        val result = readIntegerString();
        if (result == null) return null;
        return new BigInteger(result);
    }

    /**
     * 从当前偏移量开始以十进制尝试读取一个 BigInteger
     * <p>
     * 该方法解析逻辑基于 {@link StringReader#readIntegerString()}<br>
     * 实质上就是执行了一个
     * <pre>
     * String result = readIntegerString();
     * if (result == null) return def;
     * return new BigInteger(result);
     * </pre>
     *
     * @param def 默认值
     * @return 解析成功的 BigInteger，若无法识别则返回 {@code def}
     * @see StringReader#readIntegerString()
     */
    public @NonNull BigInteger readBigInteger(@NonNull BigInteger def) {
        val result = readIntegerString();
        if (result == null) return def;
        return new BigInteger(result);
    }

    /**
     * 从当前偏移量开始以十进制尝试读取一个 Float
     * <p>
     * 该方法解析逻辑类似于 {@link StringReader#readDecimalString()}<br>
     * 大于 Float.MAX_VALUE ，将返回 Infinity<br>
     * 小于 -Float.MAX_VALUE ，将返回 -Infinity
     *
     * @return 解析成功的 Float，若无法识别则返回 {@code null}
     * @see StringReader#readDecimalString()
     */
    public @Nullable Float readFloat() {
        val result = readDecimalString();
        if (result == null) return null;
        return Float.parseFloat(result);
    }

    /**
     * 从当前偏移量开始以十进制尝试读取一个 Float
     * <p>
     * 该方法解析逻辑类似于 {@link StringReader#readDecimalString()}<br>
     * 大于 Float.MAX_VALUE ，将返回 Infinity<br>
     * 小于 -Float.MAX_VALUE ，将返回 -Infinity
     *
     * @param def 默认值
     * @return 解析成功的 Float，若无法识别则返回 {@code def}
     * @see StringReader#readDecimalString()
     */
    public float readFloat(float def) {
        val result = readDecimalString();
        if (result == null) return def;
        return Float.parseFloat(result);
    }

    /**
     * 从当前偏移量开始以十进制尝试读取一个 Double
     * <p>
     * 该方法解析逻辑类似于 {@link StringReader#readDecimalString()}<br>
     * 大于 Double.MAX_VALUE ，将返回 Infinity<br>
     * 小于 -Double.MAX_VALUE ，将返回 -Infinity
     *
     * @return 解析成功的 Double，若无法识别则返回 {@code null}
     * @see StringReader#readDecimalString()
     */
    public @Nullable Double readDouble() {
        val result = readDecimalString();
        if (result == null) return null;
        return Double.parseDouble(result);
    }

    /**
     * 从当前偏移量开始以十进制尝试读取一个 Double
     * <p>
     * 该方法解析逻辑类似于 {@link StringReader#readDecimalString()}<br>
     * 大于 Double.MAX_VALUE ，将返回 Infinity<br>
     * 小于 -Double.MAX_VALUE ，将返回 -Infinity
     *
     * @param def 默认值
     * @return 解析成功的 Double，若无法识别则返回 {@code def}
     * @see StringReader#readDecimalString()
     */
    public double readDouble(double def) {
        val result = readDecimalString();
        if (result == null) return def;
        return Double.parseDouble(result);
    }

    /**
     * 从当前偏移量开始以十进制尝试读取一个 BigDecimal
     * <p>
     * 该方法解析逻辑基于 {@link StringReader#readDecimalString()}<br>
     * 实质上就是执行了一个
     * <pre>
     * String result = readDecimalString();
     * if (result == null) return null;
     * return new BigDecimal(result);
     * </pre>
     *
     * @return 解析成功的 BigDecimal，若无法识别则返回 {@code null}
     * @see StringReader#readDecimalString()
     */
    public @Nullable BigDecimal readBigDecimal() {
        val result = readDecimalString();
        if (result == null) return null;
        return new BigDecimal(result);
    }

    /**
     * 从当前偏移量开始以十进制尝试读取一个 BigDecimal
     * <p>
     * 该方法解析逻辑基于 {@link StringReader#readDecimalString()}<br>
     * 实质上就是执行了一个
     * <pre>
     * String result = readDecimalString();
     * if (result == null) return def;
     * return new BigDecimal(result);
     * </pre>
     *
     * @param def 默认值
     * @return 解析成功的 BigDecimal，若无法识别则返回 {@code def}
     * @see StringReader#readDecimalString()
     */
    public @NonNull BigDecimal readBigDecimal(@NonNull BigDecimal def) {
        val result = readDecimalString();
        if (result == null) return def;
        return new BigDecimal(result);
    }

    /**
     * 从当前偏移量开始读取一段转义后的文本片段，并尝试转换为布尔量
     * <p>
     * 该方法解析逻辑为，先通过 {@link StringReader#readString()} 方法获取一段字符串<br>
     * 如果字符串在忽视大小写差异的前提下等同于字符串 {@code "true"} 则返回 {@code true}<br>
     * 如果字符串在忽视大小写差异的前提下等同于字符串 {@code "false"} 则返回 {@code false}<br>
     * 如果二者皆不符合，则返回 {@code null}
     *
     * @return 解析成功的布尔量，若无法识别则返回 {@code null}
     * @see StringReader#readString()
     */
    public @Nullable Boolean readBoolean() {
        val text = readString();
        if (text.isEmpty()) return null;
        if (text.equalsIgnoreCase("true")) return true;
        if (text.equalsIgnoreCase("false")) return false;
        return null;
    }

    /**
     * 从当前偏移量开始读取一段转义后的文本片段，并尝试转换为布尔量
     * <p>
     * 该方法解析逻辑为，先通过 {@link StringReader#readString()} 方法获取一段字符串<br>
     * 如果字符串在忽视大小写差异的前提下等同于字符串 {@code "true"} 则返回 {@code true}<br>
     * 如果字符串在忽视大小写差异的前提下等同于字符串 {@code "false"} 则返回 {@code false}<br>
     * 如果二者皆不符合，则返回 {@code def}
     *
     * @param def 默认值
     * @return 解析成功的布尔量，若无法识别则返回 {@code def}
     * @see StringReader#readString()
     */
    public boolean readBoolean(boolean def) {
        val text = readString();
        if (text.isEmpty()) return def;
        if (text.equalsIgnoreCase("true")) return true;
        if (text.equalsIgnoreCase("false")) return false;
        return def;
    }

    /**
     * 将当前 StringReader 转换为 java.io.Reader
     *
     * @return 经过转换的 java.io.Reader
     */
    public @NonNull StringReaderReader toReader() {
        return new StringReaderReader(this);
    }

    @AllArgsConstructor
    public static class StringReaderReader extends Reader {
        private final StringReader reader;

        @Override
        public int read(char @NonNull [] chars, int off, int len) {
            if (len == 0) {
                return 0;
            }
            if (reader.offset >= reader.string.length())
                return -1;
            int n = Math.min(reader.string.length() - reader.offset, len);
            reader.string.getChars(reader.offset, reader.offset + n, chars, off);
            reader.offset += n;
            return n;
        }

        @Override
        public void close() {
        }
    }
}
