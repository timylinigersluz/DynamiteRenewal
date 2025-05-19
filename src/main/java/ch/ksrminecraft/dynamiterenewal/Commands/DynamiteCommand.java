package ch.ksrminecraft.dynamiterenewal.Commands;

import ch.ksrminecraft.dynamiterenewal.DynamiteRenewal;
import ch.ksrminecraft.dynamiterenewal.Utils.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class DynamiteCommand implements CommandExecutor {

    private final WorldManager worldManager;
    private final DynamiteRenewal plugin; // Referenz zu deinem Haupt-Plugin

    public DynamiteCommand(WorldManager worldManager, DynamiteRenewal plugin) {
        this.worldManager = worldManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Nur Spieler können diesen Befehl ausführen.");
            return false;
        }

        Player player = (Player) commandSender;
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Bitte gib einen Befehl an.");
            return false;
        }

        // Berechtigungsprüfung
        if (!checkPermission(player, args[0])) {
            player.sendMessage(ChatColor.RED + "Keine Berechtigung.");
            return true;
        }

        // Aufruf der entsprechenden Methode basierend auf dem Befehl
        return handleCommand(player, args);
    }

    private boolean handleCommand(Player player, String[] args) {
        switch (args[0].toLowerCase()) {
            case "create":
                return handleCreate(player, args);
            case "start":
                return handleStart(player, args);
            case "stop":
                return handleStop(player, args);
            case "restore":
                return handleRestore(player, args);
            case "backup":
                return handleBackup(player, args);
            case "delete":
                return handleDelete(player, args);
            case "reload":
                return handleReload(player);
            case "setspawn": // Neuer Fall für den Befehl /dynamite setspawn
                return handleSetSpawn(player, args);
            case "exit":
                return handleExit(player);
            default:
                player.sendMessage(ChatColor.RED + "Unbekannter Befehl.");
                return false;
        }
    }

    private boolean checkPermission(Player player, String command) {
        switch (command.toLowerCase()) {
            case "create":
            case "restore":
            case "backup":
            case "delete":
                return player.hasPermission("dynamite.admin");
            case "start":
            case "stop":
            case "setspawn":
                return player.hasPermission("dynamite.mod");
            default:
                return true;
        }
    }

    private boolean handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Bitte gib den Namen der zu löschenden Welt an.");
            return false;
        }
        String worldName = args[1];
        if (worldManager.deleteWorld(worldName)) {
            player.sendMessage(ChatColor.GREEN + "Welt " + worldName + " wurde erfolgreich gelöscht.");

            // Welt aus der Konfigurationsliste entfernen
            List<String> worlds = plugin.getConfig().getStringList("dynamiteWorlds");
            worlds.remove(worldName);
            plugin.getConfig().set("dynamiteWorlds", worlds);
            plugin.saveConfig();

            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Welt " + worldName + " konnte nicht gelöscht werden.");
            return false;
        }
    }

    private boolean handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Bitte gib den Namen der zu erstellenden Welt an.");
            return false;
        }
        String worldName = args[1];
        worldManager.createNewWorld(worldName);
        plugin.log("Welt '" + worldName + "' wurde erstellt.");
        player.sendMessage(ChatColor.GREEN + "Welt '" + worldName + "' wurde erstellt.");
        return true;
    }

    private boolean handleStart(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Bitte gib den Namen der zu startenden Dynamite-Welt an.");
            return false;
        }

        String worldName = args[1];
        List<String> dynamiteWorlds = plugin.getConfig().getStringList("dynamiteWorlds");
        if (!dynamiteWorlds.contains(worldName)) {
            player.sendMessage(ChatColor.RED + "Die Welt " + worldName + " wurde nicht gefunden!");
            return false;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Die Welt '" + worldName + "' ist nicht geladen.");
            return false;
        }

        plugin.log("Reset-Timer für Welt '" + worldName + "' wird erst aktiviert, wenn ein Spieler die Welt betritt.");

        // Registriere einen temporären Listener für den ersten Eintritt
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onFirstJoin(PlayerChangedWorldEvent event) {
                if (event.getPlayer().getWorld().getName().equals(worldName)) {
                    Player p = event.getPlayer();
                    plugin.log("Erster Spieler '" + p.getName() + "' hat Welt '" + worldName + "' betreten. Starte Reset-Timer.");

                    // Starte dann den regulären Timer
                    startResetTaskForWorld(worldName);

                    // Listener wieder abmelden
                    PlayerChangedWorldEvent.getHandlerList().unregister(this);
                }
            }
        }, plugin);

        player.sendMessage(ChatColor.YELLOW + "Warte auf ersten Spieler in der Welt '" + worldName + "'...");
        return true;
    }

    private void startResetTaskForWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        int firstWarning = plugin.getConfig().getInt("firstWarningTime", 30);
        int secondWarning = plugin.getConfig().getInt("secondWarningTime", 10);
        int resetTime = plugin.getConfig().getInt("resetTime", 1200);
        int[] timeLeft = new int[]{resetTime};

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (timeLeft[0] == firstWarning) {
                world.getPlayers().forEach(p -> p.sendMessage(ChatColor.RED + "Welt '" + worldName + "' wird in " + timeLeft[0] + " Sekunden zurückgesetzt."));
            }
            if (timeLeft[0] == secondWarning) {
                world.getPlayers().forEach(p -> p.sendMessage(ChatColor.DARK_RED + "Achtung! Welt '" + worldName + "' wird gleich zurückgesetzt."));
            }
            if (timeLeft[0] <= 0) {
                if (plugin.isRestoreRequired()) {
                    plugin.log("Welt '" + worldName + "' wird zurückgesetzt.");
                    plugin.getWorldManager().restoreWorld(worldName);
                    plugin.setRestoreRequired(false);
                }
                timeLeft[0] = resetTime;
            }
            timeLeft[0] -= 10;
        }, 0, 10 * 20L);

        plugin.setResetTaskId(taskId);
    }

    private boolean handleStop(Player player, String[] args) {
        Integer taskId = plugin.getResetTaskId();
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
            plugin.setResetTaskId(null);
            player.sendMessage(ChatColor.GREEN + "Der Zurücksetzungsprozess für '" + args[1] + "' wurde gestoppt.");
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Es gibt derzeit keinen aktiven Zurücksetzungsprozess für " + args[1] + ".");
            return false;
        }
    }

    private boolean handleRestore(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Bitte gib den Namen der wiederherzustellenden Welt an.");
            return false;
        }
        String worldName = args[1];
        return worldManager.restoreWorld(worldName);
    }

    private boolean handleBackup(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Bitte gib den Namen der zu sichernden Welt an.");
            return false;
        }
        String worldName = args[1];
        return worldManager.backupWorld(worldName);
    }

    private boolean handleReload(Player player) {
        plugin.reloadConfig();
        player.sendMessage(ChatColor.GREEN + "Die Konfiguration wurde neu geladen.");
        return true;
    }

    private boolean handleSetSpawn(Player player, String[] args) {
        // Holen des Namens der aktuellen Welt des Spielers
        String worldName = player.getWorld().getName();

        // Überprüfe, ob die Welt in der Konfigurationsliste der Dynamite-Worlds enthalten ist
        List<String> dynamiteWorlds = plugin.getConfig().getStringList("dynamiteWorlds");

        if (!dynamiteWorlds.contains(worldName)) {
            player.sendMessage(ChatColor.RED + "Du befindest dich nicht in einer Dynamite-World.");
            plugin.log("Der Spieler befindet sich nicht in einer Dynamite-World.");
            return false;
        }

        Location playerLocation = player.getLocation();
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "mv setspawn");

        // Speichere den aktuellen Standort des Spielers als Spawnpoint für die angegebene Welt
        plugin.getConfig().set("spawnpoints." + worldName + ".x", playerLocation.getX());
        plugin.getConfig().set("spawnpoints." + worldName + ".y", playerLocation.getY());
        plugin.getConfig().set("spawnpoints." + worldName + ".z", playerLocation.getZ());
        plugin.getConfig().set("spawnpoints." + worldName + ".yaw", playerLocation.getYaw());
        plugin.getConfig().set("spawnpoints." + worldName + ".pitch", playerLocation.getPitch());

        plugin.saveConfig();
        player.sendMessage(ChatColor.RED + "Spawnpoint for world '" + worldName + "' set.");

        return true;
    }

    private boolean handleExit(Player player) {
        // Teleportiere alle Spieler in ihre Herkunftswelt oder die Fallback-Welt

        UUID playerUUID = player.getUniqueId();
        String originalWorldName = plugin.getPlayerDataManager().getLastWorldName(playerUUID);
        World teleportWorld;

        if (originalWorldName != null && Bukkit.getWorld(originalWorldName) != null) {
            teleportWorld = Bukkit.getWorld(originalWorldName);
        } else {
            String fallbackWorldName = plugin.getConfig().getString("fallbackServer", "world");
            teleportWorld = Bukkit.getWorld(fallbackWorldName);
            if (teleportWorld == null) {
                teleportWorld = Bukkit.getWorlds().get(0); // Letzter Fallback
            }
        }

        player.teleport(teleportWorld.getSpawnLocation());
        plugin.log("Spieler " + player.getName() + " teleportiert nach " + teleportWorld.getName());

        return true;
    }
}

