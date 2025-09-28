package pers.neige.colonel.context;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import pers.neige.colonel.node.Node;
import pers.neige.colonel.node.ParsedNode;
import pers.neige.colonel.node.impl.ArgumentNode;
import pers.neige.colonel.node.impl.LiteralNode;
import pers.neige.colonel.reader.StringReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 执行上下文
 */
@AllArgsConstructor
@Getter
@SuppressWarnings("unused")
public class Context<S, R> {
    /**
     * 源节点
     */
    private final @NonNull Node<S, R> root;
    /**
     * 文本读取器
     */
    private final @NonNull StringReader input;
    /**
     * 执行源
     */
    private final @Nullable S source;
    /**
     * 当前节点链
     */
    private final @NonNull NodeChain<S, R> nodeChain;
    /**
     * 是否可执行
     */
    private final boolean executable;

    /**
     * @return 节点链长度
     */
    public int size() {
        return nodeChain.size();
    }

    /**
     * 根据索引获取解析后的链节点
     *
     * @param index 节点索引
     * @return 对应索引的解析后的链节点
     */
    public ParsedNode<S, ?, R> get(int index) {
        return nodeChain.get(index);
    }

    /**
     * 根据键获取解析后的链节点
     *
     * @param key 节点键
     * @return 对应键的解析后的链节点
     */
    public ParsedNode<S, ?, R> get(@NonNull String key) {
        return nodeChain.get(key);
    }

    /**
     * 对应ID的节点是否解析成功
     *
     * @param key 节点ID
     * @return 对应ID的节点是否解析成功
     */
    public boolean isArgumentSuccess(@NonNull String key) {
        return nodeChain.isArgumentSuccess(key);
    }

    /**
     * 对应ID的节点解析后参数
     *
     * @param key 节点ID
     * @return 对应ID的节点解析后参数
     */
    public <A> A getArgument(@NonNull String key) {
        return nodeChain.getArgument(key);
    }

    /**
     * 对应ID的节点解析后参数
     *
     * @param key   节点ID
     * @param clazz 参数类型
     * @return 对应ID的节点解析后参数
     */
    public <A> A getArgument(@NonNull String key, Class<A> clazz) {
        return nodeChain.getArgument(key, clazz);
    }

    /**
     * 最后一个节点解析后参数
     *
     * @return 最后一个节点解析后参数
     */
    public <A> A getLastArgument() {
        return nodeChain.getLastArgument();
    }

    /**
     * 最后一个节点解析后参数
     *
     * @param clazz 参数类型
     * @return 最后一个节点解析后参数
     */
    public <A> A getLastArgument(Class<A> clazz) {
        return nodeChain.getLastArgument(clazz);
    }

    /**
     * 最后一个节点对象
     *
     * @return 最后一个节点对象
     */
    public @NonNull Node<S, R> lastNode() {
        val last = nodeChain.last();
        return last == null ? root : last.getNode();
    }

    /**
     * 逻辑执行
     *
     * @return 执行结果
     */
    public @Nullable R execute() {
        return execute(null);
    }

    /**
     * 逻辑执行
     *
     * @param defExecutor 默认执行器
     * @return 执行结果
     */
    public @Nullable R execute(@Nullable Function<Context<S, R>, R> defExecutor) {
        if (executable) {
            val lastNode = size() == 0 ? null : get(size() - 1);
            if (lastNode == null) {
                if (root.getExecutor() != null && input.getString().isEmpty()) {
                    return root.getExecutor().apply(this);
                } else {
                    return defExecutor == null ? null : defExecutor.apply(this);
                }
            }
            val executor = lastNode.getNode().getExecutor();
            return executor != null ? executor.apply(this) : defExecutor == null ? null : defExecutor.apply(this);
        } else {
            val last = nodeChain.last();
            if (last != null && last.getNode() instanceof ArgumentNode) {
                val argument = ((ArgumentNode<S, ?, R>) last.getNode()).getArgument();
                if (argument.getFailExecutor() != null) {
                    return argument.getFailExecutor().apply(this);
                }
            } else if (defExecutor != null) {
                return defExecutor.apply(this);
            }
        }
        return null;
    }

    /**
     * 文本补全
     *
     * @return 文本补全结果
     */
    public @NonNull List<String> tab() {
        val lastNode = lastNode();
        val remaining = input.peekRemaining();
        if (lastNode.getArgumentNode() != null) {
            val taber = lastNode.getArgumentNode().getTaber();
            if (taber != null) {
                return taber.apply(this, remaining);
            }
            return lastNode.getArgumentNode().getArgument().tab(this, remaining);
        }
        Collection<String> rawSuggestions = new ArrayList<>();
        for (LiteralNode<S, R> node : lastNode.getLiteralNodesSet()) {
            rawSuggestions.addAll(node.getTabNames());
        }
        val lowerCaseRemaining = remaining.toLowerCase();
        return rawSuggestions.stream().filter(text -> text.toLowerCase().startsWith(lowerCaseRemaining)).collect(Collectors.toList());
    }
}
