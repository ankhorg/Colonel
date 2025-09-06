package pers.neige.colonel;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pers.neige.colonel.arguments.Argument;
import pers.neige.colonel.arguments.ParseResult;
import pers.neige.colonel.arguments.impl.StringArgument;
import pers.neige.colonel.context.Context;
import pers.neige.colonel.node.Node;
import pers.neige.colonel.node.impl.ArgumentNode;
import pers.neige.colonel.node.impl.LiteralNode;
import pers.neige.colonel.node.impl.RootNode;
import pers.neige.colonel.reader.StringReader;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Example1Test {
    private static final String RUNTIME_EXCEPTION_TYPE = "运行时错误";
    private static final String INVALID_PARAMETER_EXCEPTION_TYPE = "非法参数错误";
    private static final List<String> VALID_EXCEPTION_TYPES = Arrays.asList(RUNTIME_EXCEPTION_TYPE, INVALID_PARAMETER_EXCEPTION_TYPE);
    private static Node<Void, Void> node;

    @BeforeAll
    public static void setup() {
        node = new RootNode<Void, Void>("root")
                .then(LiteralNode.<Void, Void>literal("打印文本").then(
                        ArgumentNode.<Void, String, Void>argument("文本内容", new StringArgument<>()).setNullExecutor((context) -> {
                            String text = context.getArgument("文本内容");
                            System.out.println(text);
                        })
                ))
                .then(LiteralNode.<Void, Void>literal("抛出错误").then(
                        // 可以通过Argument#setDefaultValue方法直接设置默认值
                        ArgumentNode.argument("错误类型", new StringArgument<Void, Void>().setDefaultValue(RUNTIME_EXCEPTION_TYPE)).setNullExecutor((context) -> {
                            String type = context.getArgument("错误类型");
                            if (RUNTIME_EXCEPTION_TYPE.equals(type)) {
                                throw new RuntimeException();
                            } else if (INVALID_PARAMETER_EXCEPTION_TYPE.equals(type)) {
                                throw new InvalidParameterException();
                            }
                        }).setTaber((context, remaining) -> {
                            return VALID_EXCEPTION_TYPES.stream().filter(type -> type.startsWith(remaining)).collect(Collectors.toList());
                        })
                ))
                .then(LiteralNode.<Void, Void>literal("抛出错误-写法2").then(
                        // Argument#setDefaultValue方法可以传入一个用于获取默认值的Function
                        ArgumentNode.argument("错误类型", new ExceptionTypeArgument<Void, Void>().setDefaultValue((source) -> new ParseResult<>(ExceptionType.RUNTIME_EXCEPTION, true))).setNullExecutor((context) -> {
                            ExceptionType type = context.getArgument("错误类型");
                            if (type == ExceptionType.RUNTIME_EXCEPTION) {
                                throw new RuntimeException();
                            } else if (type == ExceptionType.INVALID_PARAMETER_EXCEPTION) {
                                throw new InvalidParameterException();
                            }
                        })
                ));

    }

    @Test
    public void parse() {
        // 后台将打印"文本测试"
        node.execute(StringReader.of("打印文本 文本测试"), null);

        assertThrows(RuntimeException.class, () -> node.execute(StringReader.of("抛出错误"), null));
        assertThrows(RuntimeException.class, () -> node.execute(StringReader.of("抛出错误 " + RUNTIME_EXCEPTION_TYPE), null));
        assertThrows(InvalidParameterException.class, () -> node.execute(StringReader.of("抛出错误 " + INVALID_PARAMETER_EXCEPTION_TYPE), null));

        assertThrows(RuntimeException.class, () -> node.execute(StringReader.of("抛出错误-写法2"), null));
        assertThrows(RuntimeException.class, () -> node.execute(StringReader.of("抛出错误-写法2 " + RUNTIME_EXCEPTION_TYPE), null));
        assertThrows(InvalidParameterException.class, () -> node.execute(StringReader.of("抛出错误-写法2 " + INVALID_PARAMETER_EXCEPTION_TYPE), null));

        // 后台将打印"不存在名为 神秘的错误 的错误类型"
        node.execute(StringReader.of("抛出错误-写法2 神秘的错误"), null);

        assertEquals(
                Arrays.asList("打印文本", "抛出错误", "抛出错误-写法2"),
                node.tab(StringReader.of(""), null)
        );
        assertEquals(
                Collections.singletonList("打印文本"),
                node.tab(StringReader.of("打印"), null)
        );
        assertEquals(
                Arrays.asList("抛出错误", "抛出错误-写法2"),
                node.tab(StringReader.of("抛出错误"), null)
        );
        assertEquals(
                VALID_EXCEPTION_TYPES,
                node.tab(StringReader.of("抛出错误 "), null)
        );
        assertEquals(
                Collections.singletonList(RUNTIME_EXCEPTION_TYPE),
                node.tab(StringReader.of("抛出错误 运行时"), null)
        );
        assertEquals(
                VALID_EXCEPTION_TYPES,
                node.tab(StringReader.of("抛出错误-写法2 "), null)
        );
        assertEquals(
                Collections.singletonList(RUNTIME_EXCEPTION_TYPE),
                node.tab(StringReader.of("抛出错误-写法2 运行时"), null)
        );
    }

    @Getter
    public enum ExceptionType {
        RUNTIME_EXCEPTION(RUNTIME_EXCEPTION_TYPE),
        INVALID_PARAMETER_EXCEPTION(INVALID_PARAMETER_EXCEPTION_TYPE);

        private final @NonNull String name;

        ExceptionType(@NonNull String name) {
            this.name = name;
        }
    }

    @Getter
    public static class ExceptionTypeArgument<S, R> extends Argument<S, ExceptionType, R> {
        public ExceptionTypeArgument() {
            setNullFailExecutor((context) -> {
                val currentText = context.getInput().readString();
                System.out.println("不存在名为 " + currentText + " 的错误类型");
            });
        }

        @Override
        @NonNull
        public ParseResult<ExceptionType> parse(@NonNull StringReader input, @Nullable S source) {
            // 获取当前的字符读取偏移
            val start = input.getOffset();
            // 尝试读取一段文本(此时偏移已经移动到这段文本后)
            val name = input.readString();
            // 检测是否有符合的结果
            ExceptionType result = null;
            for (val type : ExceptionType.values()) {
                if (!type.getName().equals(name)) continue;
                result = type;
                break;
            }
            // 如果没有结果
            if (result == null) {
                // 回滚偏移
                input.setOffset(start);
                // 返回失败的解析结果
                return new ParseResult<>(null, false);
            }
            // 有结果, 则返回成功的解析结果
            return new ParseResult<>(result, true);
        }

        @Override
        public @NonNull List<String> rawTab(@NonNull Context<S, R> context, @NonNull String remaining) {
            return VALID_EXCEPTION_TYPES;
        }
    }
}
