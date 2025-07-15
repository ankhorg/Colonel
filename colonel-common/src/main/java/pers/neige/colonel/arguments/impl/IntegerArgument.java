package pers.neige.colonel.arguments.impl;

import lombok.*;
import org.jetbrains.annotations.Nullable;
import pers.neige.colonel.arguments.Argument;
import pers.neige.colonel.arguments.ParseResult;
import pers.neige.colonel.reader.StringReader;

/**
 * 整形参数类型
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class IntegerArgument<S, R> extends Argument<S, Integer, R> {
    /**
     * 参数最小值<br>
     * 默认值 {@code Integer.MIN_VALUE}
     */
    @Builder.Default
    private final int minimum = Integer.MIN_VALUE;
    /**
     * 参数最大值<br>
     * 默认值 {@code Integer.MAX_VALUE}
     */
    @Builder.Default
    private final int maximum = Integer.MAX_VALUE;

    @Override
    public @NonNull ParseResult<Integer> parse(@NonNull StringReader input, @Nullable S source) {
        val result = input.readInteger();
        if (result == null) {
            return new ParseResult<>(null, false);
        } else {
            if (result < minimum || result > maximum) {
                return new ParseResult<>(result, false);
            }
            return new ParseResult<>(result, true);
        }
    }
}
