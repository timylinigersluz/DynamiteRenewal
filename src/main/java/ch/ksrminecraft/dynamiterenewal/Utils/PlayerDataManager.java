package ch.ksrminecraft.dynamiterenewal.Utils;

import ch.ksrminecraft.dynamiterenewal.DynamiteRenewal;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlayerDataManager {

    private final DynamiteRenewal plugin;
    private final File playerDataFile;
    private final FileConfiguration playerDataConfig;

    public PlayerDataManager(DynamiteRenewal plugin) {
        this.plugin = plugin;
        this.playerDataFile = new File(plugin.getDataFolder(), "playerData.yml");
        this.playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
    }


    // Methode zum Speichern des Inventars eines Spielers für eine bestimmte Welt
    public void saveInventory(Player player, String worldName) {
        UUID playerUUID = player.getUniqueId();
        plugin.log("Speichere Inventar in PlayerData.yml für Spieler: '" + player.getName() + "' aus Welt: '" + worldName + "'");

        List<ItemStack> inventoryItems = new ArrayList<>(Arrays.asList(player.getInventory().getContents()));
        for (ItemStack item : inventoryItems) {
            if (item != null) {
                plugin.log("Speichere Item: '" + item.getType() + "', Menge: '" + item.getAmount() + "'");
            }
        }
        playerDataConfig.set(playerUUID + "." + worldName + ".inventory", inventoryItems);
        savePlayerData();
    }

    // Methode zum Laden des Inventars eines Spielers für eine bestimmte Welt
    public List<ItemStack> loadInventory(UUID playerUUID, String worldName) {
        plugin.log("Lade Inventar für Spieler: '" + playerUUID + "' in Welt: '" + worldName + "'");
        List<?> inventoryList = playerDataConfig.getList(playerUUID + "." + worldName + ".inventory");

        if (inventoryList == null) {
            plugin.log("Kein gespeichertes Inventar gefunden für: '" + playerUUID + "' in Welt: '" + worldName + "'");
            return new ArrayList<>();
        }

        List<ItemStack> loadedItems = new ArrayList<>();
        for (Object itemObject : inventoryList) {
            if (itemObject instanceof ItemStack) {
                ItemStack item = (ItemStack) itemObject;
                if (item.getType() != Material.AIR) { // Überprüfen, ob das Item nicht "AIR" ist
                    loadedItems.add(item);
                    plugin.log("Geladenes Item: '" + item.getType() + "', Menge: '" + item.getAmount() + "'");
                }
            } else {
                plugin.log("Ungültiges Objekt in der Inventarliste: '" + itemObject + "'");
            }
        }
        return loadedItems;
    }


    // Methode zum Speichern der Spielerdaten in die YAML-Datei
    private void savePlayerData() {
        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Fehler beim Speichern der Spielerdaten in playerData.yml.");
            e.printStackTrace();
        }
    }

    public void clearInventoryFromYml(Player player, String worldName) {
        plugin.log("Versuche Inventar aus Config zu löschen");
        String path = player.getUniqueId() + "." + worldName + ".inventory";
        if (playerDataConfig.contains(path)) {
            playerDataConfig.set(path, null);
            try {
                playerDataConfig.save(playerDataFile);
                plugin.log("Inventar gelöscht für: " + player.getName() + " in Welt: " + worldName);
            } catch (IOException e) {
                plugin.getLogger().severe("Fehler beim Löschen in playerData.yml.");
                e.printStackTrace();
            }
        } else {
            plugin.log("Kein Inventar zu löschen für: '" + player.getName() + "' in Welt: '" + worldName + "'");
        }
    }

    public void saveWorldName(UUID playerUUID, String worldName) {
        playerDataConfig.set(playerUUID + ".lastWorld", worldName);
        savePlayerData();
    }

    public String getLastWorldName(UUID playerUUID) {
        return playerDataConfig.getString(playerUUID + ".lastWorld");
    }

    public String getOriginalWorld(UUID playerUUID) {
        for (String worldName : playerDataConfig.getKeys(false)) {
            if (playerDataConfig.contains(playerUUID.toString() + "." + worldName + ".inventory")) {
                return worldName;
            }
        }
        return null;
    }

}
