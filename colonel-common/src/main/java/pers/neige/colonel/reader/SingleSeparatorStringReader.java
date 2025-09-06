package pers.neige.colonel.reader;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * 字符串解析器
 */
@Getter
@Setter
@SuppressWarnings("unused")
public class SingleSeparatorStringReader extends StringReader {
    /**
     * 默认分隔符
     */
    static final char DEFAULT_SEPARATOR = ' ';

    /**
     * 分隔符
     */
    private char separator;

    /**
     * 分隔符取默认值 {@link SingleSeparatorStringReader#DEFAULT_SEPARATOR}<br>
     * 分隔符取默认值 {@link SingleSeparatorStringReader#DEFAULT_ESCAPE}
     *
     * @param string 待读取字符串
     */
    public SingleSeparatorStringReader(@NonNull String string) {
        this(string, DEFAULT_SEPARATOR, DEFAULT_ESCAPE);
    }

    /**
     * 偏移量取 0
     *
     * @param string    待读取字符串
     * @param separator 分隔符
     * @param escape    转义符
     */
    public SingleSeparatorStringReader(@NonNull String string, char separator, char escape) {
        this(string, separator, escape, 0);
    }

    /**
     * @param string    待读取字符串
     * @param separator 分隔符
     * @param escape    转义符
     * @param offset    偏移量
     */
    public SingleSeparatorStringReader(@NonNull String string, char separator, char escape, int offset) {
        super(string, escape, offset);
        this.separator = separator;
    }

    @Override
    public boolean isSeparator(char c) {
        return c == separator;
    }

    @Override
    public boolean containsSeparator(@NonNull Collection<Character> chars) {
        return chars.contains(separator);
    }

    @Override
    public @NonNull StringReader newReaderWithSameConfig(@NonNull String string) {
        return new SingleSeparatorStringReader(string, separator, escape, 0);
    }

    @Override
    public @NonNull SingleSeparatorStringReader copy() {
        return new SingleSeparatorStringReader(string, separator, escape, offset);
    }

    @Override
    public @NonNull Set<Character> getSeparators() {
        return Collections.singleton(separator);
    }
}
