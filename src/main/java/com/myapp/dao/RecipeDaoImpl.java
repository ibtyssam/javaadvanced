package com.myapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.myapp.config.DatabaseConfig;
import com.myapp.models.Ingredient;
import com.myapp.models.Recipe;

public class RecipeDaoImpl implements RecipeDao {

    @Override
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
        String sql = "INSERT INTO recipe (title, description, preparation_time, cooking_time, servings, difficulty, category, owner_user_id, visibility) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            if (recipe.getOwnerUserId() == null) {
                ps.setNull(8, java.sql.Types.INTEGER);
            } else {
                ps.setInt(8, recipe.getOwnerUserId());
            }
            ps.setString(9, recipe.getVisibility() == null ? "PRIVATE" : recipe.getVisibility());

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
                     "servings = ?, difficulty = ?, category = ?, owner_user_id = ?, visibility = ? WHERE id = ?";

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
            if (recipe.getOwnerUserId() == null) {
                ps.setNull(8, java.sql.Types.INTEGER);
            } else {
                ps.setInt(8, recipe.getOwnerUserId());
            }
            ps.setString(9, recipe.getVisibility() == null ? "PRIVATE" : recipe.getVisibility());
            ps.setInt(10, recipe.getId());

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

    @Override
    public Optional<Recipe> findById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }

        String sql = "SELECT id, title, description, preparation_time, cooking_time, servings, difficulty, category, owner_user_id, visibility " +
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
        }catch (SQLException e) {
            throw new RuntimeException("Failed to find recipe by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Recipe> findAll() {
        List<Recipe> recipes = new ArrayList<>();
        String sql = "SELECT id, title, description, preparation_time, cooking_time, servings, difficulty, category, owner_user_id, visibility FROM recipe";

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

    @Override
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

    @Override
    public List<Recipe> findAllVisibleForUser(Integer userId) {
        List<Recipe> recipes = new ArrayList<>();
        String sqlNoUser = "SELECT id, title, description, preparation_time, cooking_time, servings, difficulty, category, owner_user_id, visibility "
            + "FROM recipe WHERE visibility = 'PUBLIC' OR visibility IS NULL OR visibility = ''";
        String sqlWithUser = "SELECT id, title, description, preparation_time, cooking_time, servings, difficulty, category, owner_user_id, visibility "
            + "FROM recipe WHERE visibility = 'PUBLIC' OR visibility IS NULL OR visibility = '' OR owner_user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            PreparedStatement ps;
            try {
                if (userId == null) {
                    ps = conn.prepareStatement(sqlNoUser);
                } else {
                    ps = conn.prepareStatement(sqlWithUser);
                    ps.setInt(1, userId);
                }
            } catch (SQLException e) {
                // Fallback for legacy schema without visibility/owner_user_id
                String legacySql = "SELECT id, title, description, preparation_time, cooking_time, servings, difficulty, category FROM recipe";
                ps = conn.prepareStatement(legacySql);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Recipe recipe = mapRowToRecipe(rs);
                    loadIngredients(conn, recipe);
                    loadInstructions(conn, recipe);
                    recipes.add(recipe);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find visible recipes", e);
        }
        return recipes;
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
        // Optional columns support (legacy schema without auth fields)
        if (hasColumn(rs, "owner_user_id")) {
            int ownerId = rs.getInt("owner_user_id");
            if (!rs.wasNull()) {
                recipe.setOwnerUserId(ownerId);
            }
        }
        if (hasColumn(rs, "visibility")) {
            recipe.setVisibility(rs.getString("visibility"));
        }
        return recipe;
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        var md = rs.getMetaData();
        int columns = md.getColumnCount();
        for (int i = 1; i <= columns; i++) {
            if (columnName.equalsIgnoreCase(md.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
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
