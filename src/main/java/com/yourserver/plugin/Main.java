/*
 * AuthTelegram Plugin
 * Copyright (c) 2026 neformsk
 * 
 * ЛИЦЕНЗИЯ:
 * - Бесплатно для личного использования
 * - Запрещено коммерческое использование другими лицами
 * - Автор сохраняет все права на продажу и коммерциализацию
 * 
 * Полный текст: https://github.com/neformsk/AuthTelegramPlugin/blob/main/LICENSE
 */
package com.yourserver.plugin;

import com.yourserver.plugin.database.DatabaseManager;
import com.yourserver.plugin.database.models.PlayerData;
import com.yourserver.plugin.listeners.PlayerListener;
import com.yourserver.plugin.listeners.TabListListener;
import com.yourserver.plugin.telegram.TelegramBot;
import com.yourserver.plugin.utils.LoginConfirmManager;
import com.yourserver.plugin.utils.ProtectionManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;
    private DatabaseManager dbManager;
    private ProtectionManager protectionManager;
    private LoginConfirmManager loginConfirmManager;
    private TelegramBot telegramBot;
    
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        dbManager = new DatabaseManager(this);
        protectionManager = new ProtectionManager(this);
        loginConfirmManager = new LoginConfirmManager(this);
        dbManager.init();
        
        String token = getConfig().getString("telegram.token");
        String botName = getConfig().getString("telegram.bot-name", "YourServerBot");
        
        if (token != null && !token.equals("ВАШ_BOT_TOKEN_ЗДЕСЬ") && !token.trim().isEmpty()) {
            try {
                telegramBot = new TelegramBot(token, botName, this);
                getLogger().info("Telegram бот @" + botName + " запущен!");
            } catch (Exception e) {
                getLogger().severe("Ошибка создания Telegram бота: " + e.getMessage());
            }
        } else {
            getLogger().warning("Токен Telegram бота не настроен в config.yml!");
        }
        
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new TabListListener(this), this);
        
        getLogger().info("Плагин TelegramAuth успешно запущен!");
    }
    
    @Override
    public void onDisable() {
        if (telegramBot != null) {
            telegramBot.shutdown();
        }
        if (dbManager != null) {
            dbManager.close();
        }
        getLogger().info("Плагин TelegramAuth выключен.");
    }
    
    public static Main getInstance() {
        return instance;
    }
    
    public DatabaseManager getDatabaseManager() {
        return dbManager;
    }
    
    public TelegramBot getTelegramBot() {
        return telegramBot;
    }
    
    public ProtectionManager getProtectionManager() {
        return protectionManager;
    }
    
    public LoginConfirmManager getLoginConfirmManager() {
        return loginConfirmManager;
    }
}
