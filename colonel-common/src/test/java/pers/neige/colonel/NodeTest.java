package pers.neige.colonel;

import lombok.val;
import lombok.var;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pers.neige.colonel.arguments.impl.IntegerArgument;
import pers.neige.colonel.arguments.impl.MapArgument;
import pers.neige.colonel.node.Node;
import pers.neige.colonel.node.impl.ArgumentNode;
import pers.neige.colonel.node.impl.LiteralNode;
import pers.neige.colonel.node.impl.RootNode;
import pers.neige.colonel.reader.StringReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTest {
    private static Map<String, Integer> params0;
    private static Map<String, Integer> params1;
    private static Node<Void, String> node;

    @BeforeAll
    public static void setup() {
        params0 = new HashMap<>();
        params0.put("test1", 1);
        params0.put("test2", 2);
        params0.put("test3", 3);
        params0.put("test3test", 3);
        params0.put("boom", 3);

        params1 = new HashMap<>();
        params1.put("hello7", 1);
        params1.put("hello8", 2);

        node = new RootNode<Void, String>("root")
                .setExecutor((context) -> "啥也妹有")
                .then(LiteralNode.literal("hello1"))
                .then(LiteralNode.<Void, String>literal("hello2").then(LiteralNode.literal("world1")))
                .then(LiteralNode.<Void, String>literal("hello3").then(
                        ArgumentNode.argument("map1", new MapArgument<>(() -> params0))
                ))
                .then(LiteralNode.<Void, String>literal("hello4").then(
                        ArgumentNode.argument("int1", new IntegerArgument<>())
                ))
                .then(LiteralNode.<Void, String>literal("hello5").then(
                        ArgumentNode.<Void, Integer, String>argument("int2", new IntegerArgument<>()).setExecutor((context) -> {
                            Integer int2 = context.getArgument("int2", Integer.class);
                            return "当前输入的整形参数是: " + int2;
                        })
                ))
                .then(LiteralNode.<Void, String>literal("allow separator literal").then(LiteralNode.literal("world2")))
                .then(LiteralNode.<Void, String>literal("hello6").then(
                        ArgumentNode.argument("int3", new IntegerArgument<Void, String>().setDefaultValue(0))
                ))
                .then(LiteralNode.literal("hello6hello"))
                .then(LiteralNode.literal("hello7", params1))
                .then(LiteralNode.literal("HELLO9"));

    }

    @Test
    public void init() {
        assertThrows(NullPointerException.class, () -> {
            val map = new HashMap<String, String>();
            map.put(null, "test");
            new RootNode<Void, String>("root").then(LiteralNode.literal("test", map));
        });

        assertThrows(NullPointerException.class, () -> {
            val map = new HashMap<String, String>();
            map.put("test", null);
            new RootNode<Void, String>("root").then(LiteralNode.literal("test", map));
        });

        assertThrows(NullPointerException.class, () -> {
            val map = new HashMap<String, String>();
            map.put(null, null);
            new RootNode<Void, String>("root").then(LiteralNode.literal("test", map));
        });
    }

    @Test
    public void parse() {
        var result = node.parseExecuteContext(StringReader.of(""), null);
        assertEquals(0, result.size());
        assertEquals("啥也妹有", result.execute());

        result = node.parseExecuteContext(StringReader.of("hello hello"), null);
        assertEquals(0, result.size());

        result = node.parseExecuteContext(StringReader.of("hello1"), null);
        assertEquals(1, result.size());
        assertEquals("hello1", result.getArgument("hello1"));

        result = node.parseExecuteContext(StringReader.of("hello1 hello"), null);
        assertEquals(1, result.size());
        assertEquals("hello1", result.getArgument("hello1"));

        result = node.parseExecuteContext(StringReader.of("hello2 world1"), null);
        assertEquals(2, result.size());
        assertEquals("hello2", result.getArgument("hello2"));
        assertEquals("world1", result.getArgument("world1"));

        result = node.parseExecuteContext(StringReader.of("hello2 world1 hello"), null);
        assertEquals(2, result.size());
        assertEquals("hello2", result.getArgument("hello2"));
        assertEquals("world1", result.getArgument("world1"));

        result = node.parseExecuteContext(StringReader.of("hello3 map1"), null);
        assertEquals(2, result.size());
        assertEquals("hello3", result.getArgument("hello3"));
        assertNull(result.getArgument("map1"));

        result = node.parseExecuteContext(StringReader.of("hello3 test1"), null);
        assertEquals(2, result.size());
        assertEquals("hello3", result.getArgument("hello3"));
        assertEquals(1, result.<Integer>getArgument("map1"));

        result = node.parseExecuteContext(StringReader.of("hello4 123123"), null);
        assertEquals(2, result.size());
        assertEquals("hello4", result.getArgument("hello4"));
        assertEquals(123123, result.<Integer>getArgument("int1"));

        result = node.parseExecuteContext(StringReader.of("hello4 test"), null);
        assertEquals(2, result.size());
        assertEquals("hello4", result.getArgument("hello4"));
        assertNull(result.getArgument("int1"));

        result = node.parseExecuteContext(StringReader.of("hello5 123123"), null);
        assertEquals(2, result.size());
        assertEquals("hello5", result.getArgument("hello5"));
        assertEquals(123123, result.<Integer>getArgument("int2"));
        assertEquals("当前输入的整形参数是: 123123", result.execute());

        result = node.parseExecuteContext(StringReader.of("allow separator literal world"), null);
        assertEquals(1, result.size());
        assertEquals("allow separator literal", result.getArgument("allow separator literal"));

        result = node.parseExecuteContext(StringReader.of("allow separator literal world2"), null);
        assertEquals(2, result.size());
        assertEquals("allow separator literal", result.getArgument("allow separator literal"));
        assertEquals("world2", result.getArgument("world2"));

        result = node.parseExecuteContext(StringReader.of("hello6"), null);
        assertEquals(2, result.size());
        assertEquals("hello6", result.getArgument("hello6"));
        assertEquals(0, result.<Integer>getArgument("int3"));

        result = node.parseExecuteContext(StringReader.of("hello6 xxx"), null);
        assertEquals(2, result.size());
        assertEquals("hello6", result.getArgument("hello6"));
        assertFalse(result.isExecutable());
        assertFalse(result.isArgumentSuccess("int3"));
        assertNull(result.getArgument("int3"));

        result = node.parseExecuteContext(StringReader.of("hello6 123"), null);
        assertEquals(2, result.size());
        assertEquals("hello6", result.getArgument("hello6"));
        assertEquals(123, result.<Integer>getArgument("int3"));

        result = node.parseExecuteContext(StringReader.of("hello7"), null);
        assertEquals(1, result.size());
        assertEquals(1, result.<Integer>getArgument("hello7"));

        result = node.parseExecuteContext(StringReader.of("hello8"), null);
        assertEquals(1, result.size());
        assertEquals(2, result.<Integer>getArgument("hello7"));

        result = node.parseExecuteContext(StringReader.of("HellO9"), null);
        assertEquals(1, result.size());
        assertEquals("HELLO9", result.getArgument("HELLO9"));

        result = node.parseExecuteContext(StringReader.of("hello9"), null);
        assertEquals(1, result.size());
        assertEquals("HELLO9", result.getArgument("HELLO9"));

        result = node.parseExecuteContext(StringReader.of("HELLO9"), null);
        assertEquals(1, result.size());
        assertEquals("HELLO9", result.getArgument("HELLO9"));
    }

    @Test
    public void tab() {
        var result = node.tab(StringReader.of(""), null);
        assertEquals(node.getLiteralNodes().size(), result.size());
        assertEquals(new ArrayList<>(node.getLiteralNodes().keySet()), result);

        result = node.tab(StringReader.of("he"), null);
        assertEquals(node.getLiteralNodes().keySet().stream().filter(key -> key.startsWith("he")).count(), result.size());
        assertEquals(node.getLiteralNodes().keySet().stream().filter(key -> key.startsWith("he")).collect(Collectors.toList()), result);

        result = node.tab(StringReader.of("hello2"), null);
        assertEquals(node.getLiteralNodes().keySet().stream().filter(key -> key.startsWith("hello2")).count(), result.size());
        assertEquals(node.getLiteralNodes().keySet().stream().filter(key -> key.startsWith("hello2")).collect(Collectors.toList()), result);

        result = node.tab(StringReader.of("hello6"), null);
        assertEquals(node.getLiteralNodes().keySet().stream().filter(key -> key.startsWith("hello6")).count(), result.size());
        assertEquals(node.getLiteralNodes().keySet().stream().filter(key -> key.startsWith("hello6")).collect(Collectors.toList()), result);

        result = node.tab(StringReader.of("hello2 "), null);
        assertEquals(1, result.size());
        assertEquals(Collections.singletonList("world1"), result);

        result = node.tab(StringReader.of("hello2 world"), null);
        assertEquals(1, result.size());
        assertEquals(Collections.singletonList("world1"), result);

        result = node.tab(StringReader.of("hello2 world1"), null);
        assertEquals(1, result.size());
        assertEquals(Collections.singletonList("world1"), result);

        result = node.tab(StringReader.of("hello3 "), null);
        assertEquals(params0.size(), result.size());
        assertEquals(new ArrayList<>(params0.keySet()), result);

        result = node.tab(StringReader.of("hello3 te"), null);
        assertEquals(params0.keySet().stream().filter(key -> key.startsWith("te")).count(), result.size());
        assertEquals(params0.keySet().stream().filter(key -> key.startsWith("te")).collect(Collectors.toList()), result);

        result = node.tab(StringReader.of("hello3 bo"), null);
        assertEquals(params0.keySet().stream().filter(key -> key.startsWith("bo")).count(), result.size());
        assertEquals(params0.keySet().stream().filter(key -> key.startsWith("bo")).collect(Collectors.toList()), result);

        result = node.tab(StringReader.of("hello3 test3"), null);
        assertEquals(params0.keySet().stream().filter(key -> key.startsWith("test3")).count(), result.size());
        assertEquals(params0.keySet().stream().filter(key -> key.startsWith("test3")).collect(Collectors.toList()), result);

        result = node.tab(StringReader.of("allow "), null);
        assertEquals(node.getLiteralNodes().keySet().stream().filter(key -> key.startsWith("allow ")).count(), result.size());
        assertEquals(node.getLiteralNodes().keySet().stream().filter(key -> key.startsWith("allow ")).collect(Collectors.toList()), result);
    }
}
