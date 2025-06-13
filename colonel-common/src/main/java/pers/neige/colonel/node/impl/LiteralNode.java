package pers.neige.colonel.node.impl;

import lombok.NonNull;
import pers.neige.colonel.node.Node;

import java.util.Collection;

/**
 * 字面量节点
 */
@SuppressWarnings("unused")
public class LiteralNode<S, R> extends Node<S, R> {
    private LiteralNode(
            @NonNull String id
    ) {
        super(id);
    }

    private LiteralNode(
            @NonNull String id,
            @NonNull String... names
    ) {
        super(id, names);
    }

    private LiteralNode(
            @NonNull String id,
            @NonNull Collection<String> names
    ) {
        super(id, names);
    }

    public static <S, R> LiteralNode<S, R> literal(
            @NonNull String id
    ) {
        return new LiteralNode<>(id);
    }

    public static <S, R> LiteralNode<S, R> literal(
            @NonNull String id,
            @NonNull String... names
    ) {
        return new LiteralNode<>(id, names);
    }

    public static <S, R> LiteralNode<S, R> literal(
            @NonNull String id,
            @NonNull Collection<String> names
    ) {
        return new LiteralNode<>(id, names);
    }
}
