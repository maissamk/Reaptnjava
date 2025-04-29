package services;

import models.ParcelleRegressionModel;
import models.ParcelleRegressionModel.EstimationResultat;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service gérant l'estimation des parcelles et les suggestions de localités
 */
public class ParcelleEstimationService {
    private static ParcelleEstimationService instance;
    private ParcelleRegressionModel model;
    private ExecutorService executorService;
    private boolean isModelLoaded = false;
    private static final String MODEL_FILE = "parcelleModel.dat";

    // Cache des suggestions récentes pour performance
    private Map<String, List<String>> suggestionsCache = new HashMap<>();
    private static final int CACHE_SIZE = 100;
    private static final int MAX_SUGGESTIONS = 10;

    // Historique des suggestions sélectionnées par les utilisateurs (pour améliorer les recommandations)
    private List<String> popularLocations = new ArrayList<>();

    /**
     * Constructeur privé (singleton)
     */
    private ParcelleEstimationService() {
        model = new ParcelleRegressionModel();
        executorService = Executors.newFixedThreadPool(2); // Pool de threads pour les opérations asynchrones
        loadModelAsync();
        initializePopularLocations();
    }

    /**
     * Initialise les localités populaires (pourrait être chargé depuis une base de données)
     */
    private void initializePopularLocations() {
        popularLocations.addAll(Arrays.asList(
                "Tunis", "Sousse", "Sfax", "Hammamet", "Nabeul", "Bizerte",
                "Monastir", "Djerba", "Gabes", "Kairouan", "La Marsa", "Carthage"
        ));
    }

    /**
     * Récupère l'instance unique du service
     */
    public static synchronized ParcelleEstimationService getInstance() {
        if (instance == null) {
            instance = new ParcelleEstimationService();
        }
        return instance;
    }

    /**
     * Charge le modèle de manière asynchrone
     */
    private void loadModelAsync() {
        CompletableFuture.runAsync(() -> {
            File modelFile = new File(MODEL_FILE);
            if (modelFile.exists()) {
                model.loadModel(MODEL_FILE);
            } else {
                // Entraîner et sauvegarder le modèle si aucun fichier n'existe
                model.train();
                model.saveModel(MODEL_FILE);
            }
            isModelLoaded = true;
        }, executorService);
    }

    /**
     * Estime la valeur d'une parcelle
     */
    public EstimationResultat estimerParcelle(
            String typeBien, String lieu, double surface,
            boolean accesEau, boolean cheminAcces,
            boolean cloture, boolean construisible) {

        // Si le modèle n'est pas encore chargé, attendre sa disponibilité
        if (!isModelLoaded) {
            synchronized (this) {
                try {
                    wait(1000); // Attendre 1 seconde max
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return model.estimer(typeBien, lieu, surface, accesEau, cheminAcces, cloture, construisible);
    }

    /**
     * Obtient les suggestions de localités basées sur le terme de recherche
     * Utilise à la fois le modèle et une logique de préférence intelligente
     */
    public List<String> getSuggestions(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            // Retourner les localités populaires si la recherche est vide
            return popularLocations.subList(0, Math.min(MAX_SUGGESTIONS, popularLocations.size()));
        }

        // Vérifier dans le cache d'abord
        String key = searchTerm.toLowerCase().trim();
        if (suggestionsCache.containsKey(key)) {
            return suggestionsCache.get(key);
        }

        // Filtrer les localités basées sur le terme de recherche
        List<String> filteredLocalities = model.filterLocalities(key);

        // Trier les résultats par pertinence
        List<String> prioritizedResults = prioritizeSuggestions(filteredLocalities, key);

        // Limiter le nombre de suggestions
        List<String> limitedResults = prioritizedResults.subList(0,
                Math.min(MAX_SUGGESTIONS, prioritizedResults.size()));

        // Mettre en cache les résultats pour les futures requêtes
        if (suggestionsCache.size() >= CACHE_SIZE) {
            // Supprimer une entrée aléatoire si le cache est plein
            List<String> keys = new ArrayList<>(suggestionsCache.keySet());
            suggestionsCache.remove(keys.get(new Random().nextInt(keys.size())));
        }
        suggestionsCache.put(key, limitedResults);

        return limitedResults;
    }

    /**
     * Priorise les suggestions selon plusieurs critères pour une meilleure expérience utilisateur
     */
    private List<String> prioritizeSuggestions(List<String> suggestions, String searchTerm) {
        if (suggestions.isEmpty()) {
            return suggestions;
        }

        // Convertir en majuscules la première lettre de chaque mot pour une meilleure présentation
        suggestions = suggestions.stream()
                .map(this::capitalizeWords)
                .collect(Collectors.toList());

        // Trier les suggestions selon plusieurs critères
        return suggestions.stream()
                .sorted((s1, s2) -> {
                    // 1. Priorité aux correspondances exactes au début du mot
                    boolean s1StartsWithTerm = s1.toLowerCase().startsWith(searchTerm.toLowerCase());
                    boolean s2StartsWithTerm = s2.toLowerCase().startsWith(searchTerm.toLowerCase());
                    if (s1StartsWithTerm && !s2StartsWithTerm) return -1;
                    if (!s1StartsWithTerm && s2StartsWithTerm) return 1;

                    // 2. Priorité aux localités populaires
                    boolean s1IsPopular = popularLocations.contains(s1);
                    boolean s2IsPopular = popularLocations.contains(s2);
                    if (s1IsPopular && !s2IsPopular) return -1;
                    if (!s1IsPopular && s2IsPopular) return 1;

                    // 3. Priorité aux noms plus courts (généralement les localités principales)
                    return Integer.compare(s1.length(), s2.length());
                })
                .collect(Collectors.toList());
    }

    /**
     * Met en majuscule la première lettre de chaque mot
     */
    private String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c) || c == '-') {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }

        return result.toString();
    }

    /**
     * Enregistre la sélection d'une localité pour améliorer les suggestions futures
     */
    public void registerLocationSelection(String location) {
        // Ajouter à la liste des localités populaires si elle n'y est pas déjà
        String capitalizedLocation = capitalizeWords(location);
        if (!popularLocations.contains(capitalizedLocation)) {
            popularLocations.add(capitalizedLocation);
            // Maintenir la liste à une taille raisonnable
            if (popularLocations.size() > 20) {
                popularLocations.remove(0);
            }
        } else {
            // Déplacer vers la fin de la liste pour augmenter sa priorité
            popularLocations.remove(capitalizedLocation);
            popularLocations.add(capitalizedLocation);
        }
    }

    /**
     * Efface le cache de suggestions
     */
    public void clearSuggestionsCache() {
        suggestionsCache.clear();
    }

    /**
     * Ferme le service et libère les ressources
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}