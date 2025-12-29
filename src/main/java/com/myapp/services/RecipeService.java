package com.myapp.services;

import com.myapp.models.Recipe;

import java.util.List;
import java.util.Optional;

public interface RecipeService {
    List<Recipe> getAllRecipes();
    Optional<Recipe> getRecipeById(Integer id);
    Recipe saveRecipe(Recipe recipe);
    void deleteRecipe(Integer id);
    List<Recipe> searchRecipes(String query);
    List<Recipe> getRecipesByCategory(String category);
}