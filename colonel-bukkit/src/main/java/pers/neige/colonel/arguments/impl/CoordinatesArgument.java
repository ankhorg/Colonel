package pers.neige.colonel.arguments.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.var;
import org.jetbrains.annotations.Nullable;
import pers.neige.colonel.arguments.Argument;
import pers.neige.colonel.arguments.ParseResult;
import pers.neige.colonel.context.Context;
import pers.neige.colonel.coordinates.Coordinates;
import pers.neige.colonel.coordinates.CoordinatesContainer;
import pers.neige.colonel.coordinates.LocationType;
import pers.neige.colonel.reader.StringReader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 世界参数类型
 */
@Getter
@SuppressWarnings("unused")
public class CoordinatesArgument<S, R> extends Argument<S, CoordinatesContainer, R> {
    private final static List<String> SUGGESTIONS = Arrays.asList("~ ~ ~", "^ ^ ^");

    @Override
    @NonNull
    public ParseResult<CoordinatesContainer> parse(@NonNull StringReader input, @Nullable S source) {
        val container = Coordinates.parse(input);
        return new ParseResult<>(container, container.getResult() != null);
    }

    @Override
    public @NonNull List<String> tab(@NonNull Context<S, R> context, @NonNull String remaining) {
        if (remaining.isEmpty()) {
            return SUGGESTIONS;
        }
        val builder = new StringBuilder();
        val input = context.getInput();
        val reader = new StringReader(remaining, input.getSeparator(), input.getEscape());

        val type = Coordinates.readLocationType(reader);
        Double d;
        if (!reader.canRead() || (d = reader.readDouble()) != null && !reader.canRead()) {
            builder.append(reader.peekPrevious());
            if (type == LocationType.RELATIVE) {
                builder.append(" ~ ~");
            } else {
                builder.append(" ^ ^");
            }
            return Collections.singletonList(builder.toString());
        }
        if (d == null || reader.current() != reader.getSeparator()) {
            builder.append(reader.peekPrevious());
            return Collections.singletonList(builder.toString());
        }
        reader.skipSeparator();
        if (!reader.canRead()) {
            builder.append(reader.peekPrevious());
            if (type == LocationType.RELATIVE) {
                builder.append("~ ~");
            } else if (type == LocationType.LOCAL) {
                builder.append("^ ^");
            }
            return Collections.singletonList(builder.toString());
        }
        var nextType = Coordinates.readLocationType(reader);
        if (nextType != type) {
            if (type != LocationType.ABSOLUTE) {
                reader.skip(-1);
            }
            builder.append(reader.peekPrevious());
            if (type == LocationType.RELATIVE) {
                builder.append("~ ~");
            } else if (type == LocationType.LOCAL) {
                builder.append("^ ^");
            }
            return Collections.singletonList(builder.toString());
        }
        if (!reader.canRead() || (d = reader.readDouble()) != null && !reader.canRead()) {
            builder.append(reader.peekPrevious());
            if (type == LocationType.RELATIVE) {
                builder.append(" ~");
            } else if (type == LocationType.LOCAL) {
                builder.append(" ^");
            }
            return Collections.singletonList(builder.toString());
        }
        if (d == null || reader.current() != reader.getSeparator()) {
            builder.append(reader.peekPrevious());
            return Collections.singletonList(builder.toString());
        }
        nextType = Coordinates.readLocationType(reader);
        if (nextType != type) {
            if (type != LocationType.ABSOLUTE) {
                reader.skip(-1);
            }
            builder.append(reader.peekPrevious());
            if (type == LocationType.RELATIVE) {
                builder.append("~");
            } else if (type == LocationType.LOCAL) {
                builder.append("^");
            }
            return Collections.singletonList(builder.toString());
        }
        reader.readDouble();
        builder.append(reader.peekPrevious());
        return Collections.singletonList(builder.toString());
    }
}
