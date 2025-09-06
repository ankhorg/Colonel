package pers.neige.colonel.reader;

import lombok.Getter;
import lombok.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 字符串解析器
 */
@Getter
@SuppressWarnings("unused")
public class MultiSeparatorStringReader extends StringReader {
    /**
     * 分隔符
     */
    private @NonNull Set<Character> separators;

    /**
     * 偏移量取 0
     *
     * @param string        待读取字符串
     * @param separators    分隔符
     * @param escape        转义符
     * @param defensiveCopy 如果为 {@code true}，将对传入的 {@code separators} 集合进行防御性复制，<br>
     *                      以防止外部修改影响读取器实例，这是更安全的选择。<br>
     *                      如果为 {@code false}，将直接使用传入的集合引用以提高性能。<br>
     *                      <b>警告:</b> 当设为 {@code false} 时，调用者必须保证在读取器的生命周期内，<br>
     *                      不会修改传入的 {@code separators} 集合。
     */
    public MultiSeparatorStringReader(@NonNull String string, @NonNull Set<Character> separators, char escape, boolean defensiveCopy) {
        this(string, separators, escape, 0, defensiveCopy);
    }

    /**
     * @param string        待读取字符串
     * @param separators    分隔符
     * @param escape        转义符
     * @param offset        偏移量
     * @param defensiveCopy 如果为 {@code true}，将对传入的 {@code separators} 集合进行防御性复制，<br>
     *                      以防止外部修改影响读取器实例，这是更安全的选择。<br>
     *                      如果为 {@code false}，将直接使用传入的集合引用以提高性能。<br>
     *                      <b>警告:</b> 当设为 {@code false} 时，调用者必须保证在读取器的生命周期内，<br>
     *                      不会修改传入的 {@code separators} 集合。
     */
    public MultiSeparatorStringReader(@NonNull String string, @NonNull Set<Character> separators, char escape, int offset, boolean defensiveCopy) {
        super(string, escape, offset);
        if (separators.isEmpty()) {
            throw new IllegalArgumentException("separators cannot be empty");
        }
        if (defensiveCopy) {
            this.separators = separators;
        } else {
            this.separators = Collections.unmodifiableSet(new HashSet<>(separators));
        }
    }

    /**
     * 安全地设置分隔符。
     * 内部会创建一个不可变的副本，防止外部修改。
     *
     * @param separators 分隔符集合
     */
    public void setSeparators(@NonNull Set<Character> separators) {
        unsafeSetSeparators(Collections.unmodifiableSet(new HashSet<>(separators)));
    }

    /**
     * 不安全地设置分隔符，直接使用传入的集合引用以提高性能。
     * <p>
     * <b>警告:</b> 调用者必须保证在设置之后不会再修改此 {@code separators} 集合。
     *
     * @param separators 要直接使用的分隔符集合
     */
    public void unsafeSetSeparators(@NonNull Set<Character> separators) {
        if (separators.isEmpty()) {
            throw new IllegalArgumentException("separators cannot be empty");
        }
        this.separators = separators;
    }

    @Override
    public boolean isSeparator(char c) {
        return separators.contains(c);
    }

    @Override
    public boolean containsSeparator(@NonNull Collection<Character> chars) {
        for (Character separator : separators) {
            if (chars.contains(separator)) return true;
        }
        return false;
    }

    @Override
    public @NonNull StringReader newReaderWithSameConfig(@NonNull String string) {
        return new MultiSeparatorStringReader(string, separators, escape, false);
    }

    @Override
    public @NonNull MultiSeparatorStringReader copy() {
        return new MultiSeparatorStringReader(string, separators, escape, offset, false);
    }

    @Override
    public char getSeparator() {
        return separators.iterator().next();
    }
}
