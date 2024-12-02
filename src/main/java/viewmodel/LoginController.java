package viewmodel;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import service.UserSession;

import java.util.Objects;
import java.util.prefs.Preferences;

public class LoginController {
    @FXML
    private GridPane rootPane;
    @FXML
    private ImageView collegeImage;
    @FXML
    private Button loginBtn;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label passwordLabel;
    @FXML
    private Button signUpButton;
    @FXML
    private Label usernameLabel;
    @FXML
    private TextField usernameField;


    public void initialize() {
        rootPane.setBackground(new Background(
                createImage("https://edencoding.com/wp-content/uploads/2021/03/layer_06_1920x1080.png"),
                null,
                null,
                null,
                null,
                null
        ));

        rootPane.setOpacity(0);
        FadeTransition fadeOut2 = new FadeTransition(Duration.seconds(10), rootPane);
        fadeOut2.setFromValue(0);
        fadeOut2.setToValue(1);
        fadeOut2.play();
    }

    private static BackgroundImage createImage(String url) {
        return new BackgroundImage(
                new Image(url),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
                new BackgroundPosition(Side.LEFT, 0, true, Side.BOTTOM, 0, true),
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, false, true));
    }

    @FXML
    public void handleLogin(ActionEvent actionEvent) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Access stored credentials from Preferences
        Preferences userPreferences = Preferences.userRoot().node(SignupController.class.getName());
        String storedUsername = userPreferences.get("USERNAME", null);
        String storedPassword = userPreferences.get("PASSWORD", null);

        if (storedUsername == null || storedPassword == null) {
            showErrorAlert("No account found. Please sign up first.");
            return;
        }

        // Validate credentials
        if (username.equals(storedUsername) && password.equals(storedPassword)) {
            // Login successful, set user session
            UserSession.getInstance(storedUsername, storedPassword);

            // Redirect to the main page
            navigateToMainPage(actionEvent);
        } else {
            showErrorAlert("Invalid username or password. Please try again.");
        }
    }

    private void navigateToMainPage(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/db_interface_gui.fxml")));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Failed to load the application interface.");
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void signUp(ActionEvent actionEvent) {
        System.out.println("Navigating to sign-up page...");
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/signUp.fxml")));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            System.err.println("Error loading signUp.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
