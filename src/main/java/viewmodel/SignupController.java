package viewmodel;

import dao.DbConnectivityClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Person;

import java.io.IOException;

public class SignupController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label statusLabel;

    private final DbConnectivityClass db = new DbConnectivityClass();

    @FXML
    private void handleSubmit() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Fields cannot be empty!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Passwords do not match!");
            return;
        }

        if (db.isUsernameExists(username)) {
            statusLabel.setText("Username already exists.");
            return;
        }

        Person newUser = new Person();
        newUser.setFirstName(username);
        newUser.setPassword(password); // Store plain text password as per requirement

        db.insertUser(newUser);
        statusLabel.setText("Account created successfully!");

        // Optional: Clear fields after successful registration
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
