package pers.neige.colonel.arguments.impl;

import lombok.*;
import org.jetbrains.annotations.Nullable;
import pers.neige.colonel.arguments.Argument;
import pers.neige.colonel.arguments.ParseResult;
import pers.neige.colonel.context.NodeChain;
import pers.neige.colonel.reader.StringReader;

/**
 * 长整型参数类型
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class LongArgument<S, R> extends Argument<S, Long, R> {
    /**
     * 参数最小值<br>
     * 默认值 {@code Long.MIN_VALUE}
     */
    @Builder.Default
    private final long minimum = Long.MIN_VALUE;
    /**
     * 参数最大值<br>
     * 默认值 {@code Long.MAX_VALUE}
     */
    @Builder.Default
    private final long maximum = Long.MAX_VALUE;

    @Override
    public @NonNull ParseResult<Long> parse(@NonNull NodeChain<S, R> nodeChain, @NonNull StringReader input, @Nullable S source) {
        val result = input.readLong();
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
