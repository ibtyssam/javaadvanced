package com.myapp.dao;

import java.util.List;
import java.util.Optional;

import com.myapp.models.Recipe;

public interface RecipeDao {
    Recipe save(Recipe recipe);
    Optional<Recipe> findById(Integer id);
    List<Recipe> findAll();
    void delete(Integer id);
    List<Recipe> findAllVisibleForUser(Integer userId);
}
