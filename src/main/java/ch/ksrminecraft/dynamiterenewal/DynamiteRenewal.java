package ch.ksrminecraft.dynamiterenewal;

import ch.ksrminecraft.dynamiterenewal.Commands.DynamiteCommand;
import ch.ksrminecraft.dynamiterenewal.Commands.DynamiteCommandTabCompleter;
import ch.ksrminecraft.dynamiterenewal.Listeners.PlayerWorldChangeListener;
import ch.ksrminecraft.dynamiterenewal.Listeners.WorldResourcePackListener;
import ch.ksrminecraft.dynamiterenewal.Utils.PlayerDataManager;
import ch.ksrminecraft.dynamiterenewal.Utils.WorldManager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class DynamiteRenewal extends JavaPlugin {

    private WorldManager worldManager;
    private String loggerPrefix;
    private PlayerDataManager playerDataManager; // Füge die Instanzvariable für den Spielerdaten-Manager hinzu
    private Integer resetTaskId = null; // Variable für die Task-ID
    private boolean restoreRequired = false; // Variable, um den Restore-Zustand zu speichern

    @Override
    public void onEnable() {

        if (!setupMultiverseCore()) {
            getLogger().severe("Multiverse-Core nicht gefunden! Plugin wird deaktiviert.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        worldManager = new WorldManager(this);

        // Initialisiere den Spielerdaten-Manager
        playerDataManager = new PlayerDataManager(this);

        this.getCommand("dynamite").setExecutor(new DynamiteCommand(worldManager, this));
        this.getCommand("dynamite").setTabCompleter(new DynamiteCommandTabCompleter(this));

        getServer().getPluginManager().registerEvents(new WorldResourcePackListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerWorldChangeListener(this), this);

        // Stelle sicher, dass die Konfiguration existiert
        this.saveDefaultConfig();
        loggerPrefix = getConfig().getString("loggerPrefix", "[Dynamite-Debug]"); // Lädt das Präfix

        log("DynamiteRenewal erfolgreich aktiviert!");
    }

    @Override
    public void onDisable() {
        getLogger().info("DynamiteRenewal deaktiviert.");
    }


    private boolean setupMultiverseCore() {
        return this.getServer().getPluginManager().getPlugin("Multiverse-Core") != null;
    }

    public void log(String message) {
        if (getConfig().getBoolean("log")) {
            Bukkit.getServer().getConsoleSender().sendMessage(loggerPrefix + " " + message);
        }
    }

    // Füge die Methode getPlayerDataManager hinzu, um auf den Spielerdaten-Manager zuzugreifen
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }


    public void setResetTaskId(Integer resetTaskId) {
        this.resetTaskId = resetTaskId;
    }

    public Integer getResetTaskId() {
        return resetTaskId;
    }

    public void setRestoreRequired(boolean restoreRequired) {
        this.restoreRequired = restoreRequired;
    }

    public boolean isRestoreRequired() {
        return restoreRequired;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }
}
