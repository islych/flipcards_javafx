package com.myapp.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String username;
    private String passwordHash;
    private String email;
    private boolean isActive;
    private String role;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Score> scores;

    public User() {
        this.scores = new ArrayList<>();
        this.isActive = true;
        this.role = "USER";
        this.createdAt = LocalDateTime.now();
    }

    public User(int id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.scores = new ArrayList<>();
        this.isActive = true;
        this.role = "USER";
        this.createdAt = LocalDateTime.now();
    }

    public User(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.scores = new ArrayList<>();
        this.isActive = true;
        this.role = "USER";
        this.createdAt = LocalDateTime.now();
    }

    public User(String firstName, String lastName, String username, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.scores = new ArrayList<>();
        this.isActive = true;
        this.role = "USER";
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { 
        return id; 
    }
    
    public void setId(int id) { 
        this.id = id; 
    }

    public String getFirstName() { 
        return firstName; 
    }
    
    public void setFirstName(String firstName) { 
        this.firstName = firstName; 
    }

    public String getLastName() { 
        return lastName; 
    }
    
    public void setLastName(String lastName) { 
        this.lastName = lastName; 
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isUser() {
        return "USER".equalsIgnoreCase(role);
    }

    public List<Score> getScores() {
        return scores;
    }

    public void setScores(List<Score> scores) {
        this.scores = scores != null ? scores : new ArrayList<>();
    }

    public void addScore(Score score) {
        if (score != null && !this.scores.contains(score)) {
            this.scores.add(score);
            score.setUser(this);
        }
    }

    public void removeScore(Score score) {
        if (score != null && this.scores.contains(score)) {
            this.scores.remove(score);
            score.setUser(null);
        }
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public boolean hasUsername() {
        return username != null && !username.trim().isEmpty();
    }

    public boolean hasPassword() {
        return passwordHash != null && !passwordHash.trim().isEmpty();
    }

    @Override
    public String toString() {
        if (hasUsername()) {
            return getFullName() + " (" + username + ")";
        }
        return getFullName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}