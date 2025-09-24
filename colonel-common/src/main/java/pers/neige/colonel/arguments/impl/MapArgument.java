package pers.neige.colonel.arguments.impl;

import lombok.*;
import org.jetbrains.annotations.Nullable;
import pers.neige.colonel.arguments.Argument;
import pers.neige.colonel.arguments.ParseResult;
import pers.neige.colonel.context.Context;
import pers.neige.colonel.context.NodeChain;
import pers.neige.colonel.reader.StringReader;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * 映射参数类型
 */
@Getter
@Builder
@AllArgsConstructor
@SuppressWarnings("unused")
public class MapArgument<S, A, R> extends Argument<S, A, R> {
    /**
     * 禁止返回 null<br>
     * 默认值 {@code true}
     */
    @Builder.Default
    private final boolean nonnull = true;
    /**
     * Map 获取器
     */
    private final @NonNull BiFunction<NodeChain<S, R>, S, Map<String, A>> mapGetter;

    public MapArgument(@NonNull Supplier<Map<String, A>> mapGetter) {
        this.nonnull = true;
        this.mapGetter = (nodeChain, source) -> mapGetter.get();
    }

    public MapArgument(@NonNull BiFunction<NodeChain<S, R>, S, Map<String, A>> mapGetter) {
        this.nonnull = true;
        this.mapGetter = mapGetter;
    }

    @Override
    public @NonNull ParseResult<A> parse(@NonNull NodeChain<S, R> nodeChain, @NonNull StringReader input, @Nullable S source) {
        val start = input.getOffset();
        val key = input.readString();
        if (key.isEmpty()) return new ParseResult<>(null, false);
        val value = mapGetter.apply(nodeChain, source).get(key);
        if (value == null && nonnull) {
            input.setOffset(start);
            return new ParseResult<>(null, false);
        }
        return new ParseResult<>(value, true);
    }

    @Override
    protected @NonNull Collection<String> rawTab(@NonNull Context<S, R> context, @NonNull String remaining) {
        return mapGetter.apply(context.getNodeChain(), context.getSource()).keySet();
    }
}
