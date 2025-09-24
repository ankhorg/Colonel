package pers.neige.colonel.arguments;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import pers.neige.colonel.context.Context;
import pers.neige.colonel.context.NodeChain;
import pers.neige.colonel.reader.StringReader;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 参数类型
 */
@SuppressWarnings("unused")
public abstract class Argument<S, A, R> {
    protected @Nullable A defaultValue;
    protected @Nullable Function<S, ParseResult<A>> defaultValueGetter;
    protected boolean hasDefaultValue;
    /**
     * 参数解析失败时的执行回调
     */
    @Getter
    protected @Nullable Function<Context<S, R>, R> failExecutor;

    /**
     * 设置默认值
     *
     * @param defaultValue 默认值
     * @return {@code this}
     */
    public @NonNull Argument<S, A, R> setDefaultValue(@Nullable A defaultValue) {
        this.defaultValue = defaultValue;
        this.hasDefaultValue = true;
        return this;
    }

    /**
     * 设置默认值获取器
     *
     * @param defaultValueGetter 默认值获取器
     * @return {@code this}
     */
    public @NonNull Argument<S, A, R> setDefaultValue(@NonNull Function<S, ParseResult<A>> defaultValueGetter) {
        this.defaultValueGetter = defaultValueGetter;
        this.hasDefaultValue = true;
        return this;
    }

    /**
     * 设置参数解析失败时的执行回调
     *
     * @param failExecutor 参数解析失败时的执行回调
     * @return {@code this}
     */
    public @NonNull Argument<S, A, R> setFailExecutor(@Nullable Function<Context<S, R>, R> failExecutor) {
        this.failExecutor = failExecutor;
        return this;
    }

    /**
     * 设置无返回值的参数解析失败时的执行回调
     *
     * @param failExecutor 无返回值的参数解析失败时的执行回调
     * @return {@code this}
     */
    public @NonNull Argument<S, A, R> setNullFailExecutor(@Nullable Consumer<Context<S, R>> failExecutor) {
        this.failExecutor = failExecutor == null ? null : (context) -> {
            failExecutor.accept(context);
            return null;
        };
        return this;
    }

    /**
     * 是否存在默认值
     *
     * @return 是否存在默认值
     */
    public boolean hasDefaultValue() {
        return hasDefaultValue;
    }

    /**
     * 获取默认值
     *
     * @param source 执行源
     * @return 默认值
     */
    public @NonNull ParseResult<A> getDefaultValue(@Nullable S source) {
        if (this.defaultValueGetter != null) {
            return this.defaultValueGetter.apply(source);
        }
        return new ParseResult<>(defaultValue, this.hasDefaultValue);
    }

    /**
     * 参数解析方法，解析失败时 ParseResult#success 应为 false, 且 StringReader#offset 应保持读取前状态
     *
     * @param nodeChain 此前已解析的节点链
     * @param input     输入的文本
     * @param source    执行源
     */
    public abstract @NonNull ParseResult<A> parse(@NonNull NodeChain<S, R> nodeChain, @NonNull StringReader input, @Nullable S source);

    /**
     * 根据剩余文本进行参数补全
     *
     * @param context   执行上下文
     * @param remaining 剩余文本
     * @return 补全文本
     */
    public @NonNull List<String> tab(@NonNull Context<S, R> context, @NonNull String remaining) {
        val lowerCaseRemaining = remaining.toLowerCase();
        return rawTab(context, remaining).stream().filter(text -> text.toLowerCase().startsWith(lowerCaseRemaining)).collect(Collectors.toList());
    }

    /**
     * 根据剩余文本进行参数补全, 可以出现不以剩余文本开头的补全文本
     *
     * @param context   执行上下文
     * @param remaining 剩余文本
     * @return 补全文本
     */
    protected @NonNull Collection<String> rawTab(@NonNull Context<S, R> context, @NonNull String remaining) {
        val lastNode = context.lastNode().getArgumentNode();
        return lastNode == null ? Collections.emptyList() : Collections.singletonList(lastNode.getId());
    }
}
