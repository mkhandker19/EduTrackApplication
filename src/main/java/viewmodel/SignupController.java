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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SignupController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label statusLabel;

    // Handle the "Submit" button action
    @FXML
    private void handleSubmit() {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (validateInputs(username, password, confirmPassword)) {
                // Save to database
                if (saveToDatabase(username, password)) { // Pass plain text password
                    statusLabel.setText("Account created successfully!");
                    clearFields();

                    // Navigate to login or main page (optional)
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.close(); // Close signup window
                } else {
                    statusLabel.setText("Error: Username already exists.");
                }
            }
        }

        // Handle the "Back" button action
    @FXML
    private void handleBack() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close(); // Close current window
        // Open the previous login/signup page (implement logic here)
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
            person.setLastName("N/A");      // Placeholder for the last name
            person.setDepartment("N/A");   // Placeholder for the department
            person.setMajor("N/A");        // Placeholder for the major
            person.setEmail(username + "@example.com"); // Placeholder email
            person.setImageURL("");        // Leave Image URL empty

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
    private void handleContinueToSignUpForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/signUpForm.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) continueButton.getScene().getWindow(); // Replace with your button's fx:id
            stage.setScene(new Scene(root));
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

