package pers.neige.colonel.arguments.impl;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import pers.neige.colonel.arguments.Argument;
import pers.neige.colonel.arguments.ParseResult;
import pers.neige.colonel.context.Context;
import pers.neige.colonel.reader.StringReader;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 布尔量参数类型
 */
@Builder
@NoArgsConstructor
@SuppressWarnings("unused")
public class BooleanArgument<S, R> extends Argument<S, Boolean, R> {
    private final static List<String> SUGGESTIONS = Arrays.asList("true", "false");

    @Override
    @NonNull
    public ParseResult<Boolean> parse(@NonNull StringReader input, @Nullable S source) {
        val result = input.readBoolean();
        if (result == null) {
            return new ParseResult<>(null, false);
        } else {
            return new ParseResult<>(result, true);
        }
    }

    @Override
    protected @NonNull Collection<String> rawTab(@NonNull Context<S, R> context, @NonNull String remaining) {
        return SUGGESTIONS;
    }
}
