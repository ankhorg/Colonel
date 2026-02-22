package pers.neige.colonel.node.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import pers.neige.colonel.node.Node;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * 字面量节点
 */
@SuppressWarnings("unused")
public class LiteralNode<S, A, R> extends Node<S, R> {
    /**
     * 字面量到参数值的映射, key 必须为小写
     */
    @Getter
    private final @NonNull Map<String, A> keyToPayload;

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
        return literal(id, id);
    }

    public static <S, R> LiteralNode<S, String, R> literal(
        @NonNull String id,
        @NonNull String... names
    ) {
        return literal(id, Arrays.asList(names));
    }

    public static <S, R> LiteralNode<S, String, R> literal(
        @NonNull String id,
        @NonNull Collection<String> names
    ) {
        val realNames = new HashSet<>(names);
        realNames.add(id);
        val keyToPayload = new HashMap<String, String>();
        for (val name : realNames) {
            val pre = keyToPayload.put(name.toLowerCase(Locale.ENGLISH), name);
            if (pre == null) continue;
            throw new InvalidParameterException("LiteralNode names are case insensitive, do not enter duplicate recognition names, pre lowercased name is: " + name.toLowerCase(Locale.ENGLISH) + ", current name is: " + name);
        }
        return new LiteralNode<>(id, keyToPayload, realNames);
    }

    public static <S, A, R> LiteralNode<S, A, R> literal(
        @NonNull String id,
        @NonNull Map<String, A> keyToPayload
    ) {
        val names = new ArrayList<>(keyToPayload.keySet());
        val lowercasedKeyToPayload = new HashMap<String, A>();
        keyToPayload.forEach((key, value) -> {
            val pre = lowercasedKeyToPayload.put(key.toLowerCase(Locale.ENGLISH), value);
            if (pre == null) return;
            throw new InvalidParameterException("LiteralNode names are case insensitive, do not enter duplicate recognition names, pre lowercased name is: " + key.toLowerCase(Locale.ENGLISH) + ", current name is: " + key);
        });
        return new LiteralNode<>(id, lowercasedKeyToPayload, names);
    }

    private void nullCheck() {
        for (val entry : keyToPayload.entrySet()) {
            val key = entry.getKey();
            if (key == null) {
                throw new NullPointerException("LiteralNode keyToPayload must not have null key!");
            }
            val lowercasedKeyToPayload = key.toLowerCase(Locale.ENGLISH);
            if (!lowercasedKeyToPayload.equals(key)) {
                throw new NullPointerException("LiteralNode keyToPayload key must be lowercased!");
            }
            if (entry.getValue() == null) {
                throw new NullPointerException("LiteralNode keyToPayload must not have null value, but value associate with " + entry.getKey() + " is null!");
            }
        }
    }
}
