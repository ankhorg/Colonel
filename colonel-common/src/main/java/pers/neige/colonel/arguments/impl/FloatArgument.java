package pers.neige.colonel.arguments.impl;

import lombok.*;
import org.jetbrains.annotations.Nullable;
import pers.neige.colonel.arguments.Argument;
import pers.neige.colonel.arguments.ParseResult;
import pers.neige.colonel.reader.StringReader;

/**
 * 单精度浮点数参数类型
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class FloatArgument<S, R> extends Argument<S, Float, R> {
    /**
     * 参数最小值<br>
     * 默认值 {@code -Float.MAX_VALUE}
     */
    @Builder.Default
    private final float minimum = -Float.MAX_VALUE;
    /**
     * 参数最大值<br>
     * 默认值 {@code Float.MAX_VALUE}
     */
    @Builder.Default
    private final float maximum = Float.MAX_VALUE;

    @Override
    public @NonNull ParseResult<Float> parse(@NonNull StringReader input, @Nullable S source) {
        val result = input.readFloat();
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
