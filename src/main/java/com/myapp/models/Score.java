package com.myapp.models;

import java.time.LocalDateTime;

public class Score {
    private int id;
    private User user;
    private Theme theme;
    private int attempts;
    private int timeSeconds;
    private LocalDateTime playedAt;

    public Score() {
        this.playedAt = LocalDateTime.now();
    }

    public Score(User user, Theme theme, int attempts, int timeSeconds) {
        this.setUser(user);
        this.setTheme(theme);
        this.attempts = attempts;
        this.timeSeconds = timeSeconds;
        this.playedAt = LocalDateTime.now();
    }

    public Score(int id, User user, Theme theme, int attempts, int timeSeconds, LocalDateTime playedAt) {
        this.id = id;
        this.setUser(user);
        this.setTheme(theme);
        this.attempts = attempts;
        this.timeSeconds = timeSeconds;
        this.playedAt = playedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() { 
        return user; 
    }
    
    public void setUser(User user) { 
        if (this.user != null && this.user.getScores().contains(this)) {
            this.user.getScores().remove(this);
        }
        
        this.user = user; 
        
        if (user != null && !user.getScores().contains(this)) {
            user.getScores().add(this);
        }
    }

    public Theme getTheme() { 
        return theme; 
    }
    
    public void setTheme(Theme theme) { 
        if (this.theme != null && this.theme.getScores().contains(this)) {
            this.theme.getScores().remove(this);
        }
        
        this.theme = theme; 
        
        if (theme != null && !theme.getScores().contains(this)) {
            theme.getScores().add(this);
        }
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public int getTimeSeconds() {
        return timeSeconds;
    }

    public void setTimeSeconds(int timeSeconds) {
        this.timeSeconds = timeSeconds;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(LocalDateTime playedAt) {
        this.playedAt = playedAt;
    }

    @Override
    public String toString() {
        return "Score{" +
                "id=" + id +
                ", user=" + (user != null ? user.getFullName() : "null") +
                ", theme=" + (theme != null ? theme.getName() : "null") +
                ", attempts=" + attempts +
                ", timeSeconds=" + timeSeconds +
                ", playedAt=" + playedAt +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Score score = (Score) obj;
        return id == score.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}