package pers.neige.colonel

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import pers.neige.colonel.arguments.impl.IntegerArgument
import pers.neige.colonel.arguments.impl.MapArgument
import pers.neige.colonel.context.Context
import pers.neige.colonel.reader.StringReader

class ColonelTest {
    @Test
    fun test() {
        val node = root<String, String>("root") {
            literal("hello1")
            literal("hello2") {
                literal("world")
            }
            literal("hello3") {
                argument("map", MapArgument { hashMapOf("test1" to 1) })
            }
            literal("hello4") {
                argument("int", IntegerArgument())
            }
            literal("hello5") {
                argument("int", IntegerArgument()) {
                    setExecutor { context ->
                        val int = context.getArgument(
                            "int", Integer::class.java
                        )
                        "当前输入的整形参数是: $int"
                    }
                }
            }
            literal("allow separator literal") {
                literal("world")
            }
            literal("hello6") {
                argument("int", IntegerArgument<String, String>().setDefaultValue(0))
            }
            literal("hello7", hashMapOf("hello7" to 1, "hello8" to 2))
        }

        var result: Context<String, String> = node.parseExecuteContext(
            StringReader.of(""), "source"
        )
        Assertions.assertEquals(0, result.size())

        result = node.parseExecuteContext(StringReader.of("hello hello"), "source")
        Assertions.assertEquals(0, result.size())

        result = node.parseExecuteContext(StringReader.of("hello1"), "source")
        Assertions.assertEquals(1, result.size())
        Assertions.assertEquals("hello1", result.getArgument("hello1"))

        result = node.parseExecuteContext(StringReader.of("hello1 hello"), "source")
        Assertions.assertEquals(1, result.size())
        Assertions.assertEquals("hello1", result.getArgument("hello1"))

        result = node.parseExecuteContext(StringReader.of("hello2 world"), "source")
        Assertions.assertEquals(2, result.size())
        Assertions.assertEquals("hello2", result.getArgument("hello2"))
        Assertions.assertEquals("world", result.getArgument("world"))

        result = node.parseExecuteContext(StringReader.of("hello2 world hello"), "source")
        Assertions.assertEquals(2, result.size())
        Assertions.assertEquals("hello2", result.getArgument("hello2"))
        Assertions.assertEquals("world", result.getArgument("world"))

        result = node.parseExecuteContext(StringReader.of("hello3 map"), "source")
        Assertions.assertEquals(2, result.size())
        Assertions.assertEquals("hello3", result.getArgument("hello3"))
        Assertions.assertNull(result.getArgument("map"))

        result = node.parseExecuteContext(StringReader.of("hello3 test1"), "source")
        Assertions.assertEquals(2, result.size())
        Assertions.assertEquals("hello3", result.getArgument("hello3"))
        Assertions.assertEquals(1, result.getArgument("map"))

        result = node.parseExecuteContext(StringReader.of("hello4 123123"), "source")
        Assertions.assertEquals(2, result.size())
        Assertions.assertEquals("hello4", result.getArgument("hello4"))
        Assertions.assertEquals(123123, result.getArgument("int"))

        result = node.parseExecuteContext(StringReader.of("hello4 test"), "source")
        Assertions.assertEquals(2, result.size())
        Assertions.assertEquals("hello4", result.getArgument("hello4"))
        Assertions.assertNull(result.getArgument("int"))

        result = node.parseExecuteContext(StringReader.of("hello5 123123"), "source")
        Assertions.assertEquals(2, result.size())
        Assertions.assertEquals("hello5", result.getArgument("hello5"))
        Assertions.assertEquals(123123, result.getArgument("int"))
        Assertions.assertEquals("当前输入的整形参数是: 123123", result.execute())

        result = node.parseExecuteContext(StringReader.of("allow separator literal world1"), "source")
        Assertions.assertEquals(1, result.size())
        Assertions.assertEquals("allow separator literal", result.getArgument("allow separator literal"))

        result = node.parseExecuteContext(StringReader.of("allow separator literal world"), "source")
        Assertions.assertEquals(2, result.size())
        Assertions.assertEquals("allow separator literal", result.getArgument("allow separator literal"))
        Assertions.assertEquals("world", result.getArgument("world"))

        result = node.parseExecuteContext(StringReader.of("hello6"), "source")
        Assertions.assertEquals(2, result.size())
        Assertions.assertEquals("hello6", result.getArgument("hello6"))
        Assertions.assertEquals(0, result.getArgument("int"))

        result = node.parseExecuteContext(StringReader.of("hello6 xxx"), "source")
        Assertions.assertEquals(2, result.size())
        Assertions.assertEquals("hello6", result.getArgument("hello6"))
        Assertions.assertFalse(result.isExecutable)
        Assertions.assertFalse(result.isArgumentSuccess("int"))
        Assertions.assertNull(result.getArgument("int"))

        result = node.parseExecuteContext(StringReader.of("hello6 123"), "source")
        Assertions.assertEquals(2, result.size())
        Assertions.assertEquals("hello6", result.getArgument("hello6"))
        Assertions.assertEquals(123, result.getArgument("int"))

        result = node.parseExecuteContext(StringReader.of("hello7"), "source")
        Assertions.assertEquals(1, result.size())
        Assertions.assertEquals(1, result.getArgument("hello7"))

        result = node.parseExecuteContext(StringReader.of("hello8"), "source")
        Assertions.assertEquals(1, result.size())
        Assertions.assertEquals(2, result.getArgument("hello7"))
    }
}
