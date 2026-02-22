package pers.neige.colonel.node;

import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import org.neosearch.stringsearcher.StringSearcher;
import pers.neige.colonel.arguments.ParseResult;
import pers.neige.colonel.context.Context;
import pers.neige.colonel.context.NodeChain;
import pers.neige.colonel.node.impl.ArgumentNode;
import pers.neige.colonel.node.impl.LiteralNode;
import pers.neige.colonel.reader.StringReader;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 节点
 */
@SuppressWarnings("unused")
@ToString(of = {"id", "tabNames"})
public abstract class Node<S, R> {
    /**
     * 所有子节点
     */
    protected final @NonNull Map<String, Node<S, R>> childNodes = new LinkedHashMap<>();
    /**
     * 所有 LiteralNode 类型子节点
     */
    protected final @NonNull Map<String, LiteralNode<S, ?, R>> literalNodes = new LinkedHashMap<>();
    /**
     * 所有 LiteralNode 类型子节点
     */
    protected final @NonNull Set<LiteralNode<S, ?, R>> literalNodesSet = new LinkedHashSet<>();
    /**
     * 标识符
     */
    @Getter
    protected final @NonNull String id;
    /**
     * 解析识别名, 必定全部为小写字符
     */
    @Getter
    protected final @NonNull Set<String> names;
    /**
     * 用于补全的显示名, 会有大写字符
     */
    @Getter
    protected final @NonNull Set<String> tabNames;
    /**
     * LiteralNode 类型子节点的小写识别名中出现过的字符
     */
    protected final @NonNull Set<Character> literalChars = new HashSet<>();
    /**
     * 父节点
     */
    @Getter
    protected @Nullable Node<S, R> parentNode = null;
    /**
     * LiteralNode 识别名搜索器，仅在识别名中包含传入的 StringReader 分隔符时启用
     */
    protected @Nullable StringSearcher<LiteralNode<S, ?, R>> literalNodesSearcher = null;
    /**
     * LiteralNode 类型子节点的最大长度
     */
    protected int literalNodesMaxLength = 0;
    /**
     * ArgumentNode 类型子节点
     */
    @Getter
    protected @Nullable ArgumentNode<S, ?, R> argumentNode = null;
    /**
     * 执行器
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    protected @Nullable Function<Context<S, R>, R> executor;

    protected Node(
        @NonNull String id
    ) {
        this(id, id);
    }

    protected Node(
        @NonNull String id,
        @NonNull String... names
    ) {
        this(id, Arrays.asList(names));
    }

    protected Node(
        @NonNull String id,
        @NonNull Collection<String> names
    ) {
        this(id, names, null);
    }

    protected Node(
        @NonNull String id,
        @NonNull Collection<String> names,
        @Nullable Function<Context<S, R>, R> executor
    ) {
        this.id = id;
        this.names = names.stream().map(name -> name.toLowerCase(Locale.ENGLISH)).collect(Collectors.toSet());
        this.tabNames = new HashSet<>(names);
        this.executor = executor;
    }

    /**
     * 在父节点后接续其他子节点<br>
     * 同一个 Node 后只能跟随多个 LiteralNode 或 一个 ArgumentNode，不可混合构建
     *
     * @param parentNode 父节点
     * @param childNode  子节点
     * @return {@code parentNode}
     */
    public static <N extends Node<S, R>, S, R> @NonNull N then(@NonNull N parentNode, @NonNull Node<S, R> childNode) {
        return Node.then(parentNode, childNode, true);
    }

    /**
     * 在父节点后接续其他子节点<br>
     * 同一个 Node 后只能跟随多个 LiteralNode 或 一个 ArgumentNode，不可混合构建
     *
     * @param parentNode 父节点
     * @param childNode  子节点
     * @param build      对于 LiteralNode，是否立即构建字符搜索器
     * @return {@code parentNode}
     */
    public static <N extends Node<S, R>, S, R> @NonNull N then(@NonNull N parentNode, @NonNull Node<S, R> childNode, boolean build) {
        if (childNode instanceof LiteralNode) {
            if (parentNode.argumentNode != null) {
                throw new InvalidParameterException("Node 后只能跟随多个 LiteralNode 或 一个 ArgumentNode");
            }
            for (String name : childNode.getNames()) {
                parentNode.literalNodes.put(name, (LiteralNode<S, ?, R>) childNode);
                parentNode.literalNodesSet.add((LiteralNode<S, ?, R>) childNode);
                parentNode.literalNodesMaxLength = Math.max(parentNode.literalNodesMaxLength, name.length());
                name.chars().forEach(c -> parentNode.literalChars.add((char) c));
            }
            if (build) parentNode.buildLiteralSearcher();
        } else if (childNode instanceof ArgumentNode) {
            if (!parentNode.literalNodes.isEmpty()) {
                throw new InvalidParameterException("Node 后只能跟随多个 LiteralNode 或 一个 ArgumentNode");
            }
            parentNode.argumentNode = (ArgumentNode<S, ?, R>) childNode;
        } else {
            throw new InvalidParameterException("Node 后只能跟随多个 LiteralNode 或 一个 ArgumentNode");
        }
        parentNode.childNodes.put(childNode.getId(), childNode);
        childNode.parentNode = parentNode;
        return parentNode;
    }

    /**
     * 所有子节点
     */
    public @NonNull Map<String, Node<S, R>> getChildNodes() {
        return Collections.unmodifiableMap(childNodes);
    }

    /**
     * 所有 LiteralNode 类型子节点
     */
    public @NonNull Map<String, LiteralNode<S, ?, R>> getLiteralNodes() {
        return Collections.unmodifiableMap(literalNodes);
    }

    /**
     * 所有 LiteralNode 类型子节点
     */
    public @NonNull Set<LiteralNode<S, ?, R>> getLiteralNodesSet() {
        return Collections.unmodifiableSet(literalNodesSet);
    }

    /**
     * 设置无返回值的执行器
     */
    public Node<S, R> setNullExecutor(@Nullable Consumer<Context<S, R>> executor) {
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

    /**
     * 在当前节点后接续其他子节点<br>
     * 同一个 Node 后只能跟随多个 LiteralNode 或 一个 ArgumentNode，不可混合构建
     *
     * @param childNode 子节点
     * @return {@code this}
     */
    public Node<S, R> then(@NonNull Node<S, R> childNode) {
        return Node.then(this, childNode, true);
    }

    /**
     * 在当前节点后接续其他子节点<br>
     * 同一个 Node 后只能跟随多个 LiteralNode 或 一个 ArgumentNode，不可混合构建
     *
     * @param childNode 子节点
     * @param build     对于 LiteralNode, 是否立即构建字符搜索器
     * @return {@code this}
     */
    public @NonNull Node<S, R> then(@NonNull Node<S, R> childNode, boolean build) {
        return Node.then(this, childNode, build);
    }

    /**
     * 构建字符搜索器
     */
    public void buildLiteralSearcher() {
        val builder = StringSearcher.<LiteralNode<S, ?, R>>builderWithPayload().ignoreOverlaps();
        literalNodes.forEach(builder::addSearchString);
        literalNodesSearcher = builder.build();
    }

    /**
     * 解析获取下一个节点
     *
     * @param input 输入的文本读取器
     * @return 下一个节点
     */
    public @Nullable Node<S, R> getNextNode(@NonNull StringReader input) {
        if (argumentNode != null) {
            return argumentNode;
        }
        val literalMatchResult = matchLiteralNode(input);
        return literalMatchResult == null ? null : literalMatchResult.getNode();
    }

    /**
     * 解析获取下一个字面量节点
     *
     * @param input 输入的文本读取器
     * @return 下一个字面量节点
     */
    private @Nullable ParsedNode<S, ?, R> matchLiteralNode(@NonNull StringReader input) {
        if (!input.canRead()) return null;
        if (literalNodesSearcher != null && input.containsSeparator(literalChars)) {
            val textToParse = input.peek(literalNodesMaxLength).toLowerCase(Locale.ENGLISH);
            val emit = literalNodesSearcher.firstMatch(textToParse);
            if (emit == null) return null;
            val text = emit.getSearchString();
            if (!textToParse.startsWith(text)) return null;
            input.skip(text.length());
            if (!input.canRead() || input.isSeparator(input.current())) {
                val literal = emit.getPayload();
                return literal == null ? null : new ParsedNode<>(literal, new ParseResult<>(literal.getKeyToPayload().get(text), true));
            } else {
                input.skip(-text.length());
                return null;
            }
        } else {
            val start = input.getOffset();
            val text = input.readLowerCaseString();
            val literal = literalNodes.get(text);
            if (literal == null) {
                input.setOffset(start);
                return null;
            }
            return new ParsedNode<>(literal, new ParseResult<>(literal.getKeyToPayload().get(text), true));
        }
    }

    /**
     * 解析获取下一个节点
     *
     * @param input 输入的文本读取器
     * @return 下一个节点
     */
    private @Nullable ParsedNode<S, ?, R> getParsedNextNode(@NonNull NodeChain<S, R> nodeChain, @NonNull StringReader input, @Nullable S source) {
        if (argumentNode != null) {
            if (!input.canRead()) {
                if (argumentNode.getArgument().hasDefaultValue()) {
                    return new ParsedNode<>(argumentNode, argumentNode.getArgument().getDefaultValue(source));
                } else {
                    return null;
                }
            } else {
                return new ParsedNode<>(argumentNode, argumentNode.getArgument().parse(nodeChain, input, source));
            }
        } else {
            return matchLiteralNode(input);
        }
    }

    /**
     * 以逻辑执行为目的, 进行文本解析
     *
     * @param input  输入的文本读取器
     * @param source 执行源
     * @return 解析上下文，可用于逻辑执行
     */
    public @NonNull Context<S, R> parseExecuteContext(@NonNull StringReader input, @Nullable S source) {
        val nodeChain = new NodeChain<S, R>();
        Node<S, R> current = this;
        input.skipSeparator();
        while (true) {
            val next = current.getParsedNextNode(nodeChain, input, source);
            if (next == null) {
                break;
            }
            current = next.getNode();
            if (current instanceof LiteralNode) {
                nodeChain.add(next);
            } else if (current instanceof ArgumentNode) {
                nodeChain.add(next);
                if (!next.isSuccess()) {
                    return new Context<>(this, input, source, nodeChain, false);
                }
            }
            input.skipSeparator();
        }
        return new Context<>(this, input, source, nodeChain, true);
    }

    /**
     * 逻辑执行
     *
     * @param input  输入的文本读取器
     * @param source 执行源
     * @return 执行结果
     */
    public @Nullable R execute(@NonNull StringReader input, @Nullable S source) {
        return execute(input, source, null);
    }

    /**
     * 逻辑执行
     *
     * @param input       输入的文本读取器
     * @param source      执行源
     * @param defExecutor 默认执行器
     * @return 执行结果
     */
    public @Nullable R execute(@NonNull StringReader input, @Nullable S source, @Nullable Function<Context<S, R>, R> defExecutor) {
        return parseExecuteContext(input, source).execute(defExecutor);
    }

    /**
     * 以文本补全为目的, 进行文本解析
     *
     * @param input  输入的文本读取器
     * @param source 执行源
     * @return 解析上下文，可用于文本补全
     */
    public @Nullable Context<S, R> parseTabContext(@NonNull StringReader input, @Nullable S source) {
        val nodeChain = new NodeChain<S, R>();
        Node<S, R> current = this;
        if (input.canRead()) {
            boolean skipped;
            while (true) {
                skipped = input.skipSeparator();
                if (!input.canRead()) {
                    if (skipped) {
                        break;
                    } else {
                        return null;
                    }
                }
                val start = input.getOffset();
                if (current.argumentNode != null) {
                    val parseResult = current.argumentNode.getArgument().parse(nodeChain, input, source);
                    if (!parseResult.isSuccess()) {
                        break;
                    }
                    if (!input.canRead() || !input.isSeparator(input.current())) {
                        input.setOffset(start);
                        break;
                    }
                    current = current.argumentNode;
                    nodeChain.add(new ParsedNode<>(current, parseResult));
                } else {
                    val next = current.matchLiteralNode(input);
                    if (next == null) {
                        break;
                    }
                    if (!input.canRead() || !input.isSeparator(input.current())) {
                        input.setOffset(start);
                        break;
                    }
                    current = next.getNode();
                    nodeChain.add(next);
                }
            }
        }
        return new Context<>(this, input, source, nodeChain, false);
    }

    /**
     * 文本补全
     *
     * @param input  输入的文本读取器
     * @param source 执行源
     * @return 文本补全结果
     */
    public @NonNull List<String> tab(@NonNull StringReader input, @Nullable S source) {
        val context = parseTabContext(input, source);
        if (context == null) {
            return Collections.emptyList();
        }
        return context.tab();
    }
}
