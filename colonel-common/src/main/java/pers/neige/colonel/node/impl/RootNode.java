package pers.neige.colonel.node.impl;

import lombok.NonNull;
import pers.neige.colonel.node.Node;

/**
 * 根节点
 */
@SuppressWarnings("unused")
public class RootNode<S, R> extends Node<S, R> {
    public RootNode(
        @NonNull String id
    ) {
        super(id);
    }
}
