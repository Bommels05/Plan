package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {

    private Plan plugin;

    public ReloadCommand(Plan plugin) {
        super("reload", "plan.reload", "Reload plugin config & save cached data", CommandType.CONSOLE, "");

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        plugin.reloadConfig();
        plugin.getHandler().saveCachedData();
        plugin.hookPlanLite();
        sender.sendMessage(ChatColor.GREEN+"[Plan] Reload complete.");
        
        return true;
    }

}
