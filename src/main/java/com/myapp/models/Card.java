package com.myapp.models;

public class Card {
    private int id;
    private String value;
    private boolean matched;
    private Theme theme;

    public Card() {
        this.matched = false;
    }

    public Card(int id, String value) {
        this.id = id;
        this.value = value;
        this.matched = false;
    }

    public Card(int id, String value, Theme theme) {
        this.id = id;
        this.value = value;
        this.matched = false;
        this.setTheme(theme);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        if (this.theme != null && this.theme.getCards().contains(this)) {
            this.theme.getCards().remove(this);
        }
        
        this.theme = theme;
        
        if (theme != null && !theme.getCards().contains(this)) {
            theme.getCards().add(this);
        }
    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", value='" + value + '\'' +
                ", matched=" + matched +
                ", theme=" + (theme != null ? theme.getName() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return id == card.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}