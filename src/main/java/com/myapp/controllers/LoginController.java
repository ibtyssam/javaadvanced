package com.myapp.controllers;

import java.net.URL;

import com.myapp.models.User;
import com.myapp.services.AuthService;
import com.myapp.services.AuthServiceImpl;
import com.myapp.services.SessionManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private final AuthService authService = new AuthServiceImpl();

    @FXML
    public void handleLogin(ActionEvent event) {
        try {
            String email = emailField.getText();
            String password = passwordField.getText();
            if (email == null || email.isBlank() || password == null || password.isBlank()) {
                showError("Missing info", "Please enter both email and password.");
                return;
            }
            User user = authService.login(email, password);
            if (user != null) {
                SessionManager.setCurrentUser(user);
                navigateToHome(event);
            } else {
                showError("Login failed", "Invalid email or password");
            }
        } catch (Exception e) {
            showError("Error", "Failed to login: " + e.getMessage());
        }
    }

    @FXML
    public void handleGoToRegister(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/views/register.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Stage stage = (Stage)((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            URL cssUrl = getClass().getResource("/styles/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            stage.setScene(scene);
            stage.setTitle("Recipe Management System - Register");
            stage.show();
        } catch (Exception e) {
            showError("Error", "Failed to open register page: " + e.getMessage());
        }
    }

    private void navigateToHome(ActionEvent event) throws Exception {
        // Navigate directly to the recipe list after successful login
        URL fxmlUrl = getClass().getResource("/views/recipe-list.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        Stage stage = (Stage)((javafx.scene.Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 800, 600);
        URL cssUrl = getClass().getResource("/styles/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        stage.setScene(scene);
        stage.setTitle("Recipe Manager - All Recipes");
        stage.show();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
