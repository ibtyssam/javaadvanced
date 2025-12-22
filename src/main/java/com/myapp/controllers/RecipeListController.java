package com.myapp.controllers;

import java.util.List;

import com.myapp.models.Recipe;
import com.myapp.services.RecipeService;
import com.myapp.services.RecipeServiceImpl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RecipeListController {
    @FXML private VBox root;
    @FXML private TableView<Recipe> recipeTable;
    @FXML private TableColumn<Recipe, String> titleColumn;
    @FXML private TableColumn<Recipe, String> categoryColumn;
    @FXML private TableColumn<Recipe, Integer> prepTimeColumn;
    @FXML private TableColumn<Recipe, Integer> cookTimeColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private Button addButton;
    @FXML private Button viewButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button closeButton;

    private final ObservableList<Recipe> recipeList = FXCollections.observableArrayList();
    private final RecipeService recipeService = new RecipeServiceImpl();

    @FXML
    public void initialize() {
        setupTable();
        setupEventHandlers();
        loadRecipesFromService();
        populateCategoryFilter();
    }

    private void setupTable() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        prepTimeColumn.setCellValueFactory(new PropertyValueFactory<>("preparationTime"));
        cookTimeColumn.setCellValueFactory(new PropertyValueFactory<>("cookingTime"));
        

        recipeTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                boolean itemSelected = newSelection != null;
                viewButton.setDisable(!itemSelected);
                editButton.setDisable(!itemSelected);
                deleteButton.setDisable(!itemSelected);
            });
    }

    @FXML
    private void handleAddRecipe() {
        showRecipeForm(null);
    }
    
    @FXML
    private void handleViewRecipe() {
        viewSelectedRecipe();
    }
    
    @FXML
    private void handleEditRecipe() {
        editSelectedRecipe();
    }
    
    @FXML
    private void handleDeleteRecipe() {
        deleteSelectedRecipe();
    }
    
    @FXML
    private void handleSearch(ActionEvent event) {
        System.out.println("=== SEARCH BUTTON CLICKED ===");
        String searchText = searchField.getText();
        System.out.println("Search field text: '" + searchText + "'");
        System.out.println("Search field is null: " + (searchField == null));
        System.out.println("Search field text is null: " + (searchText == null));
        filterRecipes();
    }
    
    @FXML
    private void handleClose() {
        try {
            Stage stage = null;
            
            // Try multiple ways to get the stage
            if (closeButton != null && closeButton.getScene() != null) {
                stage = (Stage) closeButton.getScene().getWindow();
            } else if (recipeTable != null && recipeTable.getScene() != null) {
                stage = (Stage) recipeTable.getScene().getWindow();
            } else if (root != null && root.getScene() != null) {
                stage = (Stage) root.getScene().getWindow();
            }
            
            if (stage != null) {
                stage.close();
                System.out.println("Window closed successfully");
            } else {
                System.err.println("Could not find stage to close");
            }
        } catch (Exception e) {
            System.err.println("Error closing window: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupEventHandlers() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Search text changed: " + newVal);
            filterRecipes();
        });
        
        categoryFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Category changed: " + newVal);
            filterRecipes();
        });
    }

    private void populateCategoryFilter() {
        try {
            List<Recipe> recipes = recipeService.getAllRecipes();
            List<String> categories = recipes.stream()
                    .map(Recipe::getCategory)
                    .filter(category -> category != null && !category.trim().isEmpty())
                    .distinct()
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
            
            // Ensure commonly used categories exist (only 'Dessert')
            if (!categories.stream().anyMatch(c -> c.equalsIgnoreCase("Dessert"))) {
                categories.add("Dessert");
            }

            // Prepend "All" option
            categories.removeIf(c -> c.equalsIgnoreCase("All"));
            categories.add(0, "All");

            categoryFilter.setItems(FXCollections.observableArrayList(categories));
            // Default selection to All
            categoryFilter.getSelectionModel().selectFirst();
        } catch (Exception e) {
            System.err.println("Failed to populate categories: " + e.getMessage());
        }
    }

    private void loadRecipesFromService() {
        try {
            List<Recipe> recipes = recipeService.getAllRecipes();
            recipeList.setAll(recipes);
            recipeTable.setItems(recipeList);
        } catch (Exception e) {
            showAlert("Error", "Failed to load recipes: " + e.getMessage());
        }
    }

    private void filterRecipes() {
        String query = searchField != null ? searchField.getText() : null;
        String category = categoryFilter != null ? categoryFilter.getSelectionModel().getSelectedItem() : null;

        System.out.println("=== FILTER RECIPES START ===");
        System.out.println("Query: '" + query + "'");
        System.out.println("Category: '" + category + "'");
        System.out.println("Query empty: " + (query == null || query.trim().isEmpty()));

        try {
            // Use service-level search (title startsWith, others contain)
            List<Recipe> filtered = recipeService.searchRecipes(query == null ? "" : query.trim());
            System.out.println("After service search: " + filtered.size() + " recipes");

            // Apply category filter if a specific category is selected (not All)
            if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("All")) {
                String lowerCaseCategory = category.toLowerCase().trim();
                System.out.println("Applying category filter for: '" + lowerCaseCategory + "'");
                filtered = filtered.stream()
                        .filter(recipe ->
                                recipe != null &&
                                recipe.getCategory() != null &&
                                recipe.getCategory().toLowerCase().contains(lowerCaseCategory)
                        )
                        .collect(java.util.stream.Collectors.toList());
                System.out.println("After category filter: " + filtered.size() + " recipes");
            }

            recipeList.setAll(filtered);
            recipeTable.setItems(recipeList);
            System.out.println("Final result: " + filtered.size() + " recipes displayed");
            System.out.println("=== FILTER RECIPES END ===");
        } catch (Exception e) {
            System.err.println("Error in filterRecipes: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to filter recipes: " + e.getMessage());
        }
    }

    private void showRecipeForm(Recipe recipe) {
        Dialog<Recipe> dialog = new Dialog<>();
        dialog.setTitle(recipe == null ? "Add Recipe" : "Edit Recipe");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField titleField = new TextField();
        TextField categoryField = new TextField();
        TextField prepField = new TextField();
        TextField cookField = new TextField();
        TextField servingsField = new TextField();
        TextField difficultyField = new TextField();
        TextArea descriptionArea = new TextArea();

        if (recipe != null) {
            titleField.setText(recipe.getTitle());
            categoryField.setText(recipe.getCategory());
            prepField.setText(String.valueOf(recipe.getPreparationTime()));
            cookField.setText(String.valueOf(recipe.getCookingTime()));
            servingsField.setText(String.valueOf(recipe.getServings()));
            difficultyField.setText(recipe.getDifficulty());
            descriptionArea.setText(recipe.getDescription());
        }

        grid.addRow(0, new Label("Title*:"), titleField);
        grid.addRow(1, new Label("Category*:"), categoryField);
        grid.addRow(2, new Label("Prep time (min)*:"), prepField);
        grid.addRow(3, new Label("Cook time (min)*:"), cookField);
        grid.addRow(4, new Label("Servings*:"), servingsField);
        grid.addRow(5, new Label("Difficulty*:"), difficultyField);
        grid.addRow(6, new Label("Description*:"), descriptionArea);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    if (cookField.getText() == null || prepField.getText() == null || servingsField.getText() == null) {
                        showAlert("shshhs", "ssss");
                    }

                    int cookTime = Integer.parseInt(cookField.getText().trim());

                    int prepTime = Integer.parseInt(prepField.getText().trim());

                    int servings = Integer.parseInt(servingsField.getText().trim());

                    Recipe toSave = recipe != null ? recipe : new Recipe();
                    toSave.setTitle(titleField.getText().trim());
                    toSave.setCategory(categoryField.getText().trim());
                    toSave.setPreparationTime(prepTime);
                    toSave.setCookingTime(cookTime);
                    toSave.setServings(servings);
                    toSave.setDifficulty(difficultyField.getText().trim());
                    toSave.setDescription(descriptionArea.getText().trim());

                    recipeService.saveRecipe(toSave);
                    return toSave;
                } catch (NumberFormatException e) {
                    showAlert("Invalid input", "Preparation time, cooking time and servings must be numbers.");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(saved -> loadRecipesFromService());
    }

    private void viewSelectedRecipe() {
        Recipe selected = recipeTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            StringBuilder details = new StringBuilder();
            details.append("Title: ").append(selected.getTitle()).append("\n");
            details.append("Category: ").append(selected.getCategory()).append("\n");
            details.append("Difficulty: ").append(selected.getDifficulty()).append("\n");
            details.append("Servings: ").append(selected.getServings()).append("\n");
            details.append("Prep time: ").append(selected.getPreparationTime()).append(" min\n");
            details.append("Cook time: ").append(selected.getCookingTime()).append(" min\n\n");

            details.append("Description:\n");
            details.append(selected.getDescription() != null ? selected.getDescription() : "").append("\n\n");

            details.append("Ingredients:\n");
            if (selected.getIngredients() != null && !selected.getIngredients().isEmpty()) {
                for (int i = 0; i < selected.getIngredients().size(); i++) {
                    details.append(" - ").append(selected.getIngredients().get(i)).append("\n");
                }
            } else {
                details.append(" (none)\n");
            }

            details.append("\nInstructions:\n");
            if (selected.getInstructions() != null && !selected.getInstructions().isEmpty()) {
                for (int i = 0; i < selected.getInstructions().size(); i++) {
                    details.append(" ").append(i + 1).append(". ").append(selected.getInstructions().get(i)).append("\n");
                }
            } else {
                details.append(" (none)\n");
            }

            showAlert("View Recipe", details.toString());
        }
    }

    private void editSelectedRecipe() {
        Recipe selected = recipeTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showRecipeForm(selected);
        }
    }

    private void deleteSelectedRecipe() {
        Recipe selected = recipeTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Recipe");
            confirm.setHeaderText(null);
            confirm.setContentText("Are you sure you want to delete '" + selected.getTitle() + "'?");

            confirm.showAndWait().ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    try {
                        recipeService.deleteRecipe(selected.getId());
                        recipeList.remove(selected);
                    } catch (Exception e) {
                        showAlert("Error", "Failed to delete recipe: " + e.getMessage());
                    }
                }
            });
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
