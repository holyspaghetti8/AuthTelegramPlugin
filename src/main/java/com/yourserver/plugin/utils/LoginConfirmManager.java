package com.yourserver.plugin.utils;

import com.yourserver.plugin.Main;
import com.yourserver.plugin.database.models.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginConfirmManager {
    private final Main plugin;
    private final Map<UUID, PendingLogin> pendingLogins = new HashMap<>();
    
    private static class PendingLogin {
        Player player;
        long telegramId;
        BukkitTask timeoutTask;
        
        PendingLogin(Player player, long telegramId, BukkitTask timeoutTask) {
            this.player = player;
            this.telegramId = telegramId;
            this.timeoutTask = timeoutTask;
        }
    }
    
    public LoginConfirmManager(Main plugin) {
        this.plugin = plugin;
    }
    
    public void startConfirmation(Player player, long telegramId) {
        UUID uuid = player.getUniqueId();
        
        cancelConfirmation(uuid);
        
        BukkitTask timeoutTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (pendingLogins.containsKey(uuid)) {
                    player.kickPlayer("§cВремя подтверждения истекло. Попробуйте снова.");
                    pendingLogins.remove(uuid);
                    plugin.getLogger().info("Таймаут подтверждения для игрока: " + player.getName());
                }
            });
        }, 20L * 60);
        
        pendingLogins.put(uuid, new PendingLogin(player, telegramId, timeoutTask));
        
        plugin.getTelegramBot().sendLoginConfirmation(telegramId, player);
        
        player.sendMessage("§6[TelegramAuth] §fОжидается подтверждение входа в вашем Telegram-аккаунте...");
        player.sendTitle("§6Ожидание подтверждения", "§fПроверьте Telegram", 10, 100, 10);
        
        plugin.getLogger().info("Запрос подтверждения отправлен для игрока: " + player.getName() + " (Telegram ID: " + telegramId + ")");
    }
    
    public void handleConfirmation(UUID playerUuid, boolean approved, String telegramUsername) {
        PendingLogin pending = pendingLogins.remove(playerUuid);
        if (pending == null) return;
        
        pending.timeoutTask.cancel();
        
        Player player = pending.player;
        if (player == null || !player.isOnline()) return;
        
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (approved) {
                plugin.getProtectionManager().unprotectPlayer(player);
                player.sendMessage("§a✅ Вход подтверждён пользователем @" + telegramUsername + "! Добро пожаловать!");
                player.sendTitle("§a§lВХОД ПОДТВЕРЖДЁН", "§fДобро пожаловать!", 10, 70, 10);
                
                PlayerData data = plugin.getDatabaseManager().getPlayer(player.getUniqueId());
                if (data != null && data.isAdmin() && !player.isOp()) {
                    player.setOp(true);
                    player.sendMessage("§6[ADMIN] §fВаши права администратора активированы.");
                }
                
                plugin.getLogger().info("Вход подтверждён для игрока: " + player.getName() + " (от @" + telegramUsername + ")");
            } else {
                player.kickPlayer("§c❌ Вход отклонён владельцем Telegram-аккаунта.");
                plugin.getLogger().info("Вход отклонён для игрока: " + player.getName() + " (от @" + telegramUsername + ")");
            }
        });
    }
    
    public void cancelConfirmation(UUID playerUuid) {
        PendingLogin pending = pendingLogins.remove(playerUuid);
        if (pending != null) {
            pending.timeoutTask.cancel();
        }
    }
    
    public boolean isPendingConfirmation(UUID playerUuid) {
        return pendingLogins.containsKey(playerUuid);
    }
    
    public Long getPendingTelegramId(UUID playerUuid) {
        PendingLogin pending = pendingLogins.get(playerUuid);
        return pending != null ? pending.telegramId : null;
    }
}
