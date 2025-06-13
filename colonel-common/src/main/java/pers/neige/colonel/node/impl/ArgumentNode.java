package pers.neige.colonel.node.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import pers.neige.colonel.arguments.Argument;
import pers.neige.colonel.context.Context;
import pers.neige.colonel.node.Node;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 参数节点
 */
@Getter
@Setter
@Accessors(chain = true)
@SuppressWarnings("unused")
public class ArgumentNode<S, A, R> extends Node<S, R> {
    /**
     * 类型参数
     */
    protected final @NonNull Argument<S, A, R> argument;
    /**
     * 自定义补全器, 可用于覆盖补全逻辑
     */
    protected @Nullable BiFunction<Context<S, R>, String, List<String>> taber = null;

    private ArgumentNode(
            @NonNull String id,
            @NonNull Argument<S, A, R> argument
    ) {
        super(id);
        this.argument = argument;
    }

    public static <S, A, R> ArgumentNode<S, A, R> argument(
            @NonNull String id,
            @NonNull Argument<S, A, R> argument
    ) {
        return new ArgumentNode<>(id, argument);
    }

    /**
     * 设置执行器
     */
    public ArgumentNode<S, A, R> setExecutor(@Nullable Function<Context<S, R>, R> executor) {
        this.executor = executor;
        return this;
    }

    /**
     * 设置无返回值的执行器
     */
    public ArgumentNode<S, A, R> setNullExecutor(@Nullable Consumer<Context<S, R>> executor) {
        if (executor == null) {
            this.executor = null;
        } else {
            this.executor = context -> {
                executor.accept(context);
                return null;
            };
        }
        return this;
    }
}
