package com.yourserver.plugin.database;

import com.yourserver.plugin.database.models.PlayerData;
import com.yourserver.plugin.Main;
import com.yourserver.plugin.database.models.PlayerData;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {
    private final Main plugin;
    private Connection connection;
    
    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
    }
    
    public void init() {
        try {
            String url = "jdbc:sqlite:" + plugin.getDataFolder() + "/data.db";
            connection = DriverManager.getConnection(url);
            
            String sql = "CREATE TABLE IF NOT EXISTS players (" +
                "uuid TEXT PRIMARY KEY," +
                "username TEXT NOT NULL," +
                "telegram_id INTEGER," +
                "is_admin BOOLEAN DEFAULT 0," +
                "reg_date BIGINT," +
                "nickname TEXT," +
                "play_time BIGINT DEFAULT 0" +
            ");";
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
                plugin.getLogger().info("База данных инициализирована");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка инициализации БД: " + e.getMessage());
        }
    }
    
    // Упрощённый метод поиска по Telegram ID
    public PlayerData getPlayerByTelegramId(long telegramId) {
        String sql = "SELECT * FROM players WHERE telegram_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, telegramId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return resultSetToPlayerData(rs);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка getPlayerByTelegramId: " + e.getMessage());
        }
        return null;
    }
    
    // Основной метод получения данных игрока
    public PlayerData getPlayer(UUID uuid) {
        String sql = "SELECT * FROM players WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return resultSetToPlayerData(rs);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка getPlayer: " + e.getMessage());
        }
        return null;
    }
    
    // Вспомогательный метод преобразования ResultSet в PlayerData
    private PlayerData resultSetToPlayerData(ResultSet rs) throws SQLException {
        UUID uuid = UUID.fromString(rs.getString("uuid"));
        String username = rs.getString("username");
        Long telegramId = (Long) rs.getObject("telegram_id");
        boolean isAdmin = rs.getBoolean("is_admin");
        long regDate = rs.getLong("reg_date");
        String nickname = rs.getString("nickname");
        long playTime = rs.getLong("play_time");
        
        PlayerData data = new PlayerData(uuid, username, telegramId, isAdmin, regDate, nickname, playTime);
        return data;
    }
    
    // Метод сохранения игрока
    public void savePlayer(PlayerData data) {
        String sql = "INSERT OR REPLACE INTO players (uuid, username, telegram_id, is_admin, reg_date, nickname, play_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, data.getUuid().toString());
            pstmt.setString(2, data.getUsername());
            
            if (data.getTelegramId() != null) {
                pstmt.setLong(3, data.getTelegramId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            
            pstmt.setBoolean(4, data.isAdmin());
            pstmt.setLong(5, data.getRegistrationDate());
            pstmt.setString(6, data.getNickname());
            pstmt.setLong(7, data.getPlayTimeMinutes());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка сохранения игрока: " + e.getMessage());
        }
    }
    
    // Метод для обновления времени игры
    public void updatePlayTime(UUID uuid, long additionalMinutes) {
        String sql = "UPDATE players SET play_time = play_time + ? WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, additionalMinutes);
            pstmt.setString(2, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка обновления времени игры: " + e.getMessage());
        }
    }

    public boolean isTelegramAlreadyUsed(long telegramId) {
    String sql = "SELECT COUNT(*) as count FROM players WHERE telegram_id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setLong(1, telegramId);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            return rs.getInt("count") > 0;
        }
    } catch (SQLException e) {
        plugin.getLogger().severe("Ошибка проверки Telegram: " + e.getMessage());
    }
    return false;
}

public boolean isPlayerAlreadyRegistered(UUID playerUuid) {
    String sql = "SELECT telegram_id FROM players WHERE uuid = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setString(1, playerUuid.toString());
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            return rs.getObject("telegram_id") != null;
        }
    } catch (SQLException e) {
        plugin.getLogger().severe("Ошибка проверки игрока: " + e.getMessage());
    }
    return false;
}
    
    public Connection getConnection() {
        return connection;
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка закрытия соединения БД: " + e.getMessage());
        }
    }
}
