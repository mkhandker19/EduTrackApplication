package viewmodel;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.awt.*;
import java.io.IOException;

import dao.DbConnectivityClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;


public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private DbConnectivityClass dbConnectivityClass;
    @FXML
    private GridPane rootpane;
    public void initialize() {
        rootpane.setBackground(new Background(
                        createImage("https://edencoding.com/wp-content/uploads/2021/03/layer_06_1920x1080.png"),
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );


        rootpane.setOpacity(0);
        FadeTransition fadeOut2 = new FadeTransition(Duration.seconds(10), rootpane);
        fadeOut2.setFromValue(0);
        fadeOut2.setToValue(1);
        fadeOut2.play();
        dbConnectivityClass = new DbConnectivityClass(); // Ensure database connectivity class is initialized
    }
    private static BackgroundImage createImage(String url) {
        return new BackgroundImage(
                new Image(url),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
                new BackgroundPosition(Side.LEFT, 0, true, Side.BOTTOM, 0, true),
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, false, true));
    }
    @FXML
    public void login(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/db_interface_gui.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void signUp() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/signUp.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin() {
        // Retrieve input from username and password fields
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Check if fields are empty
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password.");
            return;
        }

        // Call the database validation method
        DbConnectivityClass dbConnectivityClass = null;
        boolean isValidUser = dbConnectivityClass.validateLogin(username, password);

        if (isValidUser) {
            statusLabel.setText("Login successful!");
            navigateToMainPage(); // Navigate to the main page of the application
        } else {
            statusLabel.setText("Invalid username or password.");
        }
    }
    private void navigateToMainPage() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/MainPage.fxml")); // Adjust path as needed
            Stage stage = (Stage) usernameField.getScene().getWindow(); // Assuming you use the login window
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading main application page.");
        }
    }

        private void navigateToMainApp() {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/view/mainApp.fxml"));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root, 900, 600));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Error loading main application.");
            }
        }


    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = rootpane.getScene(); // Use rootpane instead of menuBar
            Stage stage = (Stage) scene.getWindow();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

