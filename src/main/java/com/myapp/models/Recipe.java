package com.myapp.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a recipe in the system.
 */
public class Recipe extends BaseModel {
    private String title;
    private String description;
    private int preparationTime; // in minutes
    private int cookingTime; // in minutes
    private int servings;
    private String difficulty;
    private List<Ingredient> ingredients;
    private List<String> instructions;
    private String category;

    public Recipe() {
        this.ingredients = new ArrayList<>();
        this.instructions = new ArrayList<>();
    }
    
    public Recipe(String title, String category, int prepTime, int cookTime, int servings, String difficulty, String description, String instructions) {
        this();
        this.title = title;
        this.category = category;
        this.preparationTime = prepTime;
        this.cookingTime = cookTime;
        this.servings = servings;
        this.difficulty = difficulty;
        this.description = description;
        if (instructions != null) {
            this.instructions = List.of(instructions.split("\\n"));
        }
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }

    public void addInstruction(String instruction) {
        this.instructions.add(instruction);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "id=" + getId() +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", preparationTime=" + preparationTime +
                ", cookingTime=" + cookingTime +
                ", servings=" + servings +
                ", difficulty='" + difficulty + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
