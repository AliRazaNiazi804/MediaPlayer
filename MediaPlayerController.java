package com.mediaplayer.controller;

import com.mediaplayer.db.DatabaseUtil;
import com.mediaplayer.model.Media;
import com.mediaplayer.model.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class MediaPlayerController {

    @FXML
    private MenuBar menuBar;

    @FXML
    private MediaView mediaView;

    @FXML
    private VBox controlBox;

    @FXML
    private Button playPauseButton;

    @FXML
    private Button stopButton;

    @FXML
    private Button loopButton;

    @FXML
    private Slider speedSlider;

    @FXML
    private Slider progressSlider;

    @FXML
    private Label timeLabel;

    @FXML
    private CheckMenuItem darkModeMenuItem;

    private MediaPlayer mediaPlayer;
    private User currentUser;
    private Stage stage;
    private boolean isPlaying = false;
    private boolean isLooping = false;
    private File currentFile;

    public void setUser(User user) {
        this.currentUser = user;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        // Initialize controls
        speedSlider.setValue(1.0);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setRate(newVal.doubleValue());
            }
        });

        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null && !progressSlider.isValueChanging()) {
                Duration duration = mediaPlayer.getMedia().getDuration();
                mediaPlayer.seek(duration.multiply(newVal.doubleValue() / 100.0));
            }
        });

        // Update progress slider and time label
        Platform.runLater(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    if (!progressSlider.isValueChanging()) {
                        Duration duration = mediaPlayer.getMedia().getDuration();
                        progressSlider.setValue(newTime.toMillis() / duration.toMillis() * 100.0);
                        updateTimeLabel(newTime, duration);
                    }
                });
            }
        });
    }

    @FXML
    private void handleOpenFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Media File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.webm", "*.gif"),
            new FileChooser.ExtensionFilter("Audio Files", "*.mp3"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            loadMedia(file);
        }
    }

    @FXML
    private void handleSaveFile(ActionEvent event) {
        if (currentFile != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Media File");
            fileChooser.setInitialFileName(currentFile.getName());

            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    Files.copy(currentFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    showAlert("File saved successfully!");
                } catch (Exception e) {
                    showAlert("Error saving file: " + e.getMessage());
                }
            }
        } else {
            showAlert("No file to save.");
        }
    }

    @FXML
    private void handleDownload(ActionEvent event) {
        // For simplicity, this will just copy the current file to Downloads
        if (currentFile != null) {
            try {
                String userHome = System.getProperty("user.home");
                File downloadsDir = new File(userHome, "Downloads");
                File destFile = new File(downloadsDir, currentFile.getName());
                Files.copy(currentFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                showAlert("File downloaded to Downloads folder!");
            } catch (Exception e) {
                showAlert("Error downloading file: " + e.getMessage());
            }
        } else {
            showAlert("No file to download.");
        }
    }

    @FXML
    private void handlePlayPause(ActionEvent event) {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.pause();
                playPauseButton.setText("Play");
            } else {
                mediaPlayer.play();
                playPauseButton.setText("Pause");
            }
            isPlaying = !isPlaying;
        }
    }

    @FXML
    private void handleStop(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying = false;
            playPauseButton.setText("Play");
            progressSlider.setValue(0);
        }
    }

    @FXML
    private void handleLoop(ActionEvent event) {
        isLooping = !isLooping;
        if (mediaPlayer != null) {
            mediaPlayer.setCycleCount(isLooping ? MediaPlayer.INDEFINITE : 1);
        }
        loopButton.setText(isLooping ? "Loop On" : "Loop Off");
    }

    @FXML
    private void handleDarkMode(ActionEvent event) {
        Scene scene = stage.getScene();
        if (darkModeMenuItem.isSelected()) {
            scene.getRoot().setStyle("-fx-base: #2b2b2b; -fx-background: #2b2b2b;");
        } else {
            scene.getRoot().setStyle("");
        }
    }

    private void loadMedia(File file) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
            }

            javafx.scene.media.Media media = new javafx.scene.media.Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            currentFile = file;

            // Save to database
            Media mediaObj = new Media(file.getName(), file.getAbsolutePath(),
                file.getName().toLowerCase().endsWith(".mp3") ? "audio" : "video", currentUser.getId());
            DatabaseUtil.saveMedia(mediaObj);

            // Set up media player
            mediaPlayer.setOnReady(() -> {
                Duration duration = mediaPlayer.getMedia().getDuration();
                progressSlider.setMax(100.0);
                updateTimeLabel(Duration.ZERO, duration);
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                if (!isLooping) {
                    isPlaying = false;
                    playPauseButton.setText("Play");
                    progressSlider.setValue(0);
                }
            });

        } catch (Exception e) {
            showAlert("Error loading media: " + e.getMessage());
        }
    }

    private void updateTimeLabel(Duration current, Duration total) {
        timeLabel.setText(formatDuration(current) + " / " + formatDuration(total));
    }

    private String formatDuration(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%d:%02d", minutes, seconds);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Media Player");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
