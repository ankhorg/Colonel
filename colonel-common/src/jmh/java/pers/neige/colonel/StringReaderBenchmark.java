package pers.neige.colonel;

import org.openjdk.jmh.annotations.*;
import pers.neige.colonel.reader.StringReader;

import java.util.concurrent.TimeUnit;

/**
 * 在忽略分隔符的情况下，对比各种情境下 StringReader 与 Java标准库 读取整数及小数的速度差异
 *
 * <p>测试结论：
 * <ul>
 *   <li>非法字符 StringReader 读取速度快的多得多</li>
 *   <li>合法字符 StringReader 读取速度稍快</li>
 * </ul>
 * <p>
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 800, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 800, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(1)
public class StringReaderBenchmark {
    // @Benchmark
    public void readValidIntegerByReader() {
        StringReader reader = new StringReader("12345");
        reader.readInteger();
    }

    // @Benchmark
    public void readValidIntegerByParse() {
        Integer.parseInt("12345");
    }

    // @Benchmark
    public void readValidHighIntegerByReader() {
        StringReader reader = new StringReader("2147483646");
        reader.readInteger();
    }

    // @Benchmark
    public void readValidHighIntegerByParse() {
        Integer.parseInt("2147483646");
    }

    // @Benchmark
    public void readInvalidIntegerByReader() {
        StringReader reader = new StringReader("hello");
        reader.readInteger();
    }

    // @Benchmark
    public void readInvalidIntegerByParse() {
        try {
            Integer.parseInt("hello");
        } catch (NumberFormatException ignored) {
        }
    }

    // @Benchmark
    public void readValidDoubleByReader() {
        StringReader reader = new StringReader("12345");
        reader.readDouble();
    }

    // @Benchmark
    public void readValidDoubleByParse() {
        Double.parseDouble("12345");
    }

    // @Benchmark
    public void readInvalidDoubleByReader() {
        StringReader reader = new StringReader("hello");
        reader.readDouble();
    }

    // @Benchmark
    public void readInvalidDoubleByParse() {
        try {
            Double.parseDouble("hello");
        } catch (NumberFormatException ignored) {
        }
    }
}
