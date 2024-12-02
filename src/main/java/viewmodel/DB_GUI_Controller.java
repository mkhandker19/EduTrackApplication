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
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);



            Platform.runLater(() -> {
                if (menuBar.getScene() != null) {
                    menuBar.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                        // Checks if Ctrl + E is pressed
                        if (event.isControlDown() && event.getCode() == KeyCode.E) {
                            // Trigger the editRecord method when Ctrl + E is pressed
                            editRecord();
                        }
                    });
                }
            });


            Platform.runLater(() -> {
                if (menuBar.getScene() != null) {
                    menuBar.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                        // Checks if Ctrl + D is pressed
                        if (event.isControlDown() && event.getCode() == KeyCode.D) {
                            // Trigger the deleteRecord method when Ctrl + D is pressed
                            deleteRecord();
                        }
                    });
                }
            });


            Platform.runLater(() -> {
                if (menuBar.getScene() != null) {
                    menuBar.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                        // Checks if Ctrl + R is pressed
                        if (event.isControlDown() && event.getCode() == KeyCode.R) {
                            // Trigger the clearForm method when Ctrl + R is pressed
                            clearForm();
                        }
                    });
                }
            });


            Platform.runLater(() -> {
                if (menuBar.getScene() != null) {
                    menuBar.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                        // Checks if Ctrl + C is pressed
                        if (event.isControlDown() && event.getCode() == KeyCode.C) {
                            // Trigger the copyRecord method when Ctrl + C is pressed
                            copyRecord();
                        }
                    });
                }
            });


            editItem.setOnAction(event -> editRecord());
            deleteItem.setOnAction(event -> deleteRecord());
            CopyItem.setOnAction(event -> copyRecord());
            editBtn.setDisable(true);
            deleteBtn.setDisable(true);
            editItem.setDisable(true);
            deleteItem.setDisable(true);
            ClearItem.setDisable(true);
            CopyItem.setDisable(true);
            // Listener added
            tv.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Person>() {
                @Override
                public void changed(ObservableValue<? extends Person> observable, Person oldValue, Person newValue) {
                   // Edit button enabled if selected other stays disabled
                    editBtn.setDisable(newValue == null);
                    // delete button enabled if selected other stays disabled
                    deleteBtn.setDisable(newValue == null);
                    // Clearitem button enabled if selected other stays disabled
                    ClearItem.setDisable(newValue == null);
                    boolean isSelected = newValue != null;
                    editItem.setDisable(!isSelected);
                    deleteItem.setDisable(!isSelected);
                    CopyItem.setDisable(!isSelected);

                }
            });

            // Form must be filled
            first_name.textProperty().addListener((observable, oldValue, newValue) -> validateClearItem());
            last_name.textProperty().addListener((observable, oldValue, newValue) -> validateClearItem());
            department.textProperty().addListener((observable, oldValue, newValue) -> validateClearItem());
            majorComboBox.valueProperty().addListener((observable, oldValue, newValue) -> validateClearItem());
            email.textProperty().addListener((observable, oldValue, newValue) -> validateClearItem());
            imageURL.textProperty().addListener((observable, oldValue, newValue) -> validateClearItem());

            // Addbtn is disabled
            addBtn.setDisable(true);

            // Major enum values
            majorComboBox.setItems(FXCollections.observableArrayList(Major.values()));

            // ComboBox default selection
            majorComboBox.getSelectionModel().selectFirst();

            // Text fields listners
            first_name.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
            last_name.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
            department.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
            majorComboBox.valueProperty().addListener((observable, oldValue, newValue) -> validateForm());
            email.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
            imageURL.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void importCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());

        if (file != null) {
            try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
                String line;
                int lineNumber = 0;

                reader.readLine();

                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length == 7) {

                        Person person = new Person(Integer.parseInt(data[0]), data[1], data[2], data[3], data[4], data[5], data[6]);
                        cnUtil.insertUser(person);
                        this.data.add(person);
                    } else {
                        statusLabel.setText("Invalid CSV format.");
                        break;
                    }
                    lineNumber++;
                }
                statusLabel.setText("Data imported successfully.");
            } catch (Exception e) {
                statusLabel.setText("Error importing data.");
                e.printStackTrace();
            }
        }
    }
    public void insertUser(Person person) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "INSERT INTO users (first_name, last_name, department, major, email, imageURL, password) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, person.getFirstName());
            preparedStatement.setString(2, person.getLastName());
            preparedStatement.setString(3, person.getDepartment());
            preparedStatement.setString(4, person.getMajor());
            preparedStatement.setString(5, person.getEmail());
            preparedStatement.setString(6, person.getImageURL());
            preparedStatement.setString(7, "default_password");
            preparedStatement.executeUpdate();
            preparedStatement.close();
            conn.close();
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


    private void validateForm() {
        //First Name
        boolean isFirstNameValid = isValidName(first_name.getText());

        //Last Name
        boolean isLastNameValid = isValidName(last_name.getText());

        //Department
        boolean isDepartmentValid = isValidDepartment(department.getText());

        //Major
        boolean isMajorValid = majorComboBox.getValue() != null; // Ensure a major is selected

        //Email
        boolean isEmailValid = isValidEmail(email.getText());

        //Image URL
        boolean isImageURLValid = isValidImageURL(imageURL.getText());
        addBtn.setDisable(!(isFirstNameValid && isLastNameValid && isDepartmentValid &&
                isMajorValid && isEmailValid && isImageURLValid));
    }
    // Regex pattern incorporated
    private boolean isValidName(String name) {
        String nameRegex = "^[A-Za-z\\s]+$"; // letters & spaces
        Pattern pattern = Pattern.compile(nameRegex);
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    //department
    private boolean isValidDepartment(String field) {
        String departmentRegex = "^[A-Za-z\\s-]+$"; // Only letters, spaces, and hyphens
        Pattern pattern = Pattern.compile(departmentRegex);
        Matcher matcher = pattern.matcher(field);
        return matcher.matches();
    }

    //email
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$"; // email validation
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    //image URL
    private boolean isValidImageURL(String url) {
        String imageURLRegex = "^(http|https)://.*\\.(jpg|jpeg|png|gif|bmp|webp)$"; // URL with image extension
        Pattern pattern = Pattern.compile(imageURLRegex);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }



    @FXML
    protected void addNewRecord() {
        // Validate that all required fields are filled
        if (first_name.getText().isEmpty() || last_name.getText().isEmpty() || department.getText().isEmpty() ||
                email.getText().isEmpty() || majorComboBox.getValue() == null) {
            statusLabel.setText("Please fill out all required fields.");
            return;
        }

        // Create a new Person object with form inputs
        Person p = new Person(
                first_name.getText(),
                last_name.getText(),
                department.getText(),
                majorComboBox.getValue().toString(),
                email.getText(),
                imageURL.getText()
        );

        // Insert the user into the database
        cnUtil.insertUser(p); // Use cnUtil.insertUser(p) or insertUser(p), but not both
        data.add(p);          // Add to observable list for TableView
        clearForm();          // Clear the form after successful addition
        statusLabel.setText("Record added successfully.");
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
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        Person p2 = new Person(index + 1, first_name.getText(), last_name.getText(), department.getText(),
                majorComboBox.getValue().toString(), email.getText(), imageURL.getText());
        cnUtil.editUser(p.getId(), p2);
        refreshTableView();
        data.remove(p);
        data.add(index, p2);
        tv.getSelectionModel().select(index);

        // Update status label with success or failure message
        try {
            statusLabel.setText("Record updated successfully.");
        } catch (Exception e) {
            statusLabel.setText("Record was not updated successfully.");
        }
        if (statusLabel != null) {
            statusLabel.setText("Record added successfully.");
        } else {
            System.out.println("statusLabel is null. Check FXML linkage.");
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

    @FXML
    private void copyRecord() {
        // Gets selected record from the table
        Person selectedPerson = tv.getSelectionModel().getSelectedItem();
        if (selectedPerson != null) {
            // Creates a duplicate of the selected record
            Person copiedPerson = new Person(
                    data.size() + 1, // Assigns a new ID
                    selectedPerson.getFirstName(),
                    selectedPerson.getLastName(),
                    selectedPerson.getDepartment(),
                    selectedPerson.getMajor(),
                    selectedPerson.getEmail(),
                    selectedPerson.getImageURL()
            );

            // Adds duplicated record to TableView and database
            data.add(copiedPerson);
            cnUtil.insertUser(copiedPerson); // inserts into the database

            // Updates status label to show a success message
            statusLabel.setText("Record copied successfully.");
        } else {
            // Shows an error message if no record is selected
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
    private void refreshTableView() {
        data.clear(); // Clear current data
        data.addAll(cnUtil.getData()); // Add fresh data from the database
    }


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