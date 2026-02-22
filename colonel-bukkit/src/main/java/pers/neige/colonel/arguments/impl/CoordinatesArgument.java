package pers.neige.colonel.arguments.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.var;
import org.jetbrains.annotations.Nullable;
import pers.neige.colonel.arguments.Argument;
import pers.neige.colonel.arguments.ParseResult;
import pers.neige.colonel.context.Context;
import pers.neige.colonel.context.NodeChain;
import pers.neige.colonel.coordinates.Coordinates;
import pers.neige.colonel.coordinates.CoordinatesContainer;
import pers.neige.colonel.coordinates.LocationType;
import pers.neige.colonel.reader.StringReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 世界参数类型
 */
@Getter
@SuppressWarnings("unused")
public class CoordinatesArgument<S, R> extends Argument<S, CoordinatesContainer, R> {
    private final static char RELATIVE_SYMBOL = '~';
    private final static char LOCAL_SYMBOL = '^';
    private final static char ABSOLUTE_X_SYMBOL = 'x';
    private final static char ABSOLUTE_Y_SYMBOL = 'y';
    private final static char ABSOLUTE_Z_SYMBOL = 'z';

    private static @NonNull List<String> getDefaultSuggestions(@NonNull StringReader reader) {
        val result = new ArrayList<String>();
        result.add(getDefaultRelativeSuggestion(reader));
        result.add(getDefaultLocalSuggestion(reader));
        result.add(getDefaultAbsoluteSuggestion(reader));
        return result;
    }

    private static @NonNull String getDefaultRelativeSuggestion(@NonNull StringReader reader) {
        val separator = reader.getSeparator();
        return getLastRelativeArg(reader) +
            separator +
            getLastRelativeArg(reader) +
            separator +
            getLastRelativeArg(reader);
    }

    private static @NonNull String getDefaultLocalSuggestion(@NonNull StringReader reader) {
        val separator = reader.getSeparator();
        return getLastLocalArg(reader) +
            separator +
            getLastLocalArg(reader) +
            separator +
            getLastLocalArg(reader);
    }

    private static @NonNull String getDefaultAbsoluteSuggestion(@NonNull StringReader reader) {
        val builder = new StringBuilder();
        val needEscape = reader.isSeparator(ABSOLUTE_X_SYMBOL) || reader.isSeparator(ABSOLUTE_X_SYMBOL);
        if (needEscape) builder.append(reader.getEscape());
        builder.append(ABSOLUTE_X_SYMBOL);
        builder.append(reader.getSeparator());
        builder.append(getLastTwoAbsoluteArgs(reader));
        return builder.toString();
    }

    private static @NonNull String getLastTwoRelativeArgs(@NonNull StringReader reader) {
        return getLastRelativeArg(reader) +
            reader.getSeparator() +
            getLastRelativeArg(reader);
    }

    private static @NonNull String getLastTwoLocalArgs(@NonNull StringReader reader) {
        return getLastLocalArg(reader) +
            reader.getSeparator() +
            getLastLocalArg(reader);
    }

    private static @NonNull String getLastTwoAbsoluteArgs(@NonNull StringReader reader) {
        val builder = new StringBuilder();
        val needEscape = reader.isSeparator(ABSOLUTE_Y_SYMBOL) || reader.isSeparator(ABSOLUTE_Y_SYMBOL);
        if (needEscape) builder.append(reader.getEscape());
        builder.append(ABSOLUTE_Y_SYMBOL);
        builder.append(reader.getSeparator());
        builder.append(getLastAbsoluteArg(reader));
        return builder.toString();
    }

    private static @NonNull String getLastRelativeArg(@NonNull StringReader reader) {
        val builder = new StringBuilder();
        val needEscape = reader.isSeparator(RELATIVE_SYMBOL) || reader.isSeparator(RELATIVE_SYMBOL);
        if (needEscape) builder.append(reader.getEscape());
        builder.append(RELATIVE_SYMBOL);
        return builder.toString();
    }

    private static @NonNull String getLastLocalArg(@NonNull StringReader reader) {
        val builder = new StringBuilder();
        val needEscape = reader.isSeparator(LOCAL_SYMBOL) || reader.isSeparator(LOCAL_SYMBOL);
        if (needEscape) builder.append(reader.getEscape());
        builder.append(LOCAL_SYMBOL);
        return builder.toString();
    }

    private static @NonNull String getLastAbsoluteArg(@NonNull StringReader reader) {
        val builder = new StringBuilder();
        val needEscape = reader.isSeparator(ABSOLUTE_Z_SYMBOL) || reader.isSeparator(ABSOLUTE_Z_SYMBOL);
        if (needEscape) builder.append(reader.getEscape());
        builder.append(ABSOLUTE_Z_SYMBOL);
        return builder.toString();
    }

    private static @NonNull List<String> loadLastTwoArgs(@NonNull StringReader reader, @NonNull StringBuilder builder, @NonNull LocationType type) {
        if (type == LocationType.RELATIVE) {
            builder.append(getLastTwoRelativeArgs(reader));
        } else if (type == LocationType.LOCAL) {
            builder.append(getLastTwoLocalArgs(reader));
        } else {
            builder.append(getLastTwoAbsoluteArgs(reader));
        }
        return Collections.singletonList(builder.toString());
    }

    private static @NonNull List<String> loadLastArg(@NonNull StringReader reader, @NonNull StringBuilder builder, @NonNull LocationType type) {
        if (type == LocationType.RELATIVE) {
            builder.append(getLastRelativeArg(reader));
        } else if (type == LocationType.LOCAL) {
            builder.append(getLastLocalArg(reader));
        } else {
            builder.append(getLastAbsoluteArg(reader));
        }
        return Collections.singletonList(builder.toString());
    }

    @Override
    public @NonNull ParseResult<CoordinatesContainer> parse(@NonNull NodeChain<S, R> nodeChain, @NonNull StringReader input, @Nullable S source) {
        val container = Coordinates.parse(input);
        return new ParseResult<>(container, container.getResult() != null);
    }

    @Override
    public @NonNull List<String> tab(@NonNull Context<S, R> context, @NonNull String remaining) {
        val input = context.getInput();
        val separator = input.getSeparator();
        val escape = input.getEscape();
        if (remaining.isEmpty()) {
            return getDefaultSuggestions(input);
        }
        val builder = new StringBuilder();
        val reader = input.newReaderWithSameConfig(remaining);

        val type = Coordinates.readLocationType(reader);
        if (!reader.canRead()) {
            if (type == LocationType.RELATIVE) {
                return Collections.singletonList(getDefaultRelativeSuggestion(reader));
            } else if (type == LocationType.LOCAL) {
                return Collections.singletonList(getDefaultLocalSuggestion(reader));
            }
            return getDefaultSuggestions(reader);
        }
        var d = reader.readDouble();
        if (d != null && !reader.canRead()) {
            builder.append(reader.peekPrevious());
            builder.append(separator);
            return loadLastTwoArgs(reader, builder, type);
        }
        if (d == null && reader.current() != reader.getSeparator()) {
            return new ArrayList<>();
        }
        reader.skipSeparator();
        if (!reader.canRead()) {
            return loadLastTwoArgs(reader, builder, type);
        }

        var nextType = Coordinates.readLocationType(reader);
        if (nextType != type) {
            if (type != LocationType.ABSOLUTE) {
                reader.skip(-1);
            }
            return loadLastTwoArgs(reader, builder, type);
        }
        if (!reader.canRead()) {
            return loadLastTwoArgs(reader, builder, type);
        }
        d = reader.readDouble();
        if (d != null && !reader.canRead()) {
            builder.append(reader.peekPrevious());
            builder.append(separator);
            return loadLastArg(reader, builder, type);
        }
        if (d == null && reader.current() != reader.getSeparator()) {
            return new ArrayList<>();
        }
        reader.skipSeparator();
        if (!reader.canRead()) {
            return loadLastArg(reader, builder, type);
        }

        nextType = Coordinates.readLocationType(reader);
        if (nextType != type) {
            if (type != LocationType.ABSOLUTE) {
                reader.skip(-1);
            }
            return loadLastArg(reader, builder, type);
        }
        reader.readDouble();
        return new ArrayList<>();
    }
}
