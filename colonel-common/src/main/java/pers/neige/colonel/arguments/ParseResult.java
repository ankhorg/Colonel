package pers.neige.colonel.arguments;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

/**
 * 节点解析结果
 */
@Data
public class ParseResult<T> {
    /**
     * 节点解析结果
     */
    private final @Nullable T result;
    /**
     * 是否解析成功
     */
    private final boolean success;
}
