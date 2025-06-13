package pers.neige.colonel;

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
    private static Map<String, Integer> params;
    private static Node<Void, String> node;

    @BeforeAll
    public static void setup() {
        params = new HashMap<>();
        params.put("test1", 1);
        params.put("test2", 2);
        params.put("test3", 3);
        params.put("test3test", 3);
        params.put("boom", 3);

        node = new RootNode<Void, String>("root")
                .setExecutor((context) -> "啥也妹有")
                .then(LiteralNode.literal("hello1"))
                .then(LiteralNode.<Void, String>literal("hello2").then(LiteralNode.literal("world1")))
                .then(LiteralNode.<Void, String>literal("hello3").then(
                        ArgumentNode.argument("map1", new MapArgument<>(() -> params))
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
                .then(LiteralNode.literal("hello6hello"));

    }

    @Test
    public void parse() {
        var result = node.parseExecuteContext(new StringReader(""), null);
        assertEquals(0, result.size());
        assertEquals("啥也妹有", result.execute());

        result = node.parseExecuteContext(new StringReader("hello hello"), null);
        assertEquals(0, result.size());

        result = node.parseExecuteContext(new StringReader("hello1"), null);
        assertEquals(1, result.size());
        assertEquals("hello1", result.getArgument("hello1"));

        result = node.parseExecuteContext(new StringReader("hello1 hello"), null);
        assertEquals(1, result.size());
        assertEquals("hello1", result.getArgument("hello1"));

        result = node.parseExecuteContext(new StringReader("hello2 world1"), null);
        assertEquals(2, result.size());
        assertEquals("hello2", result.getArgument("hello2"));
        assertEquals("world1", result.getArgument("world1"));

        result = node.parseExecuteContext(new StringReader("hello2 world1 hello"), null);
        assertEquals(2, result.size());
        assertEquals("hello2", result.getArgument("hello2"));
        assertEquals("world1", result.getArgument("world1"));

        result = node.parseExecuteContext(new StringReader("hello3 map1"), null);
        assertEquals(2, result.size());
        assertEquals("hello3", result.getArgument("hello3"));
        assertNull(result.getArgument("map1"));

        result = node.parseExecuteContext(new StringReader("hello3 test1"), null);
        assertEquals(2, result.size());
        assertEquals("hello3", result.getArgument("hello3"));
        assertEquals(1, result.<Integer>getArgument("map1"));

        result = node.parseExecuteContext(new StringReader("hello4 123123"), null);
        assertEquals(2, result.size());
        assertEquals("hello4", result.getArgument("hello4"));
        assertEquals(123123, result.<Integer>getArgument("int1"));

        result = node.parseExecuteContext(new StringReader("hello4 test"), null);
        assertEquals(2, result.size());
        assertEquals("hello4", result.getArgument("hello4"));
        assertNull(result.getArgument("int1"));

        result = node.parseExecuteContext(new StringReader("hello5 123123"), null);
        assertEquals(2, result.size());
        assertEquals("hello5", result.getArgument("hello5"));
        assertEquals(123123, result.<Integer>getArgument("int2"));
        assertEquals("当前输入的整形参数是: 123123", result.execute());

        result = node.parseExecuteContext(new StringReader("allow separator literal world"), null);
        assertEquals(1, result.size());
        assertEquals("allow separator literal", result.getArgument("allow separator literal"));

        result = node.parseExecuteContext(new StringReader("allow separator literal world2"), null);
        assertEquals(2, result.size());
        assertEquals("allow separator literal", result.getArgument("allow separator literal"));
        assertEquals("world2", result.getArgument("world2"));

        result = node.parseExecuteContext(new StringReader("hello6"), null);
        assertEquals(2, result.size());
        assertEquals("hello6", result.getArgument("hello6"));
        assertEquals(0, result.<Integer>getArgument("int3"));

        result = node.parseExecuteContext(new StringReader("hello6 xxx"), null);
        assertEquals(2, result.size());
        assertEquals("hello6", result.getArgument("hello6"));
        assertFalse(result.isExecutable());
        assertFalse(result.isArgumentSuccess("int3"));
        assertNull(result.getArgument("int3"));

        result = node.parseExecuteContext(new StringReader("hello6 123"), null);
        assertEquals(2, result.size());
        assertEquals("hello6", result.getArgument("hello6"));
        assertEquals(123, result.<Integer>getArgument("int3"));
    }

    @Test
    public void tab() {
        var result = node.tab(new StringReader(""), null);
        assertEquals(node.getLiteralNodes().size(), result.size());
        assertEquals(new ArrayList<>(node.getLiteralNodes().keySet()), result);

        result = node.tab(new StringReader("he"), null);
        assertEquals(node.getLiteralNodes().keySet().stream().filter(key -> key.startsWith("he")).count(), result.size());
        assertEquals(node.getLiteralNodes().keySet().stream().filter(key -> key.startsWith("he")).collect(Collectors.toList()), result);

        result = node.tab(new StringReader("hello2"), null);
        assertEquals(node.getLiteralNodes().keySet().stream().filter(key -> key.startsWith("hello2")).count(), result.size());
        assertEquals(node.getLiteralNodes().keySet().stream().filter(key -> key.startsWith("hello2")).collect(Collectors.toList()), result);

        result = node.tab(new StringReader("hello6"), null);
        assertEquals(node.getLiteralNodes().keySet().stream().filter(key -> key.startsWith("hello6")).count(), result.size());
        assertEquals(node.getLiteralNodes().keySet().stream().filter(key -> key.startsWith("hello6")).collect(Collectors.toList()), result);

        result = node.tab(new StringReader("hello2 "), null);
        assertEquals(1, result.size());
        assertEquals(Collections.singletonList("world1"), result);

        result = node.tab(new StringReader("hello2 world"), null);
        assertEquals(1, result.size());
        assertEquals(Collections.singletonList("world1"), result);

        result = node.tab(new StringReader("hello2 world1"), null);
        assertEquals(1, result.size());
        assertEquals(Collections.singletonList("world1"), result);

        result = node.tab(new StringReader("hello3 "), null);
        assertEquals(params.size(), result.size());
        assertEquals(new ArrayList<>(params.keySet()), result);

        result = node.tab(new StringReader("hello3 te"), null);
        assertEquals(params.keySet().stream().filter(key -> key.startsWith("te")).count(), result.size());
        assertEquals(params.keySet().stream().filter(key -> key.startsWith("te")).collect(Collectors.toList()), result);

        result = node.tab(new StringReader("hello3 bo"), null);
        assertEquals(params.keySet().stream().filter(key -> key.startsWith("bo")).count(), result.size());
        assertEquals(params.keySet().stream().filter(key -> key.startsWith("bo")).collect(Collectors.toList()), result);

        result = node.tab(new StringReader("hello3 test3"), null);
        assertEquals(params.keySet().stream().filter(key -> key.startsWith("test3")).count(), result.size());
        assertEquals(params.keySet().stream().filter(key -> key.startsWith("test3")).collect(Collectors.toList()), result);

        result = node.tab(new StringReader("allow "), null);
        assertEquals(node.getLiteralNodes().keySet().stream().filter(key -> key.startsWith("allow ")).count(), result.size());
        assertEquals(node.getLiteralNodes().keySet().stream().filter(key -> key.startsWith("allow ")).collect(Collectors.toList()), result);
    }
}
