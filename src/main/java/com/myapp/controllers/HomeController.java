package com.myapp.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class HomeController {
    
    @FXML
    public void handleGetStarted(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/recipe-list.fxml"));
            Parent root = loader.load();
            

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            
            // Set the new scene
            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/styles/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            stage.setScene(scene);
            stage.setTitle("Recipe Manager - All Recipes");
            stage.show();
            
        } catch (IOException e) {
            showError("Error", "Failed to load recipe list: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
