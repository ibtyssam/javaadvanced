package com.myapp.controllers;

import java.net.URL;

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
        try {
            javafx.scene.control.Dialog<Recipe> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Edit Recipe");

            javafx.scene.control.ButtonType saveButtonType = new javafx.scene.control.ButtonType("Save", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, javafx.scene.control.ButtonType.CANCEL);

            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            javafx.scene.control.TextField titleField = new javafx.scene.control.TextField(nonNull(recipe.getTitle()));
            javafx.scene.control.TextField categoryField = new javafx.scene.control.TextField(nonNull(recipe.getCategory()));
            javafx.scene.control.TextField prepField = new javafx.scene.control.TextField(String.valueOf(recipe.getPreparationTime()));
            javafx.scene.control.TextField cookField = new javafx.scene.control.TextField(String.valueOf(recipe.getCookingTime()));
            javafx.scene.control.TextField servingsField = new javafx.scene.control.TextField(String.valueOf(recipe.getServings()));
            javafx.scene.control.TextField difficultyField = new javafx.scene.control.TextField(nonNull(recipe.getDifficulty()));
            javafx.scene.control.TextArea descriptionEditArea = new javafx.scene.control.TextArea(nonNull(recipe.getDescription()));
            javafx.scene.control.ComboBox<String> visibilityBox = new javafx.scene.control.ComboBox<>();
            visibilityBox.setItems(javafx.collections.FXCollections.observableArrayList("PUBLIC", "PRIVATE"));
            visibilityBox.getSelectionModel().select(recipe.getVisibility() == null ? "PRIVATE" : recipe.getVisibility());

            grid.addRow(0, new javafx.scene.control.Label("Title*:"), titleField);
            grid.addRow(1, new javafx.scene.control.Label("Category*:"), categoryField);
            grid.addRow(2, new javafx.scene.control.Label("Prep time (min)*:"), prepField);
            grid.addRow(3, new javafx.scene.control.Label("Cook time (min)*:"), cookField);
            grid.addRow(4, new javafx.scene.control.Label("Servings*:"), servingsField);
            grid.addRow(5, new javafx.scene.control.Label("Difficulty*:"), difficultyField);
            grid.addRow(6, new javafx.scene.control.Label("Description*:"), descriptionEditArea);
            grid.addRow(7, new javafx.scene.control.Label("Visibility:"), visibilityBox);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        int cookTime = Integer.parseInt(cookField.getText().trim());
                        int prepTime = Integer.parseInt(prepField.getText().trim());
                        int servings = Integer.parseInt(servingsField.getText().trim());

                        recipe.setTitle(titleField.getText().trim());
                        recipe.setCategory(categoryField.getText().trim());
                        recipe.setPreparationTime(prepTime);
                        recipe.setCookingTime(cookTime);
                        recipe.setServings(servings);
                        recipe.setDifficulty(difficultyField.getText().trim());
                        recipe.setDescription(descriptionEditArea.getText().trim());
                        recipe.setVisibility(visibilityBox.getSelectionModel().getSelectedItem());

                        recipeService.saveRecipe(recipe);
                        return recipe;
                    } catch (NumberFormatException e) {
                        showError("Preparation time, cooking time and servings must be numbers.");
                    }
                }
                return null;
            });

            dialog.showAndWait().ifPresent(updated -> {
                populateDetails();
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Recipe Updated");
                info.setHeaderText(null);
                info.setContentText("Recipe details were updated.");
                info.showAndWait();
            });
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

    // removed navigateToListAndEdit: editing is now handled inline on this page

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
