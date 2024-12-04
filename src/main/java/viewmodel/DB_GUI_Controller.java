package viewmodel;

import dao.storageUploader;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.MenuBar;

import javafx.scene.control.ComboBox;
import com.azure.storage.blob.BlobClient;
import dao.DbConnectivityClass;
import dao.storageUploader;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Person;
import service.MyLogger;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DB_GUI_Controller implements Initializable {
    final static String DB_NAME="csc311_bd_temp";
    MyLogger lg= new MyLogger();
    final static String SQL_SERVER_URL = "jdbc:mysql://csc311khandkerserver.mysql.database.azure.com";//update this server name
    final static String DB_URL = "jdbc:mysql://csc311khandkerserver.mysql.database.azure.com/"+DB_NAME;//update this database name
    final static String USERNAME = "csc311admin";// update this username
    final static String PASSWORD = "farmingdale24@.";// update this password
    //added edit btn
    @FXML
    private Button editBtn;
    //added delete Btn
    @FXML
    private Button deleteBtn;
    //added add btn
    @FXML
    private Button addBtn;
    //added clear btn
    @FXML
    private Button clearBtn;
    //added a status message
    @FXML
    private Label statusLabel;
    //added progress bar
    @FXML
    ProgressBar progressBar;
    storageUploader store = new storageUploader();
    //added major dropdown
    @FXML
    private ComboBox<Major> majorComboBox;
    //menu items under edit
    @FXML
    private MenuItem editItem;
    @FXML
    private MenuItem deleteItem;
    @FXML
    private MenuItem ClearItem;
    @FXML
    private MenuItem CopyItem;
    @FXML
    private MenuItem exportCSV;
    @FXML
    private MenuItem importCSV;
    @FXML
    TextField first_name, last_name, department, email, imageURL;
    @FXML
    ImageView img_view;
    @FXML
    MenuBar menuBar;
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;
    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();
    public boolean connectToDatabase() {
        boolean isConnected = false;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            Statement statement = conn.createStatement();

            // Create the database if it does not exist
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);

            // Connect to the specific database
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);

            // Create the EduTrack table if it does not exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                    + "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                    + "first_name VARCHAR(200) NOT NULL, "
                    + "last_name VARCHAR(200) NOT NULL, "
                    + "department VARCHAR(200), "
                    + "major VARCHAR(200), "
                    + "email VARCHAR(200) NOT NULL UNIQUE, "
                    + "imageURL VARCHAR(200))";
            statement.executeUpdate(createTableSQL);

            isConnected = true;
            statement.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isConnected;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Set up TableView columns
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));

            // Refresh data from the database
            refreshTableView();

            // Handle keyboard shortcuts for menu items
            setupKeyboardShortcuts();

            // Setup actions for menu items
            setupMenuItemActions();

            // Add listeners for form validation
            addFormValidationListeners();

            // Initialize the ComboBox with enum values
            majorComboBox.setItems(FXCollections.observableArrayList(Major.values()));
            majorComboBox.getSelectionModel().selectFirst(); // Default selection

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private boolean isValidName(String name) {
        String nameRegex = "^[A-Za-z\\s]+$"; // Only letters and spaces
        return name.matches(nameRegex);
    }
    private boolean isValidDepartment(String department) {
        String departmentRegex = "^[A-Za-z\\s-]+$"; // Letters, spaces, and hyphens
        return department.matches(departmentRegex);
    }
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"; // Standard email pattern
        return email.matches(emailRegex);
    }
    private boolean isValidImageURL(String url) {
        String imageURLRegex = "^(http|https)://.*\\.(jpg|jpeg|png|gif|bmp|webp)$"; // URL with image extension
        return url.matches(imageURLRegex);
    }


    // Refresh TableView data
    private void refreshTableView() {
        data.clear(); // Clear existing data
        data.addAll(cnUtil.getData()); // Add fresh data from the database
        tv.setItems(data); // Set the refreshed data to the TableView
    }

    // Setup keyboard shortcuts for menu items
    private void setupKeyboardShortcuts() {
        Platform.runLater(() -> {
            if (menuBar.getScene() != null) {
                menuBar.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.isControlDown()) {
                        if (event.getCode() == KeyCode.E) editRecord();
                        if (event.getCode() == KeyCode.D) deleteRecord();
                        if (event.getCode() == KeyCode.R) clearForm();
                        if (event.getCode() == KeyCode.C) copyRecord();
                    }
                });
            }
        });
    }

    // Setup actions for menu items
    private void setupMenuItemActions() {
        editItem.setOnAction(event -> editRecord());
        deleteItem.setOnAction(event -> deleteRecord());
        CopyItem.setOnAction(event -> copyRecord());

        // Disable buttons and menu items initially
        editBtn.setDisable(true);
        deleteBtn.setDisable(true);
        editItem.setDisable(true);
        deleteItem.setDisable(true);
        ClearItem.setDisable(true);
        CopyItem.setDisable(true);

        // Add listener for TableView selection
        tv.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean isSelected = newValue != null;
            editBtn.setDisable(!isSelected);
            deleteBtn.setDisable(!isSelected);
            editItem.setDisable(!isSelected);
            deleteItem.setDisable(!isSelected);
            CopyItem.setDisable(!isSelected);
        });
    }

    // Add listeners for form validation
    private void addFormValidationListeners() {
        first_name.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        last_name.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        department.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        majorComboBox.valueProperty().addListener((observable, oldValue, newValue) -> validateForm());
        email.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        imageURL.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
    }

    // Validate form for enabling/disabling the Add button
    private void validateForm() {
        boolean isFormValid = !first_name.getText().isEmpty() &&
                !last_name.getText().isEmpty() &&
                !department.getText().isEmpty() &&
                majorComboBox.getValue() != null &&
                !email.getText().isEmpty() &&
                isValidEmail(email.getText()) &&
                !imageURL.getText().isEmpty();

        addBtn.setDisable(!isFormValid);
    }

    @FXML
    protected void importCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());

        if (file != null) {
            try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
                String line;

                // Skip the header row (if present)
                reader.readLine();

                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(","); // Adjust based on your CSV delimiter

                    // Ensure the CSV line matches the expected schema
                    if (data.length == 7) { // Assuming your CSV includes ID, firstName, lastName, department, major, email, imageURL
                        // Create a new Person object
                        Person person = new Person(
                                Integer.parseInt(data[0]), // ID
                                data[1],                   // First Name
                                data[2],                   // Last Name
                                data[3],                   // Department
                                data[4],                   // Major
                                data[5],                   // Email
                                data[6]                    // Image URL
                        );

                        // Insert the user into the database
                        cnUtil.insertUser(
                                Integer.parseInt(data[0]),  // ID
                                data[1],                   // First Name
                                data[2],                   // Last Name
                                data[3],                   // Department
                                data[4],                   // Major
                                data[5],                   // Email
                                data[6]                    // Image URL
                        );

                        // Add the user to the TableView's observable list
                        this.data.add(person);
                    } else {
                        statusLabel.setText("Invalid CSV format. Ensure all fields are present.");
                        break;
                    }
                }
                statusLabel.setText("Data imported successfully.");
            } catch (Exception e) {
                statusLabel.setText("Error importing data.");
                e.printStackTrace();
            }
        }
    }

    public void insertUser(int id, String firstName, String lastName, String department, String major, String email, String imageURL) {
        connectToDatabase();
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO users (id, first_name, last_name, department, major, email, imageURL) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, id); // Include ID
            preparedStatement.setString(2, firstName);
            preparedStatement.setString(3, lastName);
            preparedStatement.setString(4, department);
            preparedStatement.setString(5, major);
            preparedStatement.setString(6, email);
            preparedStatement.setString(7, imageURL);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





    //CSV file export method
    @FXML
    protected void exportCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(menuBar.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Writing the header to the CSV
                writer.append("ID,First Name,Last Name,Department,Major,Email,Image URL\n");

                // Writing each person data to the CSV
                for (Person person : data) {
                    writer.append(person.getId() + ",");
                    writer.append(person.getFirstName() + ",");
                    writer.append(person.getLastName() + ",");
                    writer.append(person.getDepartment() + ",");
                    writer.append(person.getMajor() + ",");
                    writer.append(person.getEmail() + ",");
                    writer.append(person.getImageURL() + "\n");
                }
                // Show success message if it works
                statusLabel.setText("Data exported successfully.");
            } catch (Exception e) {
                statusLabel.setText("Error exporting data.");
                e.printStackTrace();
            }
        }
    }


    private void validateClearItem() {
        boolean isFormFilled = !first_name.getText().isEmpty() ||
                !last_name.getText().isEmpty() ||
                !department.getText().isEmpty() ||
                majorComboBox.getValue() != null ||
                !email.getText().isEmpty() ||
                !imageURL.getText().isEmpty();

        // Enable or disable clear item
        ClearItem.setDisable(!isFormFilled && tv.getSelectionModel().getSelectedItem() == null);
    }


    @FXML
    protected void addNewRecord() {
        try {
            // Insert user into the database
            cnUtil.insertUser(
                    0,  // Auto-generated ID
                    first_name.getText(),
                    last_name.getText(),
                    department.getText(),
                    majorComboBox.getValue().toString(),
                    email.getText(),
                    imageURL.getText()
            );

            // Refresh the TableView with the updated data
            refreshTableView();

            // Clear the form
            clearForm();

            // Show success message
            statusLabel.setText("Record added successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error adding record.");
        }
    }


    public int retrieveId(String email) {
        int id = -1;
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT id FROM users WHERE email = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                id = resultSet.getInt("id");
            }

            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }


    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        majorComboBox.setValue(null); // Clear the ComboBox selection
        //major.setText("");
        email.setText("");
        imageURL.setText("");
        addBtn.setDisable(true); // Disable add button after form clears

        //logic added to clear the form if u click clear from the menu item
        first_name.clear();
        last_name.clear();
        department.clear();
        majorComboBox.setValue(null);
        email.clear();
        imageURL.clear();
        tv.getSelectionModel().clearSelection();
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").getFile());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    protected void displayHelp() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/help.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void editRecord() {
        // Get the selected person from the TableView
        Person selectedPerson = tv.getSelectionModel().getSelectedItem();

        if (selectedPerson == null) {
            statusLabel.setText("No record selected to edit.");
            return;
        }

        try {
            // Validate inputs from the form
            String updatedFirstName = first_name.getText().trim();
            String updatedLastName = last_name.getText().trim();
            String updatedDepartment = department.getText().trim();
            String updatedMajor = majorComboBox.getValue() != null ? majorComboBox.getValue().toString() : null;
            String updatedEmail = email.getText().trim();
            String updatedImageURL = imageURL.getText().trim();

            // Ensure all required fields are filled
            if (updatedFirstName.isEmpty() || updatedLastName.isEmpty() || updatedDepartment.isEmpty()
                    || updatedMajor == null || updatedEmail.isEmpty()) {
                statusLabel.setText("All fields are required.");
                return;
            }

            // Fetch the account_id using the email
            int accountId = cnUtil.getAccountId(updatedEmail);

            if (accountId == -1) {
                statusLabel.setText("Account not found for the provided email.");
                return;
            }

            // Call the database method to update the user
            cnUtil.editUser(selectedPerson.getId(), accountId, updatedFirstName, updatedLastName,
                    updatedDepartment, updatedMajor, updatedEmail, updatedImageURL);

            // Update the ObservableList to reflect changes
            int selectedIndex = data.indexOf(selectedPerson);
            Person updatedPerson = new Person(
                    selectedPerson.getId(), // Keep the same ID
                    updatedFirstName,
                    updatedLastName,
                    updatedDepartment,
                    updatedMajor,
                    updatedEmail,
                    updatedImageURL
            );
            data.set(selectedIndex, updatedPerson);
            tv.getSelectionModel().select(updatedPerson);

            // Display success message
            statusLabel.setText("Record updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error updating record.");
        }
    }

    @FXML
    protected void deleteRecord() {
        Person person = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(person);
        cnUtil.deleteRecord(person.getId());
        refreshTableView();
        data.remove(index);
        tv.getSelectionModel().select(index);
    }

    private void copyRecord() {
        // Get the selected record from the TableView
        Person selectedPerson = tv.getSelectionModel().getSelectedItem();

        if (selectedPerson != null) {
            // Create a duplicate of the selected record
            Person copiedPerson = new Person(
                    data.size() + 1, // Assign a new ID
                    selectedPerson.getFirstName(),
                    selectedPerson.getLastName(),
                    selectedPerson.getDepartment(),
                    selectedPerson.getMajor(),
                    selectedPerson.getEmail(),
                    selectedPerson.getImageURL()
            );

            // Add duplicated record to TableView
            data.add(copiedPerson);

            // Insert into the database
            cnUtil.insertUser(
                    copiedPerson.getId(),
                    copiedPerson.getFirstName(),
                    copiedPerson.getLastName(),
                    copiedPerson.getDepartment(),
                    copiedPerson.getMajor(),
                    copiedPerson.getEmail(),
                    copiedPerson.getImageURL()
            );

            // Update status label to show a success message
            statusLabel.setText("Record copied successfully.");
        } else {
            // Show an error message if no record is selected
            statusLabel.setText("No record selected to copy.");
        }
    }


    //new code for progress bar, uploading profile pic
    @FXML
    protected void showImage() {
        // Open file chooser to select an image file
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            // Display the selected image in the ImageView
            img_view.setImage(new Image(file.toURI().toString()));

            // Create the upload task
            Task<Void> uploadTask = createUploadTask(file, progressBar);

            // Bind progress bar to the upload task's progress property
            progressBar.progressProperty().bind(uploadTask.progressProperty());

            // Start the upload task in a separate thread
            new Thread(uploadTask).start();
        }
    }

    //gets the url for the profile pic from the connection string and puts it into url text field
    private Task<Void> createUploadTask(File file, ProgressBar progressBar) {
        return new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    // Generate a unique name for the image in Azure Blob Storage
                    String blobName = file.getName();

                    // Create a file input stream for the file to be uploaded
                    FileInputStream fileInputStream = new FileInputStream(file);
                    long fileSize = file.length();

                    // Create a BlobClient to upload the file
                    BlobClient blobClient = store.getContainerClient().getBlobClient(blobName);

                    // Set the file upload with progress reporting
                    blobClient.upload(fileInputStream, fileSize, true);

                    // Report progress (100% when done)
                    updateProgress(1, 1); // Update progress to 100%

                    // Get the URL of the uploaded image from Azure Blob Storage
                    String uploadedImageUrl = store.getContainerClient().getBlobClient(blobName).getBlobUrl();

                    // Set the uploaded image URL to the text field
                    javafx.application.Platform.runLater(() -> {
                        imageURL.setText(uploadedImageUrl);
                        statusLabel.setText("Image uploaded successfully.");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        statusLabel.setText("Error uploading image.");
                    });
                }
                return null;
            }
        };
    }



    @FXML
    protected void addRecord() {
        showSomeone();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        first_name.setText(p.getFirstName());
        last_name.setText(p.getLastName());
        department.setText(p.getDepartment());
        //major.setText(p.getMajor());
        majorComboBox.setValue(Major.valueOf(p.getMajor())); // Set ComboBox value
        email.setText(p.getEmail());
        imageURL.setText(p.getImageURL());
    }

    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            System.out.println(menuBar.getScene());
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void darkTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            System.out.println(menuBar.getScene());
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Major> options =
                FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2,textField3, comboBox));
        Platform.runLater(textField1::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(),
                        textField2.getText(), comboBox.getValue());
            }
            return null;
        });
        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(
                    results.fname + " " + results.lname + " " + results.major);
        });
    }
    private static enum Major {CS, Medical, CPIS, IT, Cyber, Other}



    private static class Results {

        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }

}