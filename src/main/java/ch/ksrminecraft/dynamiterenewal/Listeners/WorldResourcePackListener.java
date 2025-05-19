package ch.ksrminecraft.dynamiterenewal.Listeners;

import ch.ksrminecraft.dynamiterenewal.DynamiteRenewal;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.List;

public class WorldResourcePackListener implements Listener {

    private DynamiteRenewal plugin;

    public WorldResourcePackListener(DynamiteRenewal plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        String currentWorldName = event.getPlayer().getWorld().getName();
        List<String> dynamiteWorlds = plugin.getConfig().getStringList("dynamiteWorlds");
        String resourcePackUrl = plugin.getConfig().getString("resourcePackUrl", "");
        String resourcePackHash = plugin.getConfig().getString("resourcePackHash", "");
        String alternateResourcePackUrl = plugin.getConfig().getString("alternateResourcePackUrl", "");
        String alternateResourcePackHash = plugin.getConfig().getString("alternateResourcePackHash", "");

        plugin.log("ResPack: PlayerChangedWorldEvent ausgelöst für Spieler: " + event.getPlayer().getName());

        if (dynamiteWorlds.contains(currentWorldName)) {
            plugin.log("Welt '" + currentWorldName + "' ist in der Liste der DynamiteWelten.");

            if (!resourcePackUrl.isEmpty() && !resourcePackHash.isEmpty()) {
                plugin.log("Ressourcenpaket URL und Hash gefunden. Versuche, das Paket zu laden: " + resourcePackUrl);
                event.getPlayer().setResourcePack(resourcePackUrl, resourcePackHash);
            } else if (!alternateResourcePackUrl.isEmpty() && !alternateResourcePackHash.isEmpty()) {
                plugin.log("Alternative Ressourcenpaket URL und Hash gefunden. Versuche, das Paket zu laden: " + alternateResourcePackUrl);
                event.getPlayer().setResourcePack(alternateResourcePackUrl, alternateResourcePackHash);
            } else {
                plugin.log("Keine gültige URL oder Hash für das Ressourcenpaket gefunden. Paket wird nicht geladen.");
            }
        } else {
            plugin.log("Welt '" + currentWorldName + "' ist nicht in der Liste der DynamiteWelten.");
        }
    }

}
