package ch.ksrminecraft.dynamiterenewal.Utils;

import ch.ksrminecraft.dynamiterenewal.DynamiteRenewal;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InventoryManager {

    private final File playerDataFile;
    private final FileConfiguration playerDataConfig;
    private final DynamiteRenewal plugin;
    private MVWorldManager mvWorldManager;

    public InventoryManager(DynamiteRenewal plugin) {
        this.plugin = plugin;
        this.playerDataFile = new File(plugin.getDataFolder(), "playerData.yml");
        this.playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
        MultiverseCore mvCore = (MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
        if (mvCore == null) {
            throw new IllegalStateException("Multiverse-Core ist nicht installiert oder nicht geladen.");
        }
        this.mvWorldManager = mvCore.getMVWorldManager();
    }

    public void processInvetory(Player player, String worldName) {
        clearInventory(player);
        deploySpecialInventory(player);
        setMVGameMode(player,worldName);
        setSpawnPoint(player, worldName);

    }

    public void clearInventory(Player player) {
        player.getInventory().clear();
        plugin.log("Inventar von Spieler '" + player.getName() + "' geleert.");
    }

    public void saveInventory(Player player, String worldName) {
        plugin.getPlayerDataManager().saveInventory(player, worldName);

    }

    public void restoreInventory(Player player, String worldName) {
        plugin.log("Stelle Inventar aus Config für '" + player.getName() + "' wieder her (" + worldName + ")");
        List<ItemStack> savedInventory = plugin.getPlayerDataManager().loadInventory(player.getUniqueId(), worldName);
        StringBuilder loadedItemsLog = new StringBuilder("Geladene Items: ");
        for (ItemStack item : savedInventory) {
            if (item != null) {
                player.getInventory().addItem(item);
                loadedItemsLog.append(item.getType().toString()).append(", ");
                plugin.log("Item zurückgeladen: " + item.getType());
            }
        }
        plugin.log(loadedItemsLog.toString());
    }

    private void deploySpecialInventory(Player player) {
        ConfigurationSection itemsSection = plugin.getConfig().getConfigurationSection("startingItems");
        if (itemsSection != null) {
            plugin.log("ItemSection in Config gefunden. Starte Ausrüstungsvorgang.");
            for (String key : itemsSection.getKeys(false)) {
                if (itemsSection.isList(key)) {
                    List<Map<?, ?>> itemList = itemsSection.getMapList(key);
                    for (Map<?, ?> itemMap : itemList) {
                        equipItem(player, itemMap);
                    }
                } else {
                    Map<?, ?> itemMap = itemsSection.getConfigurationSection(key).getValues(false);
                    equipItem(player, itemMap);
                }
            }
        }
    }

    private void setMVGameMode(Player player, String worldName) {
        if (mvWorldManager.getMVWorld(worldName) != null) {
            String gameModeString = plugin.getConfig().getString("defaultGameMode", "SURVIVAL").toUpperCase();
            try {
                GameMode gameMode = GameMode.valueOf(gameModeString);
                mvWorldManager.getMVWorld(worldName).setGameMode(gameMode);
                player.setGameMode(gameMode); // Setzt auch den Spielmodus des Spielers
                plugin.log("Spielmodus für Welt '" + worldName + "' auf " + gameModeString + " gesetzt.");
            } catch (IllegalArgumentException e) {
                plugin.log("Ungültiger Spielmodus in Konfiguration. Verwende Standardmodus SURVIVAL.");
                mvWorldManager.getMVWorld(worldName).setGameMode(GameMode.SURVIVAL);
                player.setGameMode(GameMode.SURVIVAL); // Setzt den Spielmodus des Spielers auf SURVIVAL
            }
        }
    }

    private void equipItem(Player player, Map<?, ?> itemMap) {
        Material material = Material.valueOf((String) itemMap.get("material"));
        Object amountObject = itemMap.get("amount");
        int amount = amountObject != null ? (int) amountObject : 1;
        ItemStack item = new ItemStack(material, amount);

        // Hinzufügen von Verzauberungen, falls vorhanden
        if (itemMap.containsKey("enchantments")) {
            List<Map<?, ?>> enchantmentList = (List<Map<?, ?>>) itemMap.get("enchantments");
            for (Map<?, ?> enchantmentMap : enchantmentList) {
                Enchantment enchantment = Enchantment.getByName((String) enchantmentMap.get("enchantment"));
                int level = (int) enchantmentMap.get("level");
                item.addUnsafeEnchantment(enchantment, level);
            }
        }

        // Rüstungsgegenstände direkt anlegen
        PlayerInventory inventory = player.getInventory();
        switch (material) {
            case NETHERITE_HELMET:
            case DIAMOND_HELMET:
            case IRON_HELMET:
            case GOLDEN_HELMET:
            case LEATHER_HELMET:
                inventory.setHelmet(item);
                break;
            case NETHERITE_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case IRON_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case LEATHER_CHESTPLATE:
                inventory.setChestplate(item);
                break;
            case NETHERITE_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case IRON_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case LEATHER_LEGGINGS:
                inventory.setLeggings(item);
                break;
            case NETHERITE_BOOTS:
            case DIAMOND_BOOTS:
            case IRON_BOOTS:
            case GOLDEN_BOOTS:
            case LEATHER_BOOTS:
                inventory.setBoots(item);
                break;
            default:
                inventory.addItem(item); // Normale Items zum Inventar hinzufügen
                break;
        }
    }

    private Location getSpawnpoint(String worldName) {
        ConfigurationSection worldSpawnSection = plugin.getConfig().getConfigurationSection("spawnpoints." + worldName);
        if (worldSpawnSection != null) {
            // Direktes Abrufen der Werte und Erstellen der Location
            return new Location(plugin.getServer().getWorld(worldName),
                    worldSpawnSection.getDouble("x"),
                    worldSpawnSection.getDouble("y"),
                    worldSpawnSection.getDouble("z"),
                    (float) worldSpawnSection.getDouble("yaw"),
                    (float) worldSpawnSection.getDouble("pitch"));
        }
        return null;
    }

    private void setSpawnPoint(Player player, String worldName) {
        Location spawnLocation = getSpawnpoint(worldName);
        if (spawnLocation == null) {
            plugin.log("Kein spezifischer Spawnpoint in '" + worldName + "' gefunden. Verwende Standard-Spawn.");
            spawnLocation = plugin.getServer().getWorld(worldName).getSpawnLocation();
        }
        player.teleport(spawnLocation);
        plugin.log("Spieler zu Spawnpoint in '" + worldName + "' teleportiert.");
    }
}
