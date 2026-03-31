package com.mediaplayer.controller;

import com.mediaplayer.Main;
import com.mediaplayer.db.DatabaseUtil;
import com.mediaplayer.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button signupButton;

    @FXML
    private Label messageLabel;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        User user = DatabaseUtil.loginUser(email, password);
        if (user != null) {
            // Login successful, open media player
            openMediaPlayer(user);
        } else {
            messageLabel.setText("Invalid email or password.");
        }
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SignupView.fxml"));
            Parent root = loader.load();
            SignupController signupController = loader.getController();
            signupController.setStage(stage);

            Scene scene = new Scene(root, 400, 500);
            stage.setScene(scene);
            stage.setTitle("Media Player - Sign Up");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openMediaPlayer(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MediaPlayerView.fxml"));
            Parent root = loader.load();
            MediaPlayerController mediaPlayerController = loader.getController();
            mediaPlayerController.setUser(user);
            mediaPlayerController.setStage(stage);

            Scene scene = new Scene(root, 900, 700);
            stage.setScene(scene);
            stage.setTitle("Media Player");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
