package com.myapp.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Theme {
    private int id;
    private String name;
    private String imagePath;
    private String description;
    private boolean isActive;
    private int createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Score> scores;
    private List<Card> cards;

    public Theme() {
        this.scores = new ArrayList<>();
        this.cards = new ArrayList<>();
        this.isActive = true;
    }

    public Theme(int id, String name) {
        this.id = id;
        this.name = name;
        this.scores = new ArrayList<>();
        this.cards = new ArrayList<>();
        this.isActive = true;
    }

    public Theme(String name) {
        this.name = name;
        this.scores = new ArrayList<>();
        this.cards = new ArrayList<>();
        this.isActive = true;
    }

    public Theme(String name, String imagePath, String description) {
        this.name = name;
        this.imagePath = imagePath;
        this.description = description;
        this.scores = new ArrayList<>();
        this.cards = new ArrayList<>();
        this.isActive = true;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
            score.setTheme(this);
        }
    }

    public void removeScore(Score score) {
        if (score != null && this.scores.contains(score)) {
            this.scores.remove(score);
            score.setTheme(null);
        }
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards != null ? cards : new ArrayList<>();
        for (Card card : this.cards) {
            card.setTheme(this);
        }
    }

    public void addCard(Card card) {
        if (card != null && !this.cards.contains(card)) {
            this.cards.add(card);
            card.setTheme(this);
        }
    }

    public void removeCard(Card card) {
        if (card != null && this.cards.contains(card)) {
            this.cards.remove(card);
            card.setTheme(null);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Theme theme = (Theme) obj;
        return id == theme.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}