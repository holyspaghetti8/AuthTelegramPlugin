package com.yourserver.plugin.utils;

import com.yourserver.plugin.Main;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProtectionManager {
    private final Main plugin;
    private final Set<UUID> protectedPlayers = new HashSet<>();
    
    public ProtectionManager(Main plugin) {
        this.plugin = plugin;
    }
    
    public void protectPlayer(Player player) {
        protectedPlayers.add(player.getUniqueId());
        player.setInvulnerable(true);
        player.setGameMode(GameMode.ADVENTURE);
        player.setCanPickupItems(false);
        player.setAllowFlight(false);
        player.setFlying(false);
        
        // Можно также скрыть игрока от других, если нужно
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (!online.equals(player)) {
                online.hidePlayer(plugin, player);
            }
        }
        plugin.getLogger().info("Включена защита для игрока: " + player.getName());
    }
    
    public void unprotectPlayer(Player player) {
        protectedPlayers.remove(player.getUniqueId());
        player.setInvulnerable(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.setCanPickupItems(true);
        
        // Показываем игрока другим
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }
        plugin.getLogger().info("Защита снята для игрока: " + player.getName());
    }
    
    public boolean isProtected(UUID uuid) {
        return protectedPlayers.contains(uuid);
    }
}
