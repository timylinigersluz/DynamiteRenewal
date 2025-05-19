package ch.ksrminecraft.dynamiterenewal.Utils;

import ch.ksrminecraft.dynamiterenewal.DynamiteRenewal;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

public class WorldManager {

    private DynamiteRenewal plugin;
    private MVWorldManager mvWorldManager;

    public WorldManager(DynamiteRenewal plugin) {
        this.plugin = plugin;
        MultiverseCore mvCore = (MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
        if (mvCore == null) {
            throw new IllegalStateException("Multiverse-Core ist nicht installiert oder nicht geladen.");
        }
        this.mvWorldManager = mvCore.getMVWorldManager();
    }

    public boolean worldExists(String worldName) {
        if (Bukkit.getWorld(worldName) == null) {
            plugin.log("Welt '" + worldName + "' existiert nicht.");
            return false;
        }
        return true;
    }

    public boolean unloadWorld(String worldName) {
        plugin.log("Starte Entladeprozess der Welt '" + worldName + "'.");
        World world = Bukkit.getWorld(worldName);

        if (!worldExists(worldName)) {
            return false;
        }

        // Teleportiere alle Spieler in ihre Herkunftswelt oder die Fallback-Welt
        for (Player player : world.getPlayers()) {
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
        }

        // Entlade die Welt
        plugin.log("Entlade die Welt: " + worldName);
        Bukkit.unloadWorld(world, true);
        return true;

    }


    public void createNewWorld(String worldName) {
        plugin.log("Versuche, neue Welt '" + worldName + "' zu erstellen.");
        if (mvWorldManager.addWorld(worldName, World.Environment.NORMAL, null, WorldType.FLAT, true, null)) {
            plugin.log("Welt '" + worldName + "' erfolgreich erstellt.");
            addWorldToConfig(worldName);

            // Setze den gewünschten Spielmodus für die Welt
            MultiverseWorld mvWorld = mvWorldManager.getMVWorld(worldName);
            if (mvWorld != null) {
                String gameModeString = plugin.getConfig().getString("defaultGameMode", "SURVIVAL").toUpperCase();
                try {
                    GameMode gameMode = GameMode.valueOf(gameModeString);
                    mvWorld.setGameMode(gameMode);
                    plugin.log("Spielmodus für Welt '" + worldName + "' auf " + gameModeString + " gesetzt.");
                } catch (IllegalArgumentException e) {
                    plugin.log("Ungültiger Spielmodus in der Konfiguration. Standardmodus SURVIVAL wird verwendet.");
                    mvWorld.setGameMode(GameMode.SURVIVAL);
                }
            }
        } else {
            plugin.log("Fehler: Die Welt '" + worldName + "' konnte nicht erstellt werden.");
        }
    }

    public boolean backupWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        plugin.log("Starte Backup.");
        if (!worldExists(worldName)) {
            return false;
        }

        unloadWorld(worldName);

        File worldFolder = world.getWorldFolder();
        File backupFolder = new File(plugin.getDataFolder(), "backups/" + worldName);

        try {

            if (worldFolder.exists()) {
                FileUtils.deleteDirectory(backupFolder);
                plugin.log("Inhalt vorhanden: Lösche files in Backupordner...");
            }

            copyFolder(worldFolder.toPath(), backupFolder.toPath());
            plugin.log("Backup für Welt '" + worldName + "' erstellt.");
            return true;
        } catch (IOException e) {
            plugin.log("Fehler beim Erstellen des Backups für Welt '" + worldName + "': " + e.getMessage());
            return false;
        } finally {
            if (!mvWorldManager.isMVWorld(worldName)) {
                plugin.log("Lade Welt '" + worldName + "' nach Backup.");
                mvWorldManager.loadWorld(worldName);
            }
        }
    }

    public boolean restoreWorld(String worldName) {

        World world = Bukkit.getWorld(worldName);

        if (!worldExists(worldName)) {
            plugin.log("Welt '" + worldName + "' existiert nicht und kann nicht gesichert werden.");
            return false;
        }
        unloadWorld(worldName);

        // Bestimmt den Backup-Ordner und den Zielordner für die Welt.
        File backupFolder = new File(plugin.getDataFolder(), "backups/" + worldName);
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);

        if (!backupFolder.exists()) {
            plugin.log("Backup-Ordner für Welt '" + worldName + "' existiert nicht.");
            return false;
        }

        try {
            if (worldFolder.exists()) {
                FileUtils.deleteDirectory(worldFolder);
                plugin.log("Lösche vorhandene Weltdateien...");
            }

            plugin.log("Beginne mit der Wiederherstellung der Welt '" + worldName + "'");
            copyFolder(backupFolder.toPath(), worldFolder.toPath());

            if (!mvWorldManager.isMVWorld(worldName)) {
                mvWorldManager.loadWorld(worldName);
                plugin.log("Welt '" + worldName + "' nach der Wiederherstellung geladen.");
            }
            return true;
        } catch (IOException e) {
            plugin.log("Fehler bei der Wiederherstellung der Welt '" + worldName + "': " + e.getMessage());
            return false;
        }
    }

    private void copyFolder(Path source, Path target) throws IOException {
        if (!Files.exists(target)) {
            plugin.log("Erstelle Zielverzeichnis: " + target.toString());
            Files.createDirectories(target);
        }
        plugin.log("Kopiere Files");
        Files.walk(source).forEach(sourcePath -> {
            Path targetPath = target.resolve(source.relativize(sourcePath));
            try {
                if (!sourcePath.toString().endsWith("session.lock")) {
                    if (Files.isDirectory(sourcePath)) {
                        if (!Files.exists(targetPath)) {
                            Files.createDirectories(targetPath);
                        }
                    } else {
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean deleteWorld(String worldName) {
        if (Bukkit.getWorld(worldName) != null) {
            Bukkit.unloadWorld(worldName, false);
        }
        if (mvWorldManager.deleteWorld(worldName)) {
            plugin.log("Welt '" + worldName + "' wurde erfolgreich gelöscht.");
            return true;
        } else {
            plugin.log("Fehler: Welt '" + worldName + "' konnte nicht gelöscht werden.");
            return false;
        }
    }

    private void addWorldToConfig(String worldName) {
        List<String> worlds = plugin.getConfig().getStringList("dynamiteWorlds");
        if (!worlds.contains(worldName)) {
            worlds.add(worldName);
            plugin.getConfig().set("dynamiteWorlds", worlds);
            plugin.saveConfig();
        }
    }

}
