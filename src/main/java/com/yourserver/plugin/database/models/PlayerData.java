package com.yourserver.plugin.database.models;

import java.util.UUID;
import java.util.Objects;

public class PlayerData {
    private final UUID uuid;
    private final String username;
    private Long telegramId;
    private boolean isAdmin;
    private long registrationDate;
    private String nickname;
    private long playTimeMinutes;

    // Основной конструктор
    public PlayerData(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username != null ? username : "Unknown";
        this.registrationDate = System.currentTimeMillis();
        this.playTimeMinutes = 0;
    }
    
    // Полный конструктор для загрузки из БД
    public PlayerData(UUID uuid, String username, Long telegramId, boolean isAdmin, 
                     long registrationDate, String nickname, long playTimeMinutes) {
        this.uuid = uuid;
        this.username = username != null ? username : "Unknown";
        this.telegramId = telegramId;
        this.isAdmin = isAdmin;
        this.registrationDate = registrationDate;
        this.nickname = nickname;
        this.playTimeMinutes = playTimeMinutes;
    }

    // --- Геттеры ---
    public UUID getUuid() { return uuid; }
    public String getUsername() { return username; }
    public Long getTelegramId() { return telegramId; }
    public boolean isAdmin() { return isAdmin; }
    public long getRegistrationDate() { return registrationDate; }
    public String getNickname() { return nickname != null ? nickname : username; }
    public long getPlayTimeMinutes() { return playTimeMinutes; }

    // --- Сеттеры ---
    public void setTelegramId(Long telegramId) { this.telegramId = telegramId; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setRegistrationDate(long registrationDate) { this.registrationDate = registrationDate; }
    public void setPlayTimeMinutes(long playTimeMinutes) { this.playTimeMinutes = playTimeMinutes; }
    public void addPlayTime(long minutes) { this.playTimeMinutes += minutes; }
    
    // --- Методы Object ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerData that = (PlayerData) o;
        return Objects.equals(uuid, that.uuid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
    
    @Override
    public String toString() {
        return "PlayerData{" +
                "uuid=" + uuid +
                ", username='" + username + '\'' +
                ", telegramId=" + telegramId +
                ", isAdmin=" + isAdmin +
                ", registrationDate=" + registrationDate +
                ", nickname='" + getNickname() + '\'' +
                ", playTimeMinutes=" + playTimeMinutes +
                '}';
    }
}
