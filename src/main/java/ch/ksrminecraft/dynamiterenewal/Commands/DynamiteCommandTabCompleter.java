package ch.ksrminecraft.dynamiterenewal.Commands;

import ch.ksrminecraft.dynamiterenewal.DynamiteRenewal;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DynamiteCommandTabCompleter implements TabCompleter {

    private DynamiteRenewal plugin;

    public DynamiteCommandTabCompleter(DynamiteRenewal plugin) {
        this.plugin = plugin;
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        // Der erste Parameter des Befehls
        if (args.length == 1) {
            completions.add("create");
            completions.add("start");
            completions.add("stop");
            completions.add("restore");
            completions.add("backup");
            completions.add("delete");
            completions.add("reload");
            completions.add("setspawn");
            completions.add("exit");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("restore") || args[0].equalsIgnoreCase("backup") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("stop")) {
                // Füge alle Welten aus der Config hinzu
                List<String> worlds = plugin.getConfig().getStringList("dynamiteWorlds");
                completions.addAll(worlds);
            }
        }

        // Weitere Parameter können hier hinzugefügt werden, falls benötigt
        // Zum Beispiel: Wenn der erste Parameter 'create' ist, könnten Sie hier eine Liste von Welttypen zurückgeben

        return completions;
    }
}
