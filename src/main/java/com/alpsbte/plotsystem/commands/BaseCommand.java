package com.alpsbte.plotsystem.commands;

import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseCommand implements CommandExecutor, ICommand {
    private final List<SubCommand> subCommands = new ArrayList<>();
    protected final LangUtil langUtil = LangUtil.getInstance();

    /**
     * Executes base command and checks for sub commands
     *
     * @param sender player or console
     * @param args   parameter
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (args.length > 0) {
            SubCommand subCommand = subCommands.stream()
                    .filter(subCmd -> Arrays.stream(subCmd.getNames()).anyMatch(sub -> sub.equalsIgnoreCase(args[0])))
                    .findFirst()
                    .orElse(null);

            // Check if sub commands have sub commands
            if (subCommand != null && args.length > 1) {
                SubCommand iterateSubCommand;
                for (int i = 1; i <= args.length; i++) {
                    final int index = i;
                    iterateSubCommand = subCommand.getSubCommands().stream()
                            .filter(subCmd -> Arrays.stream(subCmd.getNames()).anyMatch(sub -> sub.equalsIgnoreCase(args[index])))
                            .findFirst()
                            .orElse(null);

                    if (iterateSubCommand == null) {
                        break;
                    } else {
                        subCommand = iterateSubCommand;
                    }
                }
            }

            if (subCommand == null) {
                sendInfo(sender);
            } else {
                if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("You don't have permission to execute this command!"));
                    return true;
                }
                subCommand.onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return true;
    }

    /**
     * @return All sub commands
     */
    public List<SubCommand> getSubCommands() {
        return subCommands;
    }

    /**
     * Gets the player
     *
     * @param sender player
     * @return null if sender is not a player
     */
    protected Player getPlayer(CommandSender sender) {
        return sender instanceof Player p ? p : null;
    }

    /**
     * Registers sub command
     *
     * @param subCommand base sub command
     */
    protected void registerSubCommand(SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    @Override
    public void sendInfo(CommandSender sender) {
        List<Component> lines = new ArrayList<>();

        if (!subCommands.isEmpty()) {
            lines.add(Component.text("--------------------------", NamedTextColor.DARK_GRAY));

            getSubCommands().forEach(sub -> lines.add(createUsageLine(
                    "/" + getNames()[0] + " ",
                    sub.getNames()[0],
                    NamedTextColor.GOLD,
                    sub.getParameter(),
                    sub.getDescription()
            )));

            lines.add(Component.text("--------------------------", NamedTextColor.DARK_GRAY));
        } else {
            lines.add(createUsageLine(
                    "/" + getNames()[0],
                    null,
                    NamedTextColor.AQUA,
                    getParameter(),
                    getDescription()
            ));
        }

        lines.forEach(sender::sendMessage);
    }

    public static Component createUsageLine(
            String prefix,
            String command,
            NamedTextColor commandColor,
            String[] parameters,
            String description
    ) {
        Component component = Component.empty()
                .append(
                        Component.text("> ", NamedTextColor.GRAY)
                                .decorate(TextDecoration.BOLD)
                )
                .append(Component.text(prefix, NamedTextColor.AQUA));

        if (command != null) {
            component = component.append(
                    Component.text(command, commandColor)
            );
        }

        for (String parameter : parameters) {
            component = component.append(
                    Component.text(" <" + parameter + ">", NamedTextColor.GRAY)
            );
        }

        if (description != null) {
            component = component
                    .append(Component.text(" - ", NamedTextColor.WHITE))
                    .append(Component.text(description, NamedTextColor.WHITE));
        }

        return component;
    }
}
