package com.example.mockandcock;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Boisson implements Parcelable {
    private String id;
    private String nom;
    private String urlImage;
    private Map<String, String> ingredients;
    private String instructions;
    private String mockCock;
    private String verre;

    // Constructeur
    public Boisson(String id, String nom, String urlImage, String mockCock, String verre, Map<String, String> ingredients, String instructions) {
        this.id = id;
        this.nom = nom;
        this.urlImage = urlImage;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.mockCock = mockCock;
        this.verre = verre;
    }

    // Méthode statique pour créer une instance de Boisson à partir d'un JSONObject
    public static Boisson fromJSON(JSONObject jsonObject) throws JSONException {
        String id = jsonObject.getString("idDrink");
        String nom = jsonObject.getString("strDrink");
        String urlImage = jsonObject.getString("strDrinkThumb");
        Map<String, String> ingredients = new HashMap<>();
        String instructions = jsonObject.getString("strInstructionsFR");
        String mockCock = jsonObject.getString("strAlcoholic");
        String verre = jsonObject.getString("strGlass");

        for (int j = 1; j <= 15; j++) {
            String ingredientKey = "strIngredient" + j;
            String measureKey = "strMeasure" + j;
            if (jsonObject.has(ingredientKey) && !jsonObject.isNull(ingredientKey) && !jsonObject.getString(ingredientKey).trim().isEmpty()) {
                String ingredient = jsonObject.getString(ingredientKey);
                String measure = jsonObject.has(measureKey) && !jsonObject.isNull(measureKey) ? jsonObject.getString(measureKey) : "Quantité non spécifiée";
                ingredients.put(ingredient, measure);
            }
        }

        return new Boisson(id, nom, urlImage, mockCock, verre, ingredients, instructions);
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public Map<String, String> getIngredients() {
        return ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getMockCock() {
        return mockCock;
    }

    public String getVerre() {
        return verre;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public void setIngredients(Map<String, String> ingredients) {
        this.ingredients = ingredients;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public void setMockCock(String mockCock) {
        this.mockCock = mockCock;
    }

    public void setVerre(String verre) {
        this.verre = verre;
    }

    // Méthode pour afficher les informations de la boisson
    public String afficherInfos() {
        StringBuilder result = new StringBuilder();
        result.append("ID : ").append(id)
                .append("\nNom : ").append(nom)
                .append("\nImage : ").append(urlImage)
                .append("\nMock/Cocktail : ").append(mockCock)
                .append("\nVerre : ").append(verre)
                .append("\nIngrédients : \n");

        for (Map.Entry<String, String> entry : ingredients.entrySet()) {
            result.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }

        result.append("Instructions : ").append(instructions).append("\n");

        return result.toString();
    }

    // Méthode pour écrire les données de l'objet dans un Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nom);
        dest.writeString(urlImage);
        dest.writeString(instructions);
        dest.writeString(mockCock);
        dest.writeString(verre);
    }

    // Méthode pour lire les données d'un Parcel et créer un nouvel objet
    public static final Parcelable.Creator<Boisson> CREATOR = new Creator<Boisson>() {
        @Override
        public Boisson createFromParcel(Parcel in) {
            return new Boisson(in);
        }

        @Override
        public Boisson[] newArray(int size) {
            return new Boisson[size];
        }
    };

    // Constructeur privé pour Parcelable
    private Boisson(Parcel in) {
        id = in.readString();
        nom = in.readString();
        urlImage = in.readString();
        instructions = in.readString();
        mockCock = in.readString();
        verre = in.readString();
    }

    // Méthode pour décrire les contenus de l'objet
    @Override
    public int describeContents() {
        return 0;
    }
}
