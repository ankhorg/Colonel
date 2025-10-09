package pers.neige.colonel.node.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import pers.neige.colonel.node.Node;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 字面量节点
 */
@SuppressWarnings("unused")
public class LiteralNode<S, A, R> extends Node<S, R> {
    @Getter
    private final @NonNull Map<String, A> keyToPayload;

    private LiteralNode(
            @NonNull String id,
            @NonNull Map<String, A> keyToPayload,
            @NonNull String... names
    ) {
        super(id, names);
        this.keyToPayload = keyToPayload;
        nullCheck();
    }

    private LiteralNode(
            @NonNull String id,
            @NonNull Map<String, A> keyToPayload,
            @NonNull Collection<String> names
    ) {
        super(id, names);
        this.keyToPayload = keyToPayload;
        nullCheck();
    }

    private LiteralNode(
            @NonNull String id,
            @NonNull Map<String, A> keyToPayload
    ) {
        super(id, keyToPayload.keySet());
        this.keyToPayload = keyToPayload;
        nullCheck();
    }

    public static <S, R> LiteralNode<S, String, R> literal(
            @NonNull String id
    ) {
        val keyToPayload = new HashMap<String, String>();
        keyToPayload.put(id.toLowerCase(Locale.ENGLISH), id);
        return new LiteralNode<>(id, keyToPayload);
    }

    public static <S, R> LiteralNode<S, String, R> literal(
            @NonNull String id,
            @NonNull String... names
    ) {
        val keyToPayload = new HashMap<String, String>();
        for (String name : names) {
            keyToPayload.put(name.toLowerCase(Locale.ENGLISH), name);
        }
        return new LiteralNode<>(id, keyToPayload, names);
    }

    public static <S, R> LiteralNode<S, String, R> literal(
            @NonNull String id,
            @NonNull Collection<String> names
    ) {
        val keyToPayload = new HashMap<String, String>();
        for (String name : names) {
            keyToPayload.put(name.toLowerCase(Locale.ENGLISH), name);
        }
        return new LiteralNode<>(id, keyToPayload, names);
    }

    public static <S, A, R> LiteralNode<S, A, R> literal(
            @NonNull String id,
            @NonNull Map<String, A> keyToPayload
    ) {
        return new LiteralNode<>(id, keyToPayload);
    }

    private void nullCheck() {
        for (val entry : keyToPayload.entrySet()) {
            if (entry.getKey() == null) {
                throw new NullPointerException("LiteralNode keyToPayload must not have null value, but value associate with " + entry.getKey() + " is null!");
            }
            if (entry.getValue() == null) {
                throw new NullPointerException("LiteralNode keyToPayload must not have null value, but value associate with " + entry.getKey() + " is null!");
            }
        }
    }
}
