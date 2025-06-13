# Colonel

`Colonel` 是一个文本解析器，可以构建一个节点树，适配指令执行、指令补全、变量解析、变量补全等任务。

`colonel-common` 模块为主逻辑模块。

`colonel-kotlin` 模块根据我的喜好做出了 `Kotlin` 用法拓展。

`colonel-bukkit` 模块添加了一些可用于 `Bukkit` 插件的参数类型。

# 基本逻辑

## 实现示例

以这两条普通的指令为例（必填参数由 `[]` 包裹, 选填参数由 `()` 包裹）：

- `打印文本 [文本内容]`
- `抛出错误 (运行时错误/非法参数错误)`

测试类如下：

```java
package pers.neige.colonel;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;
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
        node.execute(new StringReader("打印文本 文本测试"), null);

        assertThrows(RuntimeException.class, () -> node.execute(new StringReader("抛出错误"), null));
        assertThrows(RuntimeException.class, () -> node.execute(new StringReader("抛出错误 " + RUNTIME_EXCEPTION_TYPE), null));
        assertThrows(InvalidParameterException.class, () -> node.execute(new StringReader("抛出错误 " + INVALID_PARAMETER_EXCEPTION_TYPE), null));

        assertThrows(RuntimeException.class, () -> node.execute(new StringReader("抛出错误-写法2"), null));
        assertThrows(RuntimeException.class, () -> node.execute(new StringReader("抛出错误-写法2 " + RUNTIME_EXCEPTION_TYPE), null));
        assertThrows(InvalidParameterException.class, () -> node.execute(new StringReader("抛出错误-写法2 " + INVALID_PARAMETER_EXCEPTION_TYPE), null));

        // 后台将打印"不存在名为 神秘的错误 的错误类型"
        node.execute(new StringReader("抛出错误-写法2 神秘的错误"), null);
      
        assertEquals(
                Arrays.asList("打印文本", "抛出错误", "抛出错误-写法2"),
                node.tab(new StringReader(""), null)
        );
        assertEquals(
                Collections.singletonList("打印文本"),
                node.tab(new StringReader("打印"), null)
        );
        assertEquals(
                Arrays.asList("抛出错误", "抛出错误-写法2"),
                node.tab(new StringReader("抛出错误"), null)
        );
        assertEquals(
                VALID_EXCEPTION_TYPES,
                node.tab(new StringReader("抛出错误 "), null)
        );
        assertEquals(
                Collections.singletonList(RUNTIME_EXCEPTION_TYPE),
                node.tab(new StringReader("抛出错误 运行时"), null)
        );
        assertEquals(
                VALID_EXCEPTION_TYPES,
                node.tab(new StringReader("抛出错误-写法2 "), null)
        );
        assertEquals(
                Collections.singletonList(RUNTIME_EXCEPTION_TYPE),
                node.tab(new StringReader("抛出错误-写法2 运行时"), null)
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
        public @NonNull List<String> rawTab(@NonNull Context<S, ?> context, @NonNull String remaining) {
            return VALID_EXCEPTION_TYPES;
        }
    }
}
```

## Node<S, R>

`Node<S, R>` 中，`S` 代表 `Source`，即执行源，表示执行的执行者，或者变量的解析对象，`R` 代表 `Return`，即执行的结果。

以 `Bukkit` 插件常用场景为例：

- 对于服务器指令：
  - `S` 应该为 `@NonNull CommandSender`，可能是后台（`ConsoleSender`），也可能是玩家（`Player`）。
  - `R` 代表 `@NonNull Boolean`，代表指令执行结果，也可以是 java 中的 `Void` 或 kotlin 中的 `Unit`，因为指令的返回值其实没什么用，我喜欢统统返回 `true`。
- 对于 `PlaceholderAPI` 变量：
  - `S` 应该为 `@Nullable OfflinePlayer`，可能是 `null`，可能是离线玩家（`OfflinePlayer`），也可能是在线玩家（`Player`）。
  - `R` 代表 `@Nullable String`，代表变量解析结果。

对于此次示例场景，执行源和执行结果都是不必要的，因此我将二者指定为 `Void` 类型，以 `null` 作为执行源，并返回 `null` 作为执行结果。

## ArgumentNode<S, A, R>

`ArgumentNode<S, A, R>` 和 `Argument<S, A, R>` 中，`A` 代表 `Argument`，即参数类型。

对于示例中的 `抛出错误` 指令，我给出了两种参数实现方法：

- 通过 `StringArgument` 获取 `String` 类型的参数，在指令执行器中进行类型解析，然后通过 `setTaber` 方法实现补全逻辑
- 自己实现一个 `Argument<S, ExceptionType, R>`。

## Argument#tab 和 Argument#rawTab

其中 `Argument` 类存在两个补全方法：`tab` 和 `rawTab`，可以看到，在示例代码中，我选择了覆写 `rawTab` 方法，那么他们有什么区别呢？

区别在于，`rawTab` 方法返回的是所有可能的补全值，而 `tab` 方法返回的是以当前传入文本开头的返回值。

比如，我现在输入的是"抛出错误 "，此时，`rawTab` 方法和 `tab` 方法返回同样的 `List<String>`：`["运行时错误", "非法参数错误"]`

但如果我输入的是"抛出错误 运行"，此时，`rawTab` 方法应返回 `["运行时错误", "非法参数错误"]`，而 `tab` 方法将返回 `["运行时错误"]`

对于这两个方法的 `@NonNull String remaining` 参数，上述的两种情况应该分别为空字符串和 `运行` 文本。

对于没有覆写 `rawTab` 或 `tab` 方法的参数, 补全应为当前参数节点的 ID，即 `Node#getId`。

## Argument#parse

参数的解析基于对字符串的读取，通过 `StringReader` 实现。

约定 `Argument#parse` 的实现过程中，如果参数解析失败，则应回滚字符串的读取偏移。

具体参照示例方法注释：

```java
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
```

## 参数解析失败

参数解析失败时，建议回滚偏移量，因为如果你需要在 `failExecutor` 中告知执行源异常参数的内容，你需要通过 `StringReader` 获取错误的参数文本内容。

比如你正在解析 `test 123` 文本，现在偏移量位于 `123` 前面，你需要读取一个整数，那么自然，你可以读取并返回 `123`。

但如果当前输入的是 `test 123x` 文本，很明显 `123x` 并不对应一个合法的整数，所以你需要返回 `null` 并将 `ParseResult` 构造器的第二个参数设置为 `false`

那么执行源该如何得知问题文本是 `123x` 呢？正如示例代码所示：

```java
@Getter
public static class ExceptionTypeArgument<S, R> extends Argument<S, ExceptionType, R> {
    public ExceptionTypeArgument() {
        setNullFailExecutor((context) -> {
            val currentText = context.getInput().readString();
            System.out.println("不存在名为 " + currentText + " 的错误类型");
        });
    }
}
```

我们可以通过 `setFailExecutor` 或 `setNullFailExecutor` 设置参数解析失败时的执行器。

后者与前者的区别在于不需要返回值，将永远返回 `null`。

可以看到，代码中我们通过 `context.getInput().readString()` 获取了后面的文本，而如果我们在参数解析失败时不进行偏移量回滚，我们将无法在此部分代码中获取正确的文本内容。

当然，你也可以通过其他手段达成这个目的，比如你有一个预期结果为 `Integer` 的参数，那么你可以编写一个容器类：

```java
@Data
public static class IntegerContainer {
    private final @NonNull String text;
    private final @Nullable Integer result;
}
```

然后令 `IntegerContainer` 成为你参数的解析类型。

当内容解析失败时，返回一个 `new ParseResult<>(new IntegerContainer(text, null), false)`。

当内容解析成功时，返回一个 `new ParseResult<>(new IntegerContainer(text, result), true)`。

这样在 `failExecutor` 中你就可以直接获取参数文本，而不必重新通过 `StringReader` 读取了。

当然，就算你按这种方案编写参数解析逻辑，我还是建议你在解析失败时回滚偏移量，这样可以维持主体逻辑一致，更加规范。

## 参数默认值

我给出了两种参数默认值的填写方法，一种是直接设置默认值对象，一种是设置一个用于获取默认值的 `Function`。

在上面分别体现为 `new StringArgument<Void, Void>().setDefaultValue(RUNTIME_EXCEPTION_TYPE)` 和 `new ExceptionTypeArgument<Void, Void>().setDefaultValue((source) -> new ParseResult<>(ExceptionType.RUNTIME_EXCEPTION, true)))`

直接设置默认值的方法适用于固定默认值的情况，而传入 `Function` 的方法适用于默认值与执行源相关的情况。

`ParseResult` 对象构造器的第二个参数代表解析结果是否合法，对于生成默认值的场景，一般应填入 `true`

## 执行器

示例代码中，我只使用了 `setNullExecutor` 方法设置无返回值的执行器，在实际使用过程中（如变量解析），你可以使用 `setExecutor` 方法设置带有返回值的执行器。

## Kotlin 实现

在 `Kotlin` 中，我倾向于使用这种写法：

```kotlin
node = root<Unit, Unit>("root") {
    literal("打印文本") {
        argument("文本内容", StringArgument()) {
            setNullExecutor { context ->
                val text = context.getArgument<String>("文本内容")
                println(text)
            }
        }
    }
    literal("抛出错误") {
        argument("错误类型", StringArgument<Unit, Unit>().setDefaultValue(RUNTIME_EXCEPTION_TYPE)) {
            setNullExecutor { context ->
                val type = context.getArgument<String>("错误类型")
                if (RUNTIME_EXCEPTION_TYPE == type) {
                    throw RuntimeException()
                } else if (INVALID_PARAMETER_EXCEPTION_TYPE == type) {
                    throw InvalidParameterException()
                }
            }
            setTaber { context, remaining ->
                VALID_EXCEPTION_TYPES.filter { type ->
                    type.startsWith(remaining)
                }
            }
        }
    }
    literal("抛出错误-写法2") {
        argument(
            "错误类型",
            ExceptionTypeArgument<Unit, Unit>().setDefaultValue { source -> ParseResult(ExceptionType.RUNTIME_EXCEPTION, true) }
        ) {
            setNullExecutor { context ->
                val type = context.getArgument<ExceptionType>("错误类型")
                if (type == ExceptionType.RUNTIME_EXCEPTION) {
                    throw RuntimeException()
                } else if (type == ExceptionType.INVALID_PARAMETER_EXCEPTION) {
                    throw InvalidParameterException()
                }
            }
        }
    }
}
```
