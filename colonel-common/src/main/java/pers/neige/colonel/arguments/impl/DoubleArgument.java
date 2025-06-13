package pers.neige.colonel.arguments.impl;

import lombok.*;
import org.jetbrains.annotations.Nullable;
import pers.neige.colonel.arguments.Argument;
import pers.neige.colonel.arguments.ParseResult;
import pers.neige.colonel.reader.StringReader;

/**
 * 双精度浮点数参数类型
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class DoubleArgument<S, R> extends Argument<S, Double, R> {
    /**
     * 参数最小值<br>
     * 默认值 {@code -Double.MAX_VALUE}
     */
    @Builder.Default
    private final double minimum = -Double.MAX_VALUE;
    /**
     * 参数最大值<br>
     * 默认值 {@code Double.MAX_VALUE}
     */
    @Builder.Default
    private final double maximum = Double.MAX_VALUE;

    @Override
    @NonNull
    public ParseResult<Double> parse(@NonNull StringReader input, @Nullable S source) {
        val result = input.readDouble();
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
