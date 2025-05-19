package ch.ksrminecraft.dynamiterenewal.Utils;

import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.List;

public class PlayerWorldChangeManager {

    // Überprüft, ob ein Spieler in eine Dynamit-Welt eintritt
    public boolean isEntering(String enteringWorld, String leftingWorld, List<String> dynamiteWorlds) {
        return dynamiteWorlds.contains(enteringWorld) && (!dynamiteWorlds.contains(leftingWorld));
    }

    // Überprüft, ob ein Spieler eine Dynamit-Welt verlässt
    public boolean isLeaving(String enteringWorld, String leftingWorld, List<String> dynamiteWorlds) {
        return dynamiteWorlds.contains(leftingWorld) && (!dynamiteWorlds.contains(enteringWorld));
    }

    // Überprüft, ob eine Welt in der Exception-Liste steht
    public boolean isExceptionWorld(String world, List<String> exceptionWorlds) {
        return exceptionWorlds.contains(world);
    }

    public boolean isInsideChange(String enteringWorld, String leftingWorld, List<String> dynamiteWorlds) {
        return dynamiteWorlds.contains(enteringWorld) && dynamiteWorlds.contains(leftingWorld);
    }

}
