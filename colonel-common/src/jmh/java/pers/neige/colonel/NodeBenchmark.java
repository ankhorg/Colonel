package pers.neige.colonel;

import org.openjdk.jmh.annotations.*;
import pers.neige.colonel.arguments.impl.IntegerArgument;
import pers.neige.colonel.arguments.impl.MapArgument;
import pers.neige.colonel.node.Node;
import pers.neige.colonel.node.impl.ArgumentNode;
import pers.neige.colonel.node.impl.LiteralNode;
import pers.neige.colonel.node.impl.RootNode;
import pers.neige.colonel.reader.StringReader;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 对比 LiteralNode#names 存在 StringReader 分隔符对读取速度的影响
 *
 * <p>测试结论：
 * <ul>
 *   <li>参数的数量对解析速度没有影响</li>
 *   <li>参数存在分隔符时解析速度变慢，大约为普通状态下的50%</li>
 * </ul>
 * <p>
 * 需要注意的是，对于大量参数的情况，建议统一构建字符搜索器，不然 Node 初始化的过程将非常耗时
 * <p>
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 800, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 800, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(1)
public class NodeBenchmark {
    private static final Map<String, Integer> params;
    private static final Node<String, String> specialNode;
    private static final Node<String, String> normalNode;
    private static final Node<String, String> specialBigNode;
    private static final Node<String, String> normalBigNode;

    static {
        params = new HashMap<>();
        params.put("test1", 1);
        params.put("test2", 2);
        params.put("test3", 3);

        specialNode = new RootNode<String, String>("root")
            .then(LiteralNode.literal("hello1"))
            .then(LiteralNode.<String, String>literal("hello2").then(LiteralNode.literal("world1")))
            .then(LiteralNode.<String, String>literal("hello3").then(
                ArgumentNode.argument("map1", new MapArgument<>(() -> params))
            ))
            .then(LiteralNode.<String, String>literal("hello4").then(
                ArgumentNode.argument("int1", new IntegerArgument<>())
            ))
            .then(LiteralNode.<String, String>literal("allow separator literal").then(LiteralNode.literal("world2")));

        normalNode = new RootNode<String, String>("root")
            .then(LiteralNode.literal("hello1"))
            .then(LiteralNode.<String, String>literal("hello2").then(LiteralNode.literal("world1")))
            .then(LiteralNode.<String, String>literal("hello3").then(
                ArgumentNode.argument("map1", new MapArgument<>(() -> params))
            ))
            .then(LiteralNode.<String, String>literal("hello4").then(
                ArgumentNode.argument("int1", new IntegerArgument<>())
            ))
            .then(LiteralNode.<String, String>literal("allow-separator-literal").then(LiteralNode.literal("world2")));

        specialBigNode = new RootNode<String, String>("root")
            .then(LiteralNode.<String, String>literal("allow separator literal").then(LiteralNode.literal("world2")));
        for (int index = 0; index < 10000; index++) {
            specialBigNode.then(LiteralNode.literal(UUID.randomUUID().toString()), false);
        }
        specialBigNode.buildLiteralSearcher();

        normalBigNode = new RootNode<String, String>("root")
            .then(LiteralNode.<String, String>literal("allow-separator-literal").then(LiteralNode.literal("world2")));
        for (int index = 0; index < 10000; index++) {
            normalBigNode.then(LiteralNode.literal(UUID.randomUUID().toString()), false);
        }
        normalBigNode.buildLiteralSearcher();
    }

    // @Benchmark
    public void parseA0() {
        specialNode.parseExecuteContext(StringReader.of("hello hello hello hello hello"), null);
    }

    // @Benchmark
    public void parseA1() {
        normalNode.parseExecuteContext(StringReader.of("hello hello hello hello hello"), null);
    }

    // @Benchmark
    public void parseB0() {
        specialNode.parseExecuteContext(StringReader.of("abcdefghijklmn abcdefghijklmn"), null);
    }

    // @Benchmark
    public void parseB1() {
        normalNode.parseExecuteContext(StringReader.of("abcdefghijklmn abcdefghijklmn"), null);
    }

    // @Benchmark
    public void parseSeparator0() {
        specialNode.parseExecuteContext(StringReader.of("allow separator literal world"), null);
    }

    // @Benchmark
    public void parseSeparator1() {
        normalNode.parseExecuteContext(StringReader.of("allow-separator-literal world"), null);
    }

    // @Benchmark
    public void parseBigA0() {
        specialBigNode.parseExecuteContext(StringReader.of("hello hello hello hello hello"), null);
    }

    // @Benchmark
    public void parseBigA1() {
        normalBigNode.parseExecuteContext(StringReader.of("hello hello hello hello hello"), null);
    }

    // @Benchmark
    public void parseBigB0() {
        specialBigNode.parseExecuteContext(StringReader.of("abcdefghijklmn abcdefghijklmn"), null);
    }

    // @Benchmark
    public void parseBigB1() {
        normalBigNode.parseExecuteContext(StringReader.of("abcdefghijklmn abcdefghijklmn"), null);
    }

    // @Benchmark
    public void parseBigSeparator0() {
        specialBigNode.parseExecuteContext(StringReader.of("allow separator literal world"), null);
    }

    // @Benchmark
    public void parseBigSeparator1() {
        normalBigNode.parseExecuteContext(StringReader.of("allow-separator-literal world"), null);
    }
}
