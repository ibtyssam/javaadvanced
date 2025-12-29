package com.myapp.controllers;

import com.myapp.models.Recipe;
import com.myapp.services.RecipeService;
import com.myapp.services.RecipeServiceImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;

public class RecipeDetailController {
    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private Label difficultyLabel;
    @FXML private Label servingsLabel;
    @FXML private Label prepTimeLabel;
    @FXML private Label cookTimeLabel;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea ingredientsArea;
    @FXML private TextArea instructionsArea;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button backButton;

    private final RecipeService recipeService = new RecipeServiceImpl();
    private Recipe recipe;

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
        populateDetails();
    }

    private void populateDetails() {
        if (recipe == null) return;
        titleLabel.setText(nonNull(recipe.getTitle()));
        categoryLabel.setText(nonNull(recipe.getCategory()));
        difficultyLabel.setText(nonNull(recipe.getDifficulty()));
        servingsLabel.setText(String.valueOf(recipe.getServings()));
        prepTimeLabel.setText(String.valueOf(recipe.getPreparationTime()));
        cookTimeLabel.setText(String.valueOf(recipe.getCookingTime()));
        descriptionArea.setText(nonNull(recipe.getDescription()));
        StringBuilder ing = new StringBuilder();
        if (recipe.getIngredients() != null) {
            recipe.getIngredients().forEach(i -> ing.append(i.toString()).append("\n"));
        }
        ingredientsArea.setText(ing.toString());
        StringBuilder instr = new StringBuilder();
        if (recipe.getInstructions() != null) {
            for (int i = 0; i < recipe.getInstructions().size(); i++) {
                instr.append(i + 1).append(". ").append(recipe.getInstructions().get(i)).append("\n");
            }
        }
        instructionsArea.setText(instr.toString());
    }

    @FXML
    private void handleEdit() {
        // Reuse the form dialog from list controller by navigating back and opening edit
        try {
            navigateToListAndEdit();
        } catch (Exception e) {
            showError("Failed to edit: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        try {
            if (recipe != null && recipe.getId() != null) {
                recipeService.deleteRecipe(recipe.getId());
                navigateToList();
            }
        } catch (Exception e) {
            showError("Failed to delete: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            navigateToList();
        } catch (Exception e) {
            showError("Failed to go back: " + e.getMessage());
        }
    }

    private void navigateToList() throws Exception {
        URL fxmlUrl = getClass().getResource("/views/recipe-list.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene scene = new Scene(root, 800, 600);
        URL cssUrl = getClass().getResource("/styles/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        stage.setScene(scene);
        stage.setTitle("Recipe Manager - All Recipes");
        stage.show();
    }

    private void navigateToListAndEdit() throws Exception {
        URL fxmlUrl = getClass().getResource("/views/recipe-list.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        Stage stage = (Stage) editButton.getScene().getWindow();
        Scene scene = new Scene(root, 800, 600);
        URL cssUrl = getClass().getResource("/styles/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        stage.setScene(scene);
        stage.setTitle("Recipe Manager - All Recipes");
        stage.show();
        // After navigating, try to select the recipe (optional enhancement)
    }

    private void showError(String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String nonNull(String s) {
        return s == null ? "" : s;
    }
}
