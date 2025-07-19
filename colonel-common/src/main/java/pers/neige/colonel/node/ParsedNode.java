package pers.neige.colonel.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import pers.neige.colonel.arguments.ParseResult;

/**
 * 解析后节点
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("unused")
public class ParsedNode<S, A, R> {
    /**
     * 当前节点
     */
    private final @NonNull Node<S, R> node;
    /**
     * 节点参数解析值
     */
    private final @NonNull ParseResult<A> argument;

    /**
     * 当前节点是否解析成功
     */
    public boolean isSuccess() {
        return argument.isSuccess();
    }
}
