package com.myapp.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.myapp.models.Recipe;
import com.myapp.repositories.RecipeRepository;

public class RecipeServiceImpl implements RecipeService {
    private final RecipeRepository recipeRepository;

    public RecipeServiceImpl() {
        this.recipeRepository = new RecipeRepository();
    }

    @Override
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    @Override
    public Optional<Recipe> getRecipeById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return recipeRepository.findById(id);
    }

    @Override
    public Recipe saveRecipe(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("Recipe cannot be null");
        }
        return recipeRepository.save(recipe);
    }

    @Override
    public void deleteRecipe(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Recipe ID cannot be null");
        }
        recipeRepository.delete(id);
    }

    @Override
    public List<Recipe> searchRecipes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllRecipes();
        }

        String lowerCaseQuery = query.toLowerCase().trim();
        int qlen = lowerCaseQuery.length();
        boolean shortQuery = qlen <= 2; // for very short queries, be strict
        return recipeRepository.findAll().stream()
                .filter(recipe -> {
                    if (recipe == null) return false;
                    
                    // Short queries: title must start with query
                    boolean matches = false;
                    if (recipe.getTitle() != null) {
                        String title = recipe.getTitle().toLowerCase();
                        // Match any word in the title starting with the query (e.g., 'p' -> 'Pizza')
                        /*String[] tokens = title.split("[^a-z0-9]+");
                        for (String tok : tokens) {
                            if (!tok.isEmpty() && tok.startsWith(lowerCaseQuery)) {
                                matches = true;
                                break;
                            }
                        }
                         */
                        matches = title.contains(lowerCaseQuery);
                    }

                    // For longer queries, optionally include broader matching in description/ingredients/instructions
                    if (!matches && !shortQuery && recipe.getDescription() != null) {
                        matches = recipe.getDescription().toLowerCase().contains(lowerCaseQuery);
                    }
                    
                    // Search in ingredients
                    if (!matches && !shortQuery && recipe.getIngredients() != null) {
                        matches = recipe.getIngredients().stream()
                                .anyMatch(ingredient -> 
                                    ingredient != null && 
                                    ingredient.getName() != null &&
                                    ingredient.getName().toLowerCase().contains(lowerCaseQuery)
                                );
                    }
                    
                    // Search in instructions
                    if (!matches && !shortQuery && recipe.getInstructions() != null) {
                        matches = recipe.getInstructions().stream()
                                .anyMatch(instruction -> 
                                    instruction != null && 
                                    instruction.toLowerCase().contains(lowerCaseQuery)
                                );
                    }
                    
                    return matches;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Recipe> getRecipesByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return getAllRecipes();
        }

        String lowerCaseCategory = category.toLowerCase();
        return recipeRepository.findAll().stream()
                .filter(recipe -> 
                    recipe != null && 
                    recipe.getCategory() != null &&
                    recipe.getCategory().toLowerCase().equals(lowerCaseCategory)
                )
                .collect(Collectors.toList());
    }
}