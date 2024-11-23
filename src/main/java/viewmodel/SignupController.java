package viewmodel;

import dao.DbConnectivityClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Person;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SignupController {
    @FXML
    private Button submitButton;
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label statusLabel;

    @FXML
    private void handleSubmit() {
        // Retrieve input values
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate inputs
        if (validateInputs(username, password, confirmPassword)) {
            // Attempt to save the user to the database
            if (saveToDatabase(username, password)) {
                statusLabel.setText("Account created successfully!");
                clearFields();

                // Navigate back to the login page after successful registration
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.setScene(new Scene(root, 900, 600));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    statusLabel.setText("Error navigating to login page.");
                }
            } else {
                // Handle duplicate username
                statusLabel.setText("Error: Username already exists.");
            }
        }
    }
    private boolean validateInputs(String username, String password, String confirmPassword) {
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Fields cannot be empty!");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Passwords do not match!");
            return false;
        }
        return true;
    }

    private boolean saveToDatabase(String username, String password) {
        try {
            DbConnectivityClass db = new DbConnectivityClass();

            // Check if the username already exists
            if (db.isUsernameExists(username)) {
                statusLabel.setText("Error: Username already exists.");
                return false;
            }

            // Create a Person object with placeholder values
            Person person = new Person();
            person.setFirstName(username);  // Use the username as the first name
            // Store the plain text password as part of the user session (optional)
            person.setPassword(password);  // Add this to your Person model if needed

            // Insert the user into the database
            db.insertUser(person);

            return true; // Return true if the operation was successful
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Return false in case of an error
        }
    }


    @FXML
    private void goBack(javafx.event.ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void continueToSignUpForm(javafx.event.ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/signUpForm.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

}

