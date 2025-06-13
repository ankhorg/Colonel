package pers.neige.colonel

import pers.neige.colonel.arguments.Argument
import pers.neige.colonel.node.Node
import pers.neige.colonel.node.impl.ArgumentNode
import pers.neige.colonel.node.impl.LiteralNode
import pers.neige.colonel.node.impl.RootNode

fun <S, R> root(
    id: String,
    nodeConfigurer: (RootNode<S, R>.() -> Unit)? = null
): RootNode<S, R> {
    val node = RootNode<S, R>(id)
    nodeConfigurer?.invoke(node)
    return node
}

fun <S, R> literal(
    id: String,
    names: Collection<String?> = setOf(id),
    nodeConfigurer: (LiteralNode<S, R>.() -> Unit)? = null
): LiteralNode<S, R> {
    val node = LiteralNode.literal<S, R>(id, names)
    nodeConfigurer?.invoke(node)
    return node
}

fun <N : Node<S, R>, S, R> N.literal(
    id: String,
    names: Collection<String?> = setOf(id),
    build: Boolean = true,
    nodeConfigurer: (LiteralNode<S, R>.() -> Unit)? = null
): N {
    val node = LiteralNode.literal<S, R>(id, names)
    nodeConfigurer?.invoke(node)
    return Node.then(this, node, build)
}

fun <S, A, R> argument(
    id: String,
    argument: Argument<S, A, R>,
    nodeConfigurer: (ArgumentNode<S, A, R>.() -> Unit)? = null
): ArgumentNode<S, A, R> {
    val node = ArgumentNode.argument(id, argument)
    nodeConfigurer?.invoke(node)
    return node
}

fun <N : Node<S, R>, S, A, R> N.argument(
    id: String,
    argument: Argument<S, A, R>,
    build: Boolean = true,
    nodeConfigurer: (ArgumentNode<S, A, R>.() -> Unit)? = null
): N {
    val node = ArgumentNode.argument(id, argument)
    nodeConfigurer?.invoke(node)
    return Node.then(this, node, build)
}
