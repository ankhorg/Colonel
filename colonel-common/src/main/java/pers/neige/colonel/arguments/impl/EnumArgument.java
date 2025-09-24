package pers.neige.colonel.arguments.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import pers.neige.colonel.arguments.Argument;
import pers.neige.colonel.arguments.ParseResult;
import pers.neige.colonel.context.Context;
import pers.neige.colonel.context.NodeChain;
import pers.neige.colonel.reader.StringReader;

import java.util.*;

/**
 * 枚举参数类型
 */
@Getter
@SuppressWarnings("unused")
public class EnumArgument<S, A extends Enum<A>, R> extends Argument<S, A, R> {
    /**
     * 枚举的 {@link Class} 对象
     */
    private final @NonNull Class<A> enumClass;
    /**
     * 从枚举名称原始集合
     */
    private final @NonNull Set<String> originNames;
    /**
     * 从枚举名称到枚举常量的映射
     */
    private final @NonNull Map<String, A> enumMap;
    /**
     * 忽略枚举名大小写<br>
     * 默认值 {@code true}
     */
    private final boolean ignoreCase;

    /**
     * 构造一个用于解析指定枚举类的参数。
     *
     * @param enumClass  需要处理的枚举的 {@link Class} 对象, 不能为 {@code null}
     * @param ignoreCase 枚举名是否忽略大小写, 默认为 {@code true}
     */
    public EnumArgument(@NonNull Class<A> enumClass, boolean ignoreCase) {
        this.enumClass = enumClass;
        this.originNames = new LinkedHashSet<>();
        this.enumMap = new LinkedHashMap<>();
        for (A enumConstant : enumClass.getEnumConstants()) {
            this.originNames.add(enumConstant.name());
            val name = ignoreCase ? enumConstant.name().toUpperCase(Locale.ROOT) : enumConstant.name();
            this.enumMap.put(name, enumConstant);
        }
        this.ignoreCase = ignoreCase;
    }

    /**
     * 构造一个用于解析指定枚举类的参数
     *
     * @param enumClass 需要处理的枚举的 {@link Class} 对象, 不能为 {@code null}
     */
    public EnumArgument(@NonNull Class<A> enumClass) {
        this(enumClass, true);
    }

    @Override
    public @NonNull ParseResult<A> parse(@NonNull NodeChain<S, R> nodeChain, @NonNull StringReader input, @Nullable S source) {
        val start = input.getOffset();
        val key = this.ignoreCase ? input.readString().toUpperCase(Locale.ROOT) : input.readString();
        if (key.isEmpty()) return new ParseResult<>(null, false);
        A value = enumMap.get(key);
        if (value == null) {
            input.setOffset(start);
            return new ParseResult<>(null, false);
        }
        return new ParseResult<>(value, true);
    }

    @Override
    protected @NonNull Collection<String> rawTab(@NonNull Context<S, R> context, @NonNull String remaining) {
        return originNames;
    }
}
