package com.myapp.repositories;

import com.myapp.config.DatabaseConfig;
import com.myapp.models.Ingredient;
import com.myapp.models.Recipe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipeRepository {

    public Recipe save(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("Recipe cannot be null");
        }

        validateRecipeFields(recipe);

        if (recipe.getId() == null) {
            insert(recipe);
        } else {
            update(recipe);
        }
        return recipe;
    }

    private void insert(Recipe recipe) {
        String sql = "INSERT INTO recipe (title, description, preparation_time, cooking_time, servings, difficulty, category) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);

            ps.setString(1, recipe.getTitle());
            ps.setString(2, recipe.getDescription());
            ps.setInt(3, recipe.getPreparationTime());
            ps.setInt(4, recipe.getCookingTime());
            ps.setInt(5, recipe.getServings());
            ps.setString(6, recipe.getDifficulty());
            ps.setString(7, recipe.getCategory());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    recipe.setId(rs.getInt(1));
                }
            }

            saveIngredients(conn, recipe);
            saveInstructions(conn, recipe);

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert recipe", e);
        }
    }

    private void update(Recipe recipe) {
        String sql = "UPDATE recipe SET title = ?, description = ?, preparation_time = ?, cooking_time = ?, " +
                     "servings = ?, difficulty = ?, category = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            ps.setString(1, recipe.getTitle());
            ps.setString(2, recipe.getDescription());
            ps.setInt(3, recipe.getPreparationTime());
            ps.setInt(4, recipe.getCookingTime());
            ps.setInt(5, recipe.getServings());
            ps.setString(6, recipe.getDifficulty());
            ps.setString(7, recipe.getCategory());
            ps.setInt(8, recipe.getId());

            ps.executeUpdate();

            deleteIngredients(conn, recipe.getId());
            deleteInstructions(conn, recipe.getId());
            saveIngredients(conn, recipe);
            saveInstructions(conn, recipe);

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update recipe", e);
        }
    }

    public Optional<Recipe> findById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }

        String sql = "SELECT id, title, description, preparation_time, cooking_time, servings, difficulty, category " +
                     "FROM recipe WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Recipe recipe = mapRowToRecipe(rs);
                    loadIngredients(conn, recipe);
                    loadInstructions(conn, recipe);
                    return Optional.of(recipe);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find recipe by id", e);
        }
        return Optional.empty();
    }

    public List<Recipe> findAll() {
        List<Recipe> recipes = new ArrayList<>();
        String sql = "SELECT id, title, description, preparation_time, cooking_time, servings, difficulty, category FROM recipe";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Recipe recipe = mapRowToRecipe(rs);
                loadIngredients(conn, recipe);
                loadInstructions(conn, recipe);
                recipes.add(recipe);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all recipes", e);
        }
        return recipes;
    }

    public void delete(Integer id) {
        if (id == null) {
            return;
        }

        String sql = "DELETE FROM recipe WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            deleteIngredients(conn, id);
            deleteInstructions(conn, id);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete recipe", e);
        }
    }

    public void deleteAll() {
        String sql = "DELETE FROM recipe";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete all recipes", e);
        }
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM recipe";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count recipes", e);
        }
        return 0;
    }

    private void validateRecipeFields(Recipe recipe) {
        if (recipe.getTitle() == null || recipe.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe title cannot be null or empty");
        }
        if (recipe.getDescription() == null || recipe.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe description cannot be null or empty");
        }
        if (recipe.getPreparationTime() < 0) {
            throw new IllegalArgumentException("Preparation time cannot be negative");
        }
        if (recipe.getCookingTime() < 0) {
            throw new IllegalArgumentException("Cooking time cannot be negative");
        }
        if (recipe.getServings() <= 0) {
            throw new IllegalArgumentException("Servings must be greater than 0");
        }
        if (recipe.getDifficulty() == null || recipe.getDifficulty().trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe difficulty cannot be null or empty");
        }
        if (recipe.getCategory() == null || recipe.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe category cannot be null or empty");
        }
    }

    private boolean isRecipeComplete(Recipe recipe) {
        return recipe != null &&
               recipe.getTitle() != null && !recipe.getTitle().trim().isEmpty() &&
               recipe.getDescription() != null && !recipe.getDescription().trim().isEmpty() &&
               recipe.getPreparationTime() >= 0 &&
               recipe.getCookingTime() >= 0 &&
               recipe.getServings() > 0 &&
               recipe.getDifficulty() != null && !recipe.getDifficulty().trim().isEmpty() &&
               recipe.getCategory() != null && !recipe.getCategory().trim().isEmpty() &&
               recipe.getIngredients() != null && !recipe.getIngredients().isEmpty() &&
               recipe.getInstructions() != null && !recipe.getInstructions().isEmpty();
    }

    private Recipe mapRowToRecipe(ResultSet rs) throws SQLException {
        Recipe recipe = new Recipe();
        recipe.setId(rs.getInt("id"));
        recipe.setTitle(rs.getString("title"));
        recipe.setDescription(rs.getString("description"));
        recipe.setPreparationTime(rs.getInt("preparation_time"));
        recipe.setCookingTime(rs.getInt("cooking_time"));
        recipe.setServings(rs.getInt("servings"));
        recipe.setDifficulty(rs.getString("difficulty"));
        recipe.setCategory(rs.getString("category"));
        return recipe;
    }

    private void saveIngredients(Connection conn, Recipe recipe) throws SQLException {
        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO ingredient (recipe_id, name, quantity, unit, notes) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Ingredient ing : recipe.getIngredients()) {
                if (ing == null) {
                    continue;
                }
                ps.setInt(1, recipe.getId());
                ps.setString(2, ing.getName());
                ps.setDouble(3, ing.getQuantity());
                ps.setString(4, ing.getUnit());
                ps.setString(5, ing.getNotes());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void saveInstructions(Connection conn, Recipe recipe) throws SQLException {
        if (recipe.getInstructions() == null || recipe.getInstructions().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO instruction (recipe_id, step_number, text) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int step = 1;
            for (String text : recipe.getInstructions()) {
                if (text == null || text.isBlank()) {
                    continue;
                }
                ps.setInt(1, recipe.getId());
                ps.setInt(2, step++);
                ps.setString(3, text);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void loadIngredients(Connection conn, Recipe recipe) throws SQLException {
        String sql = "SELECT id, name, quantity, unit, notes FROM ingredient WHERE recipe_id = ?";

        List<Ingredient> ingredients = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recipe.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ingredient ing = new Ingredient();
                    ing.setId(rs.getInt("id"));
                    ing.setName(rs.getString("name"));
                    ing.setQuantity(rs.getDouble("quantity"));
                    ing.setUnit(rs.getString("unit"));
                    ing.setNotes(rs.getString("notes"));
                    ingredients.add(ing);
                }
            }
        }

        recipe.setIngredients(ingredients);
    }

    private void loadInstructions(Connection conn, Recipe recipe) throws SQLException {
        String sql = "SELECT step_number, text FROM instruction WHERE recipe_id = ? ORDER BY step_number";

        List<String> instructions = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recipe.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    instructions.add(rs.getString("text"));
                }
            }
        }

        recipe.setInstructions(instructions);
    }

    private void deleteIngredients(Connection conn, int recipeId) throws SQLException {
        String sql = "DELETE FROM ingredient WHERE recipe_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recipeId);
            ps.executeUpdate();
        }
    }

    private void deleteInstructions(Connection conn, int recipeId) throws SQLException {
        String sql = "DELETE FROM instruction WHERE recipe_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recipeId);
            ps.executeUpdate();
        }
    }
}
