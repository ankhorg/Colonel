package pers.neige.colonel.arguments.impl;

import lombok.*;
import org.jetbrains.annotations.Nullable;
import pers.neige.colonel.arguments.Argument;
import pers.neige.colonel.arguments.ParseResult;
import pers.neige.colonel.reader.StringReader;

/**
 * 字符串参数类型
 */
@Getter
@Builder
@NoArgsConstructor
@SuppressWarnings("unused")
public class StringArgument<S, R> extends Argument<S, String, R> {
    /**
     * 文本最小长度<br>
     * 默认值 {@code 1}
     */
    @Builder.Default
    private final int minLength = 1;
    /**
     * 文本最大长度<br>
     * 默认值 {@code Integer.MAX_VALUE}
     */
    @Builder.Default
    private final int maxLength = Integer.MAX_VALUE;
    /**
     * 读取剩余全部文本<br>
     * 默认值 {@code false}
     */
    @Builder.Default
    private final boolean readAll = false;

    /**
     * @param minLength 读取剩余全部文本, 默认值 {@code 1}
     * @param maxLength 读取剩余全部文本, 默认值 {@code Integer.MAX_VALUE}
     * @param readAll   读取剩余全部文本, 默认值 {@code false}
     */
    public StringArgument(int minLength, int maxLength, boolean readAll) {
        this.minLength = minLength;
        if (minLength <= 0) {
            throw new IllegalArgumentException("minLength must be greater than 0");
        }
        this.maxLength = maxLength;
        this.readAll = readAll;
    }

    @Override
    @NonNull
    public ParseResult<String> parse(@NonNull StringReader input, @Nullable S source) {
        val start = input.getOffset();
        val result = this.readAll ? input.readRemaining() : input.readString();
        if (result.length() < minLength || result.length() > maxLength) {
            input.setOffset(start);
            return new ParseResult<>(result, false);
        }
        return new ParseResult<>(result, true);
    }
}
