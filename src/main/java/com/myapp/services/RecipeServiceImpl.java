package com.myapp.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.myapp.dao.RecipeDao;
import com.myapp.dao.RecipeDaoImpl;
import com.myapp.models.Recipe;

public class RecipeServiceImpl implements RecipeService {
    private final RecipeDao recipeDao;

    public RecipeServiceImpl() {
        this.recipeDao = new RecipeDaoImpl();
    }

    @Override
    public List<Recipe> getAllRecipes() {
        Integer userId = null;
        if (SessionManager.isLoggedIn() && SessionManager.getCurrentUser() != null) {
            userId = SessionManager.getCurrentUser().getId();
        }
        return recipeDao.findAllVisibleForUser(userId);
    }

    @Override
    public Optional<Recipe> getRecipeById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return recipeDao.findById(id);
    }

    @Override
    public Recipe saveRecipe(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("Recipe cannot be null");
        }
        return recipeDao.save(recipe);
    }

    @Override
    public void deleteRecipe(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Recipe ID cannot be null");
        }
        recipeDao.delete(id);
    }

    @Override
    public List<Recipe> searchRecipes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllRecipes();
        }

        String lowerCaseQuery = query.toLowerCase().trim();
        int qlen = lowerCaseQuery.length();
        boolean shortQuery = qlen <= 2; // for very short queries, be strict
        return recipeDao.findAll().stream()
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
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Recipe> getRecipesByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return getAllRecipes();
        }

        String lowerCaseCategory = category.toLowerCase();
        return recipeDao.findAll().stream()
                .filter(recipe -> 
                    recipe != null && 
                    recipe.getCategory() != null &&
                    recipe.getCategory().toLowerCase().equals(lowerCaseCategory)
                )
                .collect(Collectors.toList());
    }
}