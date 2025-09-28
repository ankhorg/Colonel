package pers.neige.colonel.context;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import pers.neige.colonel.node.ParsedNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 节点链
 */
@Getter
@SuppressWarnings("unused")
public class NodeChain<S, R> {
    /**
     * 依据顺序存储的解析后节点对象
     */
    private final @NonNull List<ParsedNode<S, ?, R>> nodes = new ArrayList<>();
    /**
     * 依据ID存储的解析后节点对象
     */
    private final @NonNull LinkedHashMap<String, ParsedNode<S, ?, R>> nodeMap = new LinkedHashMap<>();

    /**
     * 添加解析后节点对象
     *
     * @param node 解析后节点对象
     */
    public void add(@NonNull ParsedNode<S, ?, R> node) {
        nodes.add(node);
        nodeMap.put(node.getNode().getId(), node);
    }

    /**
     * 获取节点链长度
     *
     * @return 节点链长度
     */
    public int size() {
        return nodes.size();
    }

    /**
     * 根据索引获取解析后节点对象
     *
     * @param index 节点索引
     * @return 解析后节点对象
     */
    public @Nullable ParsedNode<S, ?, R> get(int index) {
        return index >= 0 && index <= (nodes.size() - 1) ? nodes.get(index) : null;
    }

    /**
     * 根据ID获取解析后节点对象
     *
     * @param key 节点ID
     * @return 解析后节点对象
     */
    public @Nullable ParsedNode<S, ?, R> get(@NonNull String key) {
        return nodeMap.get(key);
    }

    /**
     * 最后一个解析后节点对象
     *
     * @return 最后一个解析后节点对象
     */
    public @Nullable ParsedNode<S, ?, R> last() {
        return nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);
    }

    /**
     * 节点链是否为空
     *
     * @return 节点链是否为空
     */
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    /**
     * 对应ID的节点是否解析成功
     *
     * @param key 节点ID
     * @return 对应ID的节点是否解析成功
     */
    public boolean isArgumentSuccess(@NonNull String key) {
        val node = get(key);
        if (node == null) return false;
        return node.getArgument().isSuccess();
    }

    /**
     * 对应ID的节点解析后参数
     *
     * @param key 节点ID
     * @return 对应ID的节点解析后参数
     */
    @SuppressWarnings("unchecked")
    public <A> A getArgument(@NonNull String key) {
        val value = getRawArgument(key);
        return (A) value;
    }

    /**
     * 对应ID的节点解析后参数
     *
     * @param key   节点ID
     * @param clazz 参数类型
     * @return 对应ID的节点解析后参数
     */
    public <A> A getArgument(@NonNull String key, Class<A> clazz) {
        val value = getRawArgument(key);
        return clazz.isInstance(value) ? clazz.cast(value) : null;
    }

    /**
     * 对应ID的节点解析后参数
     *
     * @param key 节点ID
     * @return 对应ID的节点解析后参数
     */
    private Object getRawArgument(@NonNull String key) {
        val node = get(key);
        if (node == null) return null;
        return node.getArgument().getResult();
    }

    /**
     * 最后一个节点解析后参数
     *
     * @return 最后一个节点解析后参数
     */
    @SuppressWarnings("unchecked")
    public <A> A getLastArgument() {
        val value = getRawLastArgument();
        return (A) value;
    }

    /**
     * 最后一个节点解析后参数
     *
     * @param clazz 参数类型
     * @return 最后一个节点解析后参数
     */
    public <A> A getLastArgument(Class<A> clazz) {
        val value = getRawLastArgument();
        return clazz.isInstance(value) ? clazz.cast(value) : null;
    }

    /**
     * 最后一个节点解析后参数
     *
     * @return 最后一个节点解析后参数
     */
    private Object getRawLastArgument() {
        val node = last();
        if (node == null) return null;
        return node.getArgument().getResult();
    }
}
