package pers.neige.colonel.arguments.impl;

import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import pers.neige.colonel.arguments.Argument;
import pers.neige.colonel.arguments.ParseResult;
import pers.neige.colonel.context.Context;
import pers.neige.colonel.reader.StringReader;

import java.util.ArrayList;
import java.util.List;

/**
 * 玩家参数类型
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class PlayerArgument<S, R> extends Argument<S, Player, R> {
    /**
     * 禁止返回 null<br>
     * 默认值 {@code true}
     */
    @Builder.Default
    private final boolean nonnull = true;

    @Override
    public @NonNull ParseResult<Player> parse(@NonNull StringReader input, @Nullable S source) {
        val start = input.getOffset();
        val name = input.readString();
        var player = Bukkit.getPlayerExact(name);
        if (player == null && "me".equals(name) && source instanceof Player) {
            player = (Player) source;
        }
        if (player == null && nonnull) {
            input.setOffset(start);
            return new ParseResult<>(null, false);
        }
        return new ParseResult<>(player, true);
    }

    @Override
    public @NonNull List<String> tab(@NonNull Context<S, R> context, @NonNull String remaining) {
        remaining = remaining.toLowerCase();
        val result = new ArrayList<String>();
        if ("me".startsWith(remaining)) {
            result.add("me");
        }
        for (val player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(remaining)) {
                result.add(player.getName());
            }
        }
        return result;
    }
}
