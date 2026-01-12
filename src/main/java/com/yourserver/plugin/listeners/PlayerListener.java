package com.yourserver.plugin.listeners;

import com.yourserver.plugin.Main;
import com.yourserver.plugin.database.models.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {
    private final Main plugin;
    private final Map<UUID, Location> entryLocations = new HashMap<>();
    
    public PlayerListener(Main plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        PlayerData data = plugin.getDatabaseManager().getPlayer(uuid);
        
        if (data == null) {
            data = new PlayerData(uuid, player.getName());
            plugin.getDatabaseManager().savePlayer(data);
            plugin.getLogger().info("Создана новая запись для игрока: " + player.getName());
        }
        
        if (player.isOp() && !data.isAdmin()) {
            data.setAdmin(true);
            plugin.getDatabaseManager().savePlayer(data);
            plugin.getLogger().info("Игрок " + player.getName() + " (OP) назначен администратором плагина.");
        }
        
        if (data.getTelegramId() == null) {
            plugin.getProtectionManager().protectPlayer(player);
            entryLocations.put(uuid, player.getLocation().clone());
            
            player.sendTitle("§c§lТРЕБУЕТСЯ РЕГИСТРАЦИЯ", "§7Используйте Telegram-бота", 10, 70, 10);
            
            player.sendMessage("§6═══════════════════════════════");
            player.sendMessage("§c⚠ §fДля игры необходимо зарегистрироваться!");
            player.sendMessage("§7Найдите нашего бота в Telegram:");
            player.sendMessage("§a@" + plugin.getConfig().getString("telegram.bot-name", "YourServerBot"));
            player.sendMessage("§7И отправьте команду:");
            player.sendMessage("§e/register " + player.getName());
            player.sendMessage("§6═══════════════════════════════");
            player.sendMessage("§6═══════════════════════════════");
            player.sendMessage("§c⚠ §fДля игры необходимо зарегистрироваться!");
            player.sendMessage("§7Найдите нашего бота в Telegram:");
            player.sendMessage("§a@" + plugin.getConfig().getString("telegram.bot-name", "YourServerBot"));
            player.sendMessage("§7И отправьте команду:");
            player.sendMessage("§e/register " + player.getName());
            player.sendMessage("§6═══════════════════════════════");
            player.sendMessage("§6═══════════════════════════════");
            player.sendMessage("§c⚠ §fДля игры необходимо зарегистрироваться!");
            player.sendMessage("§7Найдите нашего бота в Telegram:");
            player.sendMessage("§a@" + plugin.getConfig().getString("telegram.bot-name", "YourServerBot"));
            player.sendMessage("§7И отправьте команду:");
            player.sendMessage("§e/register " + player.getName());
            player.sendMessage("§6═══════════════════════════════");
            player.sendMessage("§6═══════════════════════════════");
            player.sendMessage("§c⚠ §fДля игры необходимо зарегистрироваться!");
            player.sendMessage("§7Найдите нашего бота в Telegram:");
            player.sendMessage("§a@" + plugin.getConfig().getString("telegram.bot-name", "YourServerBot"));
            player.sendMessage("§7И отправьте команду:");
            player.sendMessage("§e/register " + player.getName());
            player.sendMessage("§6═══════════════════════════════");
            
            if (data.isAdmin()) {
                player.sendMessage("§6[ADMIN] §fВы администратор, но всё равно должны зарегистрироваться через бота!");
            }
            
            plugin.getLogger().info("Игрок " + player.getName() + " требует регистрации (Telegram ID не привязан)");
        } else {
            plugin.getProtectionManager().protectPlayer(player);
            entryLocations.put(uuid, player.getLocation().clone());
            
            plugin.getLoginConfirmManager().startConfirmation(player, data.getTelegramId());
            
            player.sendMessage("§6═══════════════════════════════");
            player.sendMessage("§e🔐 Требуется подтверждение входа");
            player.sendMessage("§fЗапрос отправлен в ваш Telegram-аккаунт.");
            player.sendMessage("§fУ вас есть 60 секунд для подтверждения.");
            player.sendMessage("§6═══════════════════════════════");
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (plugin.getProtectionManager().isProtected(uuid) && entryLocations.containsKey(uuid)) {
            Location from = event.getFrom();
            Location to = event.getTo();
            Location spawnPoint = entryLocations.get(uuid);
            
            if (to != null && (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ())) {
                player.teleport(spawnPoint);
                player.sendMessage("§cВы не можете ходить до завершения регистрации/подтверждения!");
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        entryLocations.remove(uuid);
        plugin.getProtectionManager().unprotectPlayer(player);
        plugin.getLoginConfirmManager().cancelConfirmation(uuid);
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (plugin.getProtectionManager().isProtected(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§cВы не можете ломать блоки до завершения регистрации/подтверждения!");
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (plugin.getProtectionManager().isProtected(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§cВы не можете ставить блоки до завершения регистрации/подтверждения!");
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (plugin.getProtectionManager().isProtected(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            if (plugin.getProtectionManager().isProtected(damager.getUniqueId())) {
                event.setCancelled(true);
                damager.sendMessage("§cВы не можете атаковать до завершения регистрации/подтверждения!");
            }
        }
    }
    
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            if (plugin.getProtectionManager().isProtected(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage("§cВы не можете открывать контейнеры до завершения регистрации/подтверждения!");
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (plugin.getProtectionManager().isProtected(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
