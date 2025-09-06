package pers.neige.colonel

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import pers.neige.colonel.arguments.Argument
import pers.neige.colonel.arguments.ParseResult
import pers.neige.colonel.arguments.impl.StringArgument
import pers.neige.colonel.context.Context
import pers.neige.colonel.node.Node
import pers.neige.colonel.reader.StringReader
import java.security.InvalidParameterException

class Example1Test {
    companion object {
        private const val RUNTIME_EXCEPTION_TYPE: String = "运行时错误"
        private const val INVALID_PARAMETER_EXCEPTION_TYPE: String = "非法参数错误"
        private val VALID_EXCEPTION_TYPES: MutableList<String> =
            arrayListOf(RUNTIME_EXCEPTION_TYPE, INVALID_PARAMETER_EXCEPTION_TYPE)
        private var node: Node<Unit, Unit>? = null

        @BeforeAll
        @JvmStatic
        fun setup() {
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
                        ExceptionTypeArgument<Unit, Unit>().setDefaultValue { source ->
                            ParseResult(
                                ExceptionType.RUNTIME_EXCEPTION,
                                true
                            )
                        }) {
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
        }
    }

    @Test
    fun parse() {
        // 后台将打印"文本测试"
        node!!.execute(StringReader.of("打印文本 文本测试"), null)

        assertThrows(RuntimeException::class.java) { node!!.execute(StringReader.of("抛出错误"), null) }
        assertThrows(RuntimeException::class.java) {
            node!!.execute(
                StringReader.of("抛出错误 $RUNTIME_EXCEPTION_TYPE"), null
            )
        }
        assertThrows(InvalidParameterException::class.java) {
            node!!.execute(
                StringReader.of("抛出错误 $INVALID_PARAMETER_EXCEPTION_TYPE"), null
            )
        }

        assertThrows(RuntimeException::class.java) { node!!.execute(StringReader.of("抛出错误-写法2"), null) }
        assertThrows(RuntimeException::class.java) {
            node!!.execute(
                StringReader.of("抛出错误-写法2 $RUNTIME_EXCEPTION_TYPE"), null
            )
        }
        assertThrows(InvalidParameterException::class.java) {
            node!!.execute(
                StringReader.of("抛出错误-写法2 $INVALID_PARAMETER_EXCEPTION_TYPE"), null
            )
        }

        assertEquals(
            mutableListOf<String?>("打印文本", "抛出错误", "抛出错误-写法2"), node!!.tab(StringReader.of(""), null)
        )
        assertEquals(
            mutableListOf<String?>("打印文本"), node!!.tab(StringReader.of("打印"), null)
        )
        assertEquals(
            mutableListOf<String?>("抛出错误", "抛出错误-写法2"), node!!.tab(StringReader.of("抛出错误"), null)
        )
        assertEquals(
            VALID_EXCEPTION_TYPES, node!!.tab(StringReader.of("抛出错误 "), null)
        )
        assertEquals(
            mutableListOf<String?>(RUNTIME_EXCEPTION_TYPE), node!!.tab(StringReader.of("抛出错误 运行时"), null)
        )
        assertEquals(
            VALID_EXCEPTION_TYPES, node!!.tab(StringReader.of("抛出错误-写法2 "), null)
        )
        assertEquals(
            mutableListOf<String?>(RUNTIME_EXCEPTION_TYPE), node!!.tab(StringReader.of("抛出错误-写法2 运行时"), null)
        )
    }

    enum class ExceptionType(val key: String) {
        RUNTIME_EXCEPTION(RUNTIME_EXCEPTION_TYPE), INVALID_PARAMETER_EXCEPTION(INVALID_PARAMETER_EXCEPTION_TYPE)
    }

    class ExceptionTypeArgument<S, R> : Argument<S?, ExceptionType?, R?>() {
        override fun parse(input: StringReader, source: S?): ParseResult<ExceptionType?> {
            // 获取当前的字符读取偏移
            val start = input.offset
            // 尝试读取一段文本(此时偏移已经移动到这段文本后)
            val name = input.readString()
            // 检测是否有符合的结果
            var result: ExceptionType? = null
            for (type in ExceptionType.entries) {
                if (type.key != name) continue
                result = type
                break
            }
            // 如果没有结果
            if (result == null) {
                // 回滚偏移
                input.offset = start
                // 返回失败的解析结果
                return ParseResult<ExceptionType?>(null, false)
            }
            // 有结果, 则返回成功的解析结果
            return ParseResult<ExceptionType?>(result, true)
        }

        override fun rawTab(context: Context<S?, R?>, remaining: String): Collection<String?> {
            return VALID_EXCEPTION_TYPES
        }
    }
}