package pers.neige.colonel.arguments.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;
import pers.neige.colonel.arguments.Argument;
import pers.neige.colonel.arguments.ParseResult;
import pers.neige.colonel.context.Context;
import pers.neige.colonel.context.NodeChain;
import pers.neige.colonel.reader.StringReader;

import java.util.ArrayList;
import java.util.List;

/**
 * 世界参数类型
 */
@Getter
@SuppressWarnings("unused")
public class WorldArgument<S, R> extends Argument<S, World, R> {
    @Override
    public @NonNull ParseResult<World> parse(@NonNull NodeChain<S, R> nodeChain, @NonNull StringReader input, @Nullable S source) {
        val start = input.getOffset();
        val name = input.readString();
        var world = Bukkit.getWorld(name);
        if (world == null) {
            input.setOffset(start);
            return new ParseResult<>(null, false);
        }
        return new ParseResult<>(world, true);
    }

    @Override
    public @NonNull List<String> tab(@NonNull Context<S, R> context, @NonNull String remaining) {
        remaining = remaining.toLowerCase();
        val result = new ArrayList<String>();
        for (val world : Bukkit.getWorlds()) {
            if (world.getName().toLowerCase().startsWith(remaining)) {
                result.add(world.getName());
            }
        }
        return result;
    }
}
