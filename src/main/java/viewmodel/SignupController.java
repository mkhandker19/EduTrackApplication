package viewmodel;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.prefs.Preferences;

public class SignupController {
    private final String usernameRegex = "^[a-zA-Z0-9]{5,20}$"; // Alphanumeric, 5-20 chars
    private final String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"; // At least 1 upper, 1 lower, 1 digit, 8+ chars
    private final String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z]+\\.[a-zA-Z]{2,6}$"; // Standard email format

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField emailField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button goBackBtn;

    @FXML
    private Button submitBtn;

    @FXML
    private Label signupValidationMessage;

    @FXML
    public void initialize() {
        // Add focus listeners for fields
        addFocusListeners();

        // BooleanBinding to check if all fields are valid
        BooleanBinding isFormValid = Bindings.createBooleanBinding(() ->
                        !usernameField.getText().matches(usernameRegex) ||
                                !passwordField.getText().matches(passwordRegex) ||
                                !confirmPasswordField.getText().equals(passwordField.getText()) ||
                                !emailField.getText().matches(emailRegex),
                usernameField.textProperty(),
                passwordField.textProperty(),
                confirmPasswordField.textProperty(),
                emailField.textProperty()
        );

        // Disable "New Account" button if the form is invalid
        submitBtn.disableProperty().bind(isFormValid);
    }

    private void addFocusListeners() {
        usernameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                checkValidity(usernameField, usernameRegex, "Username");
            }
        });

        passwordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                checkValidity(passwordField, passwordRegex, "Password");
            }
        });

        confirmPasswordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                checkPasswordMatch();
            }
        });

        emailField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                checkValidity(emailField, emailRegex, "Email");
            }
        });
    }

    private void checkValidity(TextField field, String regex, String fieldName) {
        if (field.getText().matches(regex)) {
            statusLabel.setText(fieldName + " is valid.");
        } else {
            statusLabel.setText(fieldName + " is invalid.");
        }
    }

    private void checkPasswordMatch() {
        if (confirmPasswordField.getText().equals(passwordField.getText())) {
            statusLabel.setText("Passwords match.");
        } else {
            statusLabel.setText("Passwords do not match.");
        }
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        emailField.clear();
    }

    public void createNewAccount(ActionEvent actionEvent) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();

        // Access Preferences
        Preferences userPreferences = Preferences.userRoot().node(SignupController.class.getName());
        String existingUsername = userPreferences.get("USERNAME", null);

        // Check if the username already exists
        if (existingUsername != null && existingUsername.equals(username)) {
            signupValidationMessage.setText("Username already exists.");
            return;
        }

        // Save credentials to Preferences
        userPreferences.put("USERNAME", username);
        userPreferences.put("PASSWORD", password);
        userPreferences.put("EMAIL", email);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Account Created");
        alert.setContentText("Your account has been successfully created.");
        alert.showAndWait();

        clearFields();
        goBack(actionEvent);
    }

    @FXML
    private void goBack(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/login.fxml")));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
