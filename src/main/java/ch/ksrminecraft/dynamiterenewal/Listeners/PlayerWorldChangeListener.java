package ch.ksrminecraft.dynamiterenewal.Listeners;

import ch.ksrminecraft.dynamiterenewal.DynamiteRenewal;
import ch.ksrminecraft.dynamiterenewal.Utils.InventoryManager;
import ch.ksrminecraft.dynamiterenewal.Utils.PlayerWorldChangeManager;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.List;

public class PlayerWorldChangeListener implements Listener {

    private DynamiteRenewal plugin;
    private MVWorldManager mvWorldManager;
    private PlayerWorldChangeManager worldChangeManager;
    private InventoryManager inventoryManager;

    public PlayerWorldChangeListener(DynamiteRenewal plugin) {
        this.plugin = plugin;
        MultiverseCore mvCore = (MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
        if (mvCore == null) {
            throw new IllegalStateException("Multiverse-Core ist nicht installiert oder nicht geladen.");
        }
        this.mvWorldManager = mvCore.getMVWorldManager();
        this.worldChangeManager = new PlayerWorldChangeManager();
        this.inventoryManager = new InventoryManager(plugin);
    }

    @EventHandler
    public void onPlayerEnterWorld(PlayerChangedWorldEvent event) {

        Player player = event.getPlayer();
        String enteringWorldName = player.getWorld().getName();
        String leftingWorldName = event.getFrom().getName();
        List<String> dynamiteWorlds = plugin.getConfig().getStringList("dynamiteWorlds");
        List<String> exceptionWorlds = plugin.getConfig().getStringList("exceptionWorlds");

        if (worldChangeManager.isEntering(enteringWorldName, leftingWorldName, dynamiteWorlds)) {
            plugin.log("Entry detected: '" + leftingWorldName +  "' -> '" + enteringWorldName + "'");
            plugin.setRestoreRequired(true); // setzt einen allfälligen Restore auf true
            plugin.log("Setzte restoreRequired auf true - Wert neu: " + plugin.isRestoreRequired());
            if (worldChangeManager.isExceptionWorld(leftingWorldName, exceptionWorlds)) {
                plugin.log("Entry from an exceptionWorld");
                inventoryManager.processInvetory(player, enteringWorldName);

            } else {
                plugin.log("Regular Entry");
                inventoryManager.saveInventory(player, leftingWorldName);
                inventoryManager.processInvetory(player, enteringWorldName);
            }

        } else {
            if (worldChangeManager.isLeaving(enteringWorldName, leftingWorldName, dynamiteWorlds)) {
                plugin.log("Leaving detected: '" + leftingWorldName +  "' -> '" + enteringWorldName + "'");
                inventoryManager.clearInventory(player);
                if (worldChangeManager.isExceptionWorld(enteringWorldName, exceptionWorlds)) {
                    plugin.log("Destination world is an exceptionWorld");
                } else {
                    plugin.log("Regular Leaving");
                    inventoryManager.restoreInventory(player, enteringWorldName);
                    plugin.getPlayerDataManager().clearInventoryFromYml(player, enteringWorldName);
                }

            } else {
                if (worldChangeManager.isInsideChange(enteringWorldName, leftingWorldName, dynamiteWorlds)) {
                    plugin.log("Inside worldchange detected: '"  + leftingWorldName +  "' -> '" + enteringWorldName + "'");
                    inventoryManager.processInvetory(player, enteringWorldName);
                } else {
                    plugin.log("Outsie worldchange detected: '"  + leftingWorldName +  "' -> '" + enteringWorldName + "'");
                }
            }
        }
    }
}
