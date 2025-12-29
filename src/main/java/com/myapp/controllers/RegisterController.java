package com.myapp.controllers;

import java.net.URL;

import com.myapp.models.User;
import com.myapp.services.AuthService;
import com.myapp.services.AuthServiceImpl;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;

    private final AuthService authService = new AuthServiceImpl();

    @FXML
    public void handleRegister(ActionEvent event) {
        try {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String confirm = confirmField.getText();

            if (name == null || name.isBlank() || email == null || email.isBlank() || password == null || password.isBlank()) {
                showError("Missing info", "Please fill in all fields.");
                return;
            }
            if (!password.equals(confirm)) {
                showError("Password mismatch", "Password and confirm do not match.");
                return;
            }

            User created = authService.register(name, email, password);
            if (created != null) {
                showInfo("Registration successful", "You can now login.");
                navigateToLogin(event);
            }
        } catch (IllegalArgumentException iae) {
            // Show friendly message (e.g., duplicate email)
            showError("Registration failed", iae.getMessage());
        } catch (Exception e) {
            // Avoid leaking DAO/internal messages to user
            showError("Registration failed", "Please try again later.");
            System.err.println("Register error: " + e.getMessage());
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        try {
            navigateToLogin(event);
        } catch (Exception e) {
            showError("Error", "Failed to go back: " + e.getMessage());
        }
    }

    private void navigateToLogin(ActionEvent event) throws Exception {
        URL fxmlUrl = getClass().getResource("/views/login.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        Stage stage = (Stage)((javafx.scene.Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 800, 600);
        URL cssUrl = getClass().getResource("/styles/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        stage.setScene(scene);
        stage.setTitle("Recipe Management System - Login");
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
