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

    private static @NonNull List<String> getDefaultSuggestions(char separator, char escape) {
        val result = new ArrayList<String>();
        result.add(getDefaultRelativeSuggestion(separator, escape));
        result.add(getDefaultLocalSuggestion(separator, escape));
        result.add(getDefaultAbsoluteSuggestion(separator, escape));
        return result;
    }

    private static @NonNull String getDefaultRelativeSuggestion(char separator, char escape) {
        return getLastRelativeArg(separator, escape) +
                separator +
                getLastRelativeArg(separator, escape) +
                separator +
                getLastRelativeArg(separator, escape);
    }

    private static @NonNull String getDefaultLocalSuggestion(char separator, char escape) {
        return getLastLocalArg(separator, escape) +
                separator +
                getLastLocalArg(separator, escape) +
                separator +
                getLastLocalArg(separator, escape);
    }

    private static @NonNull String getDefaultAbsoluteSuggestion(char separator, char escape) {
        val builder = new StringBuilder();
        val needEscape = ABSOLUTE_X_SYMBOL == separator || ABSOLUTE_X_SYMBOL == escape;
        if (needEscape) builder.append(escape);
        builder.append(ABSOLUTE_X_SYMBOL);
        builder.append(separator);
        builder.append(getLastTwoAbsoluteArgs(separator, escape));
        return builder.toString();
    }

    private static @NonNull String getLastTwoRelativeArgs(char separator, char escape) {
        return getLastRelativeArg(separator, escape) +
                separator +
                getLastRelativeArg(separator, escape);
    }

    private static @NonNull String getLastTwoLocalArgs(char separator, char escape) {
        return getLastLocalArg(separator, escape) +
                separator +
                getLastLocalArg(separator, escape);
    }

    private static @NonNull String getLastTwoAbsoluteArgs(char separator, char escape) {
        val builder = new StringBuilder();
        val needEscape = ABSOLUTE_Y_SYMBOL == separator || ABSOLUTE_Y_SYMBOL == escape;
        if (needEscape) builder.append(escape);
        builder.append(ABSOLUTE_Y_SYMBOL);
        builder.append(separator);
        builder.append(getLastAbsoluteArg(separator, escape));
        return builder.toString();
    }

    private static @NonNull String getLastRelativeArg(char separator, char escape) {
        val builder = new StringBuilder();
        val needEscape = RELATIVE_SYMBOL == separator || RELATIVE_SYMBOL == escape;
        if (needEscape) builder.append(escape);
        builder.append(RELATIVE_SYMBOL);
        return builder.toString();
    }

    private static @NonNull String getLastLocalArg(char separator, char escape) {
        val builder = new StringBuilder();
        val needEscape = LOCAL_SYMBOL == separator || LOCAL_SYMBOL == escape;
        if (needEscape) builder.append(escape);
        builder.append(LOCAL_SYMBOL);
        return builder.toString();
    }

    private static @NonNull String getLastAbsoluteArg(char separator, char escape) {
        val builder = new StringBuilder();
        val needEscape = ABSOLUTE_Z_SYMBOL == separator || ABSOLUTE_Z_SYMBOL == escape;
        if (needEscape) builder.append(escape);
        builder.append(ABSOLUTE_Z_SYMBOL);
        return builder.toString();
    }

    private static @NonNull List<String> loadLastTwoArgs(char separator, char escape, @NonNull StringBuilder builder, @NonNull LocationType type) {
        if (type == LocationType.RELATIVE) {
            builder.append(getLastTwoRelativeArgs(separator, escape));
        } else if (type == LocationType.LOCAL) {
            builder.append(getLastTwoLocalArgs(separator, escape));
        } else {
            builder.append(getLastTwoAbsoluteArgs(separator, escape));
        }
        return Collections.singletonList(builder.toString());
    }

    private static @NonNull List<String> loadLastArg(char separator, char escape, @NonNull StringBuilder builder, @NonNull LocationType type) {
        if (type == LocationType.RELATIVE) {
            builder.append(getLastRelativeArg(separator, escape));
        } else if (type == LocationType.LOCAL) {
            builder.append(getLastLocalArg(separator, escape));
        } else {
            builder.append(getLastAbsoluteArg(separator, escape));
        }
        return Collections.singletonList(builder.toString());
    }

    @Override
    @NonNull
    public ParseResult<CoordinatesContainer> parse(@NonNull StringReader input, @Nullable S source) {
        val container = Coordinates.parse(input);
        return new ParseResult<>(container, container.getResult() != null);
    }

    @Override
    public @NonNull List<String> tab(@NonNull Context<S, R> context, @NonNull String remaining) {
        val input = context.getInput();
        val separator = input.getSeparator();
        val escape = input.getEscape();
        if (remaining.isEmpty()) {
            return getDefaultSuggestions(separator, escape);
        }
        val builder = new StringBuilder();
        val reader = new StringReader(remaining, separator, escape);

        val type = Coordinates.readLocationType(reader);
        if (!reader.canRead()) {
            if (type == LocationType.RELATIVE) {
                return Collections.singletonList(getDefaultRelativeSuggestion(separator, escape));
            } else if (type == LocationType.LOCAL) {
                return Collections.singletonList(getDefaultLocalSuggestion(separator, escape));
            }
            return getDefaultSuggestions(separator, escape);
        }
        var d = reader.readDouble();
        if (d != null && !reader.canRead()) {
            builder.append(reader.peekPrevious());
            builder.append(separator);
            return loadLastTwoArgs(separator, escape, builder, type);
        }
        if (d == null && reader.current() != reader.getSeparator()) {
            return new ArrayList<>();
        }
        reader.skipSeparator();
        if (!reader.canRead()) {
            return loadLastTwoArgs(separator, escape, builder, type);
        }

        var nextType = Coordinates.readLocationType(reader);
        if (nextType != type) {
            if (type != LocationType.ABSOLUTE) {
                reader.skip(-1);
            }
            return loadLastTwoArgs(separator, escape, builder, type);
        }
        if (!reader.canRead()) {
            return loadLastTwoArgs(separator, escape, builder, type);
        }
        d = reader.readDouble();
        if (d != null && !reader.canRead()) {
            builder.append(reader.peekPrevious());
            builder.append(separator);
            return loadLastArg(separator, escape, builder, type);
        }
        if (d == null && reader.current() != reader.getSeparator()) {
            return new ArrayList<>();
        }
        reader.skipSeparator();
        if (!reader.canRead()) {
            return loadLastArg(separator, escape, builder, type);
        }

        nextType = Coordinates.readLocationType(reader);
        if (nextType != type) {
            if (type != LocationType.ABSOLUTE) {
                reader.skip(-1);
            }
            return loadLastArg(separator, escape, builder, type);
        }
        reader.readDouble();
        return new ArrayList<>();
    }
}
