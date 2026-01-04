package com.myapp.controllers;

import java.util.List;

import com.myapp.models.Ingredient;
import com.myapp.models.Recipe;
import com.myapp.services.RecipeService;
import com.myapp.services.RecipeServiceImpl;
import com.myapp.services.SessionManager;

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
    @FXML private Button closeButton;
    
    @FXML
    private void handleLogout() {
        try {
            com.myapp.services.SessionManager.logout();
            javafx.stage.Stage stage = null;
            if (closeButton != null && closeButton.getScene() != null) {
                stage = (javafx.stage.Stage) closeButton.getScene().getWindow();
            } else if (recipeTable != null && recipeTable.getScene() != null) {
                stage = (javafx.stage.Stage) recipeTable.getScene().getWindow();
            } else if (root != null && root.getScene() != null) {
                stage = (javafx.stage.Stage) root.getScene().getWindow();
            }
            if (stage == null) {
                throw new IllegalStateException("Cannot locate window for logout");
            }
            java.net.URL fxmlUrl = getClass().getResource("/views/login.fxml");
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxmlUrl);
            javafx.scene.Parent loginRoot = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(loginRoot, 800, 600);
            java.net.URL cssUrl = getClass().getResource("/styles/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            stage.setScene(scene);
            stage.setTitle("Recipe Management System - Login");
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Failed to logout: " + e.getMessage());
        }
    }

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
            });

        // Open details on double click
        recipeTable.setRowFactory(table -> {
            javafx.scene.control.TableRow<Recipe> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    Recipe selected = row.getItem();
                    openRecipeDetails(selected);
                }
            });
            return row;
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
    
    // Edit/Delete removed from list page; actions are available on detail page
    
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

                    // Final safety dedupe by recipe id
                    java.util.Map<Integer, Recipe> byId = filtered.stream()
                        .filter(r -> r != null && r.getId() != null)
                        .collect(java.util.stream.Collectors.toMap(
                            Recipe::getId,
                            r -> r,
                            (a,b) -> a,
                            java.util.LinkedHashMap::new
                        ));
                    java.util.List<Recipe> noId = filtered.stream()
                        .filter(r -> r != null && r.getId() == null)
                        .collect(java.util.stream.Collectors.toList());
                    filtered = new java.util.ArrayList<>(byId.values());
                    filtered.addAll(noId);
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
        dialog.setResizable(true);

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
        TextArea ingredientsArea = new TextArea();
        TextArea instructionsArea = new TextArea();
        // Keep text areas compact and readable
        descriptionArea.setWrapText(true);
        ingredientsArea.setWrapText(true);
        instructionsArea.setWrapText(true);
        descriptionArea.setPrefRowCount(5);
        ingredientsArea.setPrefRowCount(6);
        instructionsArea.setPrefRowCount(8);
        ComboBox<String> visibilityBox = new ComboBox<>();
        visibilityBox.setItems(FXCollections.observableArrayList("PUBLIC", "PRIVATE"));
        visibilityBox.getSelectionModel().select("PRIVATE");

        if (recipe != null) {
            titleField.setText(recipe.getTitle());
            categoryField.setText(recipe.getCategory());
            prepField.setText(String.valueOf(recipe.getPreparationTime()));
            cookField.setText(String.valueOf(recipe.getCookingTime()));
            servingsField.setText(String.valueOf(recipe.getServings()));
            difficultyField.setText(recipe.getDifficulty());
            descriptionArea.setText(recipe.getDescription());
            // Prefill ingredients as lines: name;quantity;unit;notes
            if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
                String ingText = recipe.getIngredients().stream()
                        .map(i -> {
                            String q = i.getQuantity() > 0 ? String.valueOf(i.getQuantity()) : "";
                            String u = i.getUnit() != null ? i.getUnit() : "";
                            String n = i.getNotes() != null ? i.getNotes() : "";
                            return String.join(";", java.util.stream.Stream.of(i.getName(), q, u, n)
                                    .map(s -> s == null ? "" : s).toArray(String[]::new));
                        })
                        .collect(java.util.stream.Collectors.joining("\n"));
                ingredientsArea.setText(ingText);
            }
            // Prefill instructions one per line
            if (recipe.getInstructions() != null && !recipe.getInstructions().isEmpty()) {
                String instrText = String.join("\n", recipe.getInstructions());
                instructionsArea.setText(instrText);
            }
        }
        if (recipe != null && recipe.getVisibility() != null) {
            visibilityBox.getSelectionModel().select(recipe.getVisibility());
        }

        grid.addRow(0, new Label("Title*:"), titleField);
        grid.addRow(1, new Label("Category*:"), categoryField);
        grid.addRow(2, new Label("Prep time (min)*:"), prepField);
        grid.addRow(3, new Label("Cook time (min)*:"), cookField);
        grid.addRow(4, new Label("Servings*:"), servingsField);
        grid.addRow(5, new Label("Difficulty*:"), difficultyField);
        grid.addRow(6, new Label("Description*:"), descriptionArea);
        grid.addRow(7, new Label("Ingredients (name;qty;unit;notes):"), ingredientsArea);
        grid.addRow(8, new Label("Instructions (one per line):"), instructionsArea);
        grid.addRow(9, new Label("Visibility:"), visibilityBox);

        // Wrap content to avoid oversized dialog and keep buttons visible
        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefSize(700, 600);

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
                    // Parse ingredients
                    java.util.List<Ingredient> parsedIngredients = new java.util.ArrayList<>();
                    String ingText = ingredientsArea.getText();
                    if (ingText != null && !ingText.trim().isEmpty()) {
                        String[] lines = ingText.split("\r?\n");
                        for (String line : lines) {
                            if (line == null) continue;
                            String trimmed = line.trim();
                            if (trimmed.isEmpty()) continue;
                            String[] parts = trimmed.split(";", -1);
                            String name = parts.length > 0 ? parts[0].trim() : "";
                            if (name.isEmpty()) continue;
                            double qty = 0.0;
                            if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                                try { qty = Double.parseDouble(parts[1].trim()); } catch (Exception ignore) {}
                            }
                            String unit = parts.length > 2 ? parts[2].trim() : "";
                            String notes = parts.length > 3 ? parts[3].trim() : "";
                            Ingredient ing = new Ingredient();
                            ing.setName(name);
                            ing.setQuantity(qty);
                            ing.setUnit(unit);
                            ing.setNotes(notes);
                            parsedIngredients.add(ing);
                        }
                    }
                    toSave.setIngredients(parsedIngredients);
                    // Parse instructions
                    java.util.List<String> parsedInstructions = new java.util.ArrayList<>();
                    String instrText = instructionsArea.getText();
                    if (instrText != null && !instrText.trim().isEmpty()) {
                        String[] lines = instrText.split("\r?\n");
                        for (String l : lines) {
                            if (l != null && !l.trim().isEmpty()) parsedInstructions.add(l.trim());
                        }
                    }
                    toSave.setInstructions(parsedInstructions);
                    toSave.setVisibility(visibilityBox.getSelectionModel().getSelectedItem());
                    if (SessionManager.isLoggedIn() && SessionManager.getCurrentUser() != null) {
                        toSave.setOwnerUserId(SessionManager.getCurrentUser().getId());
                    }

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
            openRecipeDetails(selected);
        }
    }

    private void openRecipeDetails(Recipe selected) {
        try {
            java.net.URL fxmlUrl = getClass().getResource("/views/recipe-detail.fxml");
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxmlUrl);
            javafx.scene.Parent root = loader.load();
            com.myapp.controllers.RecipeDetailController controller = loader.getController();
            controller.setRecipe(selected);

            javafx.stage.Stage stage = (javafx.stage.Stage) recipeTable.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 800, 600);
            java.net.URL cssUrl = getClass().getResource("/styles/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            stage.setScene(scene);
            stage.setTitle("Recipe Details - " + selected.getTitle());
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Failed to open recipe details: " + e.getMessage());
        }
    }

    // Deletion is now handled on the detail page

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
