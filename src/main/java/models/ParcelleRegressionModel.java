package models;

import java.util.*;
import java.io.*;

/**
 * Modèle d'apprentissage par régression pour l'estimation de parcelles
 * Utilise une régression linéaire avec gradient stochastique
 */
public class ParcelleRegressionModel {
    // Hyperparamètres
    private double learningRate = 0.01;
    private int epochs = 1000;
    private double regularizationTerm = 0.01;

    // Coefficients du modèle
    private double[] weights;
    private double bias;

    // État d'apprentissage
    private boolean isTrained = false;

    // Statistiques sur les données d'entraînement
    private Map<String, Double> regionStats = new HashMap<>();
    private Map<String, Double> typeStats = new HashMap<>();

    // Données d'exemple pour l'entraînement initial
    private List<TrainingExample> trainingData;

    /**
     * Constructeur - initialise le modèle avec des données d'exemple
     */
    public ParcelleRegressionModel() {
        // 8 features: surface, accesEau, cheminAcces, cloture, construisible,
        // + encodage one-hot pour la région et le type
        weights = new double[8];
        bias = 0.0;

        // Initialiser les poids avec de petites valeurs aléatoires
        Random random = new Random(42);
        for (int i = 0; i < weights.length; i++) {
            weights[i] = random.nextDouble() * 0.1;
        }

        // Initialiser les données d'entraînement
        initializeTrainingData();

        // Entraîner le modèle
        train();
    }

    /**
     * Initialise les données d'entraînement pour le modèle
     */
    private void initializeTrainingData() {
        trainingData = new ArrayList<>();

        // Ajouter des exemples représentatifs pour différentes régions et types de terrain
        // Format: surface, accesEau, cheminAcces, cloture, construisible, type, region, prix

        // Terres agricoles
        addTrainingExample(10000, true, true, false, false, "Terrain agricole", "tunis", 350000);
        addTrainingExample(8000, true, true, false, false, "Terrain agricole", "sousse", 240000);
        addTrainingExample(15000, true, true, false, false, "Terrain agricole", "sfax", 300000);
        addTrainingExample(12000, false, true, false, false, "Terrain agricole", "bizerte", 216000);
        addTrainingExample(10000, true, false, false, false, "Terrain agricole", "nabeul", 230000);

        // Terrains résidentiels
        addTrainingExample(500, true, true, true, true, "Terrain résidentiel", "tunis", 750000);
        addTrainingExample(400, true, true, true, true, "Terrain résidentiel", "sousse", 520000);
        addTrainingExample(600, true, true, true, true, "Terrain résidentiel", "sfax", 480000);
        addTrainingExample(450, true, true, true, true, "Terrain résidentiel", "hammamet", 630000);
        addTrainingExample(500, true, true, false, true, "Terrain résidentiel", "nabeul", 550000);

        // Forêts
        addTrainingExample(20000, false, false, false, false, "Forêt", "bizerte", 140000);
        addTrainingExample(25000, false, true, false, false, "Forêt", "nabeul", 187500);
        addTrainingExample(30000, false, false, false, false, "Forêt", "kairouan", 150000);
        addTrainingExample(15000, false, true, false, false, "Forêt", "gafsa", 75000);

        // Terrains commerciaux
        addTrainingExample(1000, true, true, true, true, "Terrain commercial", "tunis", 2000000);
        addTrainingExample(800, true, true, true, true, "Terrain commercial", "sousse", 1200000);
        addTrainingExample(1200, true, true, true, true, "Terrain commercial", "sfax", 1440000);

        // Terres et prairies
        addTrainingExample(15000, false, true, false, false, "Terres et prairies", "kairouan", 112500);
        addTrainingExample(18000, true, true, false, false, "Terres et prairies", "gafsa", 126000);
        addTrainingExample(12000, false, false, false, false, "Terres et prairies", "gabes", 84000);

        // Calculer les statistiques sur les données d'entraînement
        calculateStats();
    }

    /**
     * Ajoute un exemple d'entraînement à la liste
     */
    private void addTrainingExample(double surface, boolean accesEau, boolean cheminAcces,
                                    boolean cloture, boolean construisible,
                                    String type, String region, double prix) {
        TrainingExample example = new TrainingExample();
        example.surface = surface;
        example.accesEau = accesEau;
        example.cheminAcces = cheminAcces;
        example.cloture = cloture;
        example.construisible = construisible;
        example.type = type;
        example.region = region;
        example.prix = prix;

        trainingData.add(example);
    }

    /**
     * Calcule les statistiques sur les données d'entraînement
     */
    private void calculateStats() {
        // Calculer les prix moyens par région
        Map<String, List<Double>> regionPrices = new HashMap<>();
        Map<String, List<Double>> typePrices = new HashMap<>();

        for (TrainingExample example : trainingData) {
            // Ajouter à la liste des prix par région
            regionPrices.computeIfAbsent(example.region, k -> new ArrayList<>())
                    .add(example.prix / example.surface);

            // Ajouter à la liste des prix par type
            typePrices.computeIfAbsent(example.type, k -> new ArrayList<>())
                    .add(example.prix / example.surface);
        }

        // Calculer les moyennes par région
        for (Map.Entry<String, List<Double>> entry : regionPrices.entrySet()) {
            double average = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            regionStats.put(entry.getKey(), average);
        }

        // Calculer les moyennes par type
        for (Map.Entry<String, List<Double>> entry : typePrices.entrySet()) {
            double average = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            typeStats.put(entry.getKey(), average);
        }
    }

    /**
     * Entraîne le modèle sur les données d'exemple
     */
    public void train() {
        // Si nous n'avons pas de données d'entraînement, initialiser
        if (trainingData == null || trainingData.isEmpty()) {
            initializeTrainingData();
        }

        // Normaliser les données pour l'entraînement
        double maxSurface = trainingData.stream()
                .mapToDouble(example -> example.surface)
                .max()
                .orElse(1.0);

        double maxPrice = trainingData.stream()
                .mapToDouble(example -> example.prix)
                .max()
                .orElse(1.0);

        // Descente de gradient stochastique
        for (int epoch = 0; epoch < epochs; epoch++) {
            // Mélanger les données à chaque époque
            Collections.shuffle(trainingData);

            for (TrainingExample example : trainingData) {
                // Préparer les features
                double[] features = new double[8];
                features[0] = example.surface / maxSurface; // Surface normalisée
                features[1] = example.accesEau ? 1.0 : 0.0;
                features[2] = example.cheminAcces ? 1.0 : 0.0;
                features[3] = example.cloture ? 1.0 : 0.0;
                features[4] = example.construisible ? 1.0 : 0.0;

                // Encodage simple pour le type et la région
                // Dans un vrai système, on utiliserait un one-hot encoding plus sophistiqué
                features[5] = getTypeEncoding(example.type);
                features[6] = getRegionEncoding(example.region);
                features[7] = features[5] * features[6]; // Interaction type-région

                // Prix normalisé
                double normalizedPrice = example.prix / maxPrice;

                // Prédiction
                double prediction = predict(features);

                // Erreur
                double error = prediction - normalizedPrice;

                // Mise à jour des poids
                for (int i = 0; i < weights.length; i++) {
                    // Gradient de l'erreur par rapport au poids
                    double gradient = error * features[i];
                    // Mise à jour du poids avec régularisation L2
                    weights[i] = weights[i] - learningRate * (gradient + regularizationTerm * weights[i]);
                }

                // Mise à jour du biais
                bias = bias - learningRate * error;
            }
        }

        isTrained = true;
    }

    /**
     * Prédit le prix d'une parcelle
     * @param features tableau des caractéristiques normalisées
     * @return prix prédit normalisé
     */
    private double predict(double[] features) {
        double prediction = bias;
        for (int i = 0; i < weights.length; i++) {
            prediction += weights[i] * features[i];
        }
        return Math.max(0, prediction); // Le prix ne peut pas être négatif
    }

    /**
     * Estime le prix d'une parcelle
     * @param typeBien Type de bien (forêt, terrain agricole, etc.)
     * @param region Région de la parcelle
     * @param surface Surface en m²
     * @param accesEau Accès à l'eau
     * @param cheminAcces Chemin d'accès
     * @param cloture Si le terrain est clôturé
     * @param construisible Si le terrain est constructible
     * @return Prix estimé et prix au m²
     */
    public EstimationResultat estimer(String typeBien, String region, double surface,
                                      boolean accesEau, boolean cheminAcces,
                                      boolean cloture, boolean construisible) {

        if (!isTrained) {
            train();
        }

        // Trouver la région la plus proche dans nos données
        String matchedRegion = findClosestRegion(region.toLowerCase());

        // Normaliser les features
        // Cette normalisation devrait être cohérente avec celle utilisée dans l'entraînement
        double[] features = new double[8];

        // Dans un système réel, cette valeur devrait être cohérente avec la normalisation d'entraînement
        double maxSurface = 30000.0;

        features[0] = surface / maxSurface;
        features[1] = accesEau ? 1.0 : 0.0;
        features[2] = cheminAcces ? 1.0 : 0.0;
        features[3] = cloture ? 1.0 : 0.0;
        features[4] = construisible ? 1.0 : 0.0;
        features[5] = getTypeEncoding(typeBien);
        features[6] = getRegionEncoding(matchedRegion);
        features[7] = features[5] * features[6];

        // Dans un système réel, cette valeur devrait être cohérente avec la normalisation d'entraînement
        double maxPrice = 2000000.0;

        // Prédiction normalisée
        double normalizedPrediction = predict(features);

        // Dénormaliser pour obtenir le prix réel
        double prixTotal = normalizedPrediction * maxPrice;

        // Ajouter une variation aléatoire pour simuler la "prédiction IA"
        double randomFactor = 0.9 + Math.random() * 0.2; // Entre 0.9 et 1.1
        prixTotal *= randomFactor;

        // Calculer le prix au m²
        double prixAuM2 = prixTotal / surface;

        return new EstimationResultat(prixAuM2, prixTotal);
    }

    /**
     * Trouve la région la plus proche dans les données d'entraînement
     * @param region Région à rechercher
     * @return Nom de la région correspondante dans les données d'entraînement
     */
    private String findClosestRegion(String region) {
        // Si la région est exactement dans nos données, la retourner
        if (regionStats.containsKey(region)) {
            return region;
        }

        // Sinon, chercher la région qui contient la chaîne recherchée
        for (String knownRegion : regionStats.keySet()) {
            if (region.contains(knownRegion) || knownRegion.contains(region)) {
                return knownRegion;
            }
        }

        // Si aucune correspondance, retourner "default"
        return "default";
    }

    /**
     * Encodage numérique simple pour le type de bien
     * @param type Type de bien
     * @return Valeur numérique encodée
     */
    private double getTypeEncoding(String type) {
        switch(type) {
            case "Terres et prairies": return 0.2;
            case "Forêt": return 0.4;
            case "Terrain agricole": return 0.6;
            case "Terrain résidentiel": return 0.8;
            case "Terrain commercial": return 1.0;
            default: return 0.5;
        }
    }

    /**
     * Encodage numérique simple pour la région
     * @param region Région
     * @return Valeur numérique encodée
     */
    private double getRegionEncoding(String region) {
        switch(region) {
            case "tunis": return 1.0;
            case "sousse": return 0.9;
            case "sfax": return 0.8;
            case "bizerte": return 0.7;
            case "nabeul": return 0.75;
            case "hammamet": return 0.85;
            case "monastir": return 0.8;
            case "mahdia": return 0.65;
            case "gabes": return 0.6;
            case "djerba": return 0.9;
            case "kairouan": return 0.55;
            case "gafsa": return 0.45;
            default: return 0.5;
        }
    }

    /**
     * Récupère la liste des régions connues par le modèle
     * @return Liste des régions
     */
    public List<String> getKnownRegions() {
        List<String> regions = new ArrayList<>(regionStats.keySet());
        Collections.sort(regions);
        return regions;
    }

    /**
     * Récupère la liste de toutes les localités connues (régions avec des synonymes)
     * @return Liste des localités
     */
    public List<String> getAllLocalities() {
        List<String> localities = new ArrayList<>();

        // Régions principales
        localities.addAll(getKnownRegions());

        // Ajouter des synonymes et sous-régions
        localities.add("Grand Tunis");
        localities.add("Ariana");
        localities.add("La Marsa");
        localities.add("Carthage");
        localities.add("Sidi Bou Said");
        localities.add("Ben Arous");
        localities.add("La Soukra");
        localities.add("Manouba");
        localities.add("Lac Tunis");
        localities.add("Sousse Nord");
        localities.add("Sousse Sud");
        localities.add("Kantaoui");
        localities.add("Monastir Ville");
        localities.add("Skanes");
        localities.add("Mahdia Centre");
        localities.add("Sfax Ville");
        localities.add("Sfax Sud");
        localities.add("Kerkennah");
        localities.add("Bizerte Ville");
        localities.add("Bizerte Nord");
        localities.add("Cap Bon");
        localities.add("Hammamet Nord");
        localities.add("Hammamet Sud");
        localities.add("Yasmine Hammamet");
        localities.add("Nabeul Ville");
        localities.add("Korba");
        localities.add("Kelibia");
        localities.add("Djerba Houmt Souk");
        localities.add("Djerba Midoun");
        localities.add("Zarzis");
        localities.add("Gabes Ville");
        localities.add("Kairouan Centre");
        localities.add("Gafsa Ville");
        localities.add("Tozeur");
        localities.add("Douz");
        localities.add("Tataouine");

        Collections.sort(localities);
        return localities;
    }

    /**
     * Filtre les localités basées sur un terme de recherche
     * @param searchTerm Terme de recherche
     * @return Liste des localités correspondantes
     */
    public List<String> filterLocalities(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllLocalities();
        }

        String term = searchTerm.toLowerCase().trim();
        List<String> filtered = new ArrayList<>();

        for (String locality : getAllLocalities()) {
            if (locality.toLowerCase().contains(term)) {
                filtered.add(locality);
            }
        }

        return filtered;
    }

    /**
     * Classe interne pour représenter un exemple d'entraînement
     */
    private class TrainingExample {
        double surface;
        boolean accesEau;
        boolean cheminAcces;
        boolean cloture;
        boolean construisible;
        String type;
        String region;
        double prix;
    }

    /**
     * Classe représentant le résultat d'une estimation
     */
    public static class EstimationResultat {
        private double prixAuM2;
        private double valeurTotale;

        public EstimationResultat(double prixAuM2, double valeurTotale) {
            this.prixAuM2 = prixAuM2;
            this.valeurTotale = valeurTotale;
        }

        public double getPrixAuM2() {
            return prixAuM2;
        }

        public double getValeurTotale() {
            return valeurTotale;
        }
    }

    /**
     * Sauvegarde le modèle dans un fichier
     */
    public void saveModel(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(weights);
            out.writeObject(bias);
            out.writeObject(regionStats);
            out.writeObject(typeStats);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde du modèle: " + e.getMessage());
        }
    }

    /**
     * Charge le modèle depuis un fichier
     */
    @SuppressWarnings("unchecked")
    public void loadModel(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            weights = (double[]) in.readObject();
            bias = (double) in.readObject();
            regionStats = (Map<String, Double>) in.readObject();
            typeStats = (Map<String, Double>) in.readObject();
            isTrained = true;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erreur lors du chargement du modèle: " + e.getMessage());
            // Si le modèle ne peut pas être chargé, en créer un nouveau
            initializeTrainingData();
            train();
        }
    }
}