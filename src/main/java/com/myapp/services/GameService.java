package com.myapp.services;

import com.myapp.models.Card;
import com.myapp.models.Theme;

import java.util.*;
import java.util.stream.Collectors;

/**
 * GameService contient la logique principale du jeu Memory : génération du deck, 
 * logique de retournement/vérification, suivi des tentatives et du temps.
 * Mis à jour pour respecter les relations UML Card-Theme.
 */
public class GameService {
    private List<Card> deck = new ArrayList<>();
    private Card firstSelected = null;
    private int attempts = 0;
    private long startTime = 0L;
    private Theme currentTheme; // Thème actuel du jeu

    /**
     * Démarre un nouveau jeu avec les valeurs spécifiées et le thème donné
     */
    public void startNewGame(List<String> values, Theme theme) {
        this.currentTheme = theme;
        deck.clear();
        List<String> pairedValues = new ArrayList<>();

        // Pour chaque valeur, on ajoute DEUX cartes (une paire)
        for (String v : values) {
            pairedValues.add(v); // Première carte de la paire
            pairedValues.add(v); // Deuxième carte de la paire
        }

        // On mélange TOUTES les cartes ensemble
        Collections.shuffle(pairedValues);

        // Création des cartes avec des IDs uniques et association au thème
        int id = 0;
        for (String v : pairedValues) {
            Card card = new Card(id++, v, theme);
            deck.add(card);
        }

        attempts = 0;
        firstSelected = null;
        startTime = System.currentTimeMillis();
    }

    /**
     * Version de compatibilité pour l'ancien code (sans thème explicite)
     */
    public void startNewGame(List<String> values) {
        // Créer un thème par défaut si aucun n'est fourni
        Theme defaultTheme = new Theme(0, "Default");
        startNewGame(values, defaultTheme);
    }

    public List<Card> getDeck() { 
        return Collections.unmodifiableList(deck); 
    }

    public int getAttempts() { 
        return attempts; 
    }

    public int getElapsedSeconds() {
        if (startTime == 0L) return 0;
        return (int)((System.currentTimeMillis() - startTime) / 1000);
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Retourne une carte par index. Retourne : 0 = pas encore de correspondance (attente de la seconde), 
     * 1 = paire trouvée, -1 = pas de correspondance.
     */
    public int flipCard(int cardIndex) {
        if (cardIndex < 0 || cardIndex >= deck.size()) return 0;
        Card c = deck.get(cardIndex);
        if (c.isMatched()) return 0; // déjà appariée
        
        if (firstSelected == null) {
            firstSelected = c;
            return 0;
        } else if (firstSelected.getId() == c.getId()) {
            // même carte cliquée deux fois
            return 0;
        } else {
            attempts++;
            if (firstSelected.getValue().equals(c.getValue())) {
                // correspondance trouvée
                firstSelected.setMatched(true);
                c.setMatched(true);
                firstSelected = null;
                return 1;
            } else {
                // pas de correspondance
                firstSelected = null;
                return -1;
            }
        }
    }

    public boolean isFinished() {
        return deck.stream().allMatch(Card::isMatched);
    }

    /**
     * Méthode utilitaire pour construire des valeurs numériques si pas d'images
     */
    public static List<String> buildNumericValues(int pairCount) {
        return java.util.stream.IntStream.rangeClosed(1, pairCount)
                .mapToObj(Integer::toString)
                .collect(Collectors.toList());
    }

    /**
     * Crée un deck de cartes pour un thème donné avec les valeurs spécifiées
     */
    public static List<Card> createDeckForTheme(Theme theme, List<String> values) {
        List<Card> cards = new ArrayList<>();
        List<String> pairedValues = new ArrayList<>();

        // Créer les paires
        for (String value : values) {
            pairedValues.add(value);
            pairedValues.add(value);
        }

        // Mélanger
        Collections.shuffle(pairedValues);

        // Créer les cartes
        int id = 0;
        for (String value : pairedValues) {
            cards.add(new Card(id++, value, theme));
        }

        return cards;
    }
}
