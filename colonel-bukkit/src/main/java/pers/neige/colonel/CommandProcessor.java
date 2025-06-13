package pers.neige.colonel;

import lombok.NonNull;
import lombok.val;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.Nullable;
import pers.neige.colonel.context.Context;
import pers.neige.colonel.node.Node;
import pers.neige.colonel.node.ParsedNode;
import pers.neige.colonel.node.impl.LiteralNode;
import pers.neige.colonel.reader.StringReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Bukkit指令处理工具
 */
@SuppressWarnings("unused")
public class CommandProcessor {
    private static @NonNull String getPermission(@NonNull String commandName, @Nullable Context<CommandSender, ?> context) {
        val permission = new StringBuilder(commandName);
        permission.append(".command");
        if (context == null) return permission.toString();
        for (ParsedNode<CommandSender, ?, ?> parsedNode : context.getNodeChain().getNodes()) {
            if (parsedNode.getNode() instanceof LiteralNode) {
                permission.append(".");
                permission.append(parsedNode.getArgument().getResult());
            }
        }
        return permission.toString();
    }

    private static @NonNull String getCommandText(String @NonNull [] args) {
        val builder = new StringBuilder();
        for (int index = 0; index < args.length; index++) {
            if (index > 0) {
                builder.append(" ");
            }
            builder.append(args[index]);
        }
        return builder.toString();
    }

    /**
     * 以节点接替Bukkit指令的执行器和补全器
     *
     * @param command     Bukkit指令对象
     * @param root        根节点
     * @param defExecutor 默认执行器
     */
    public static <R> void processCommand(@NonNull PluginCommand command, @NonNull Node<CommandSender, R> root, @Nullable Function<Context<CommandSender, R>, R> defExecutor) {
        processCommand(command, root, null, defExecutor);
    }

    /**
     * 以节点接替Bukkit指令的执行器和补全器
     *
     * @param command              Bukkit指令对象
     * @param root                 根节点
     * @param noPermissionExecutor 无权限执行器
     * @param defExecutor          默认执行器
     */
    public static <R> void processCommand(@NonNull PluginCommand command, @NonNull Node<CommandSender, R> root, @Nullable BiFunction<Context<CommandSender, R>, String, R> noPermissionExecutor, @Nullable Function<Context<CommandSender, R>, R> defExecutor) {
        val tabPermission = command.getName() + ".command.tab";
        command.setExecutor((sender, it, label, args) -> {
            val text = getCommandText(args).trim();
            val context = root.parseExecuteContext(new StringReader(text, ' ', '\\'), sender);
            val permission = getPermission(command.getName().toLowerCase(), context);
            if (!sender.hasPermission(permission)) {
                if (noPermissionExecutor != null) {
                    noPermissionExecutor.apply(context, permission);
                }
                return true;
            }
            context.execute(defExecutor);
            return true;
        });
        command.setTabCompleter((sender, it, label, args) -> {
            if (!sender.hasPermission(tabPermission)) {
                return new ArrayList<>();
            }
            val text = getCommandText(args);
            val context = root.parseTabContext(new StringReader(text, ' ', '\\'), sender);
            val permission = getPermission(command.getName().toLowerCase(), context);
            if (!sender.hasPermission(permission)) {
                return new ArrayList<>();
            }
            return context == null ? Collections.emptyList() : context.tab();
        });
    }
}
