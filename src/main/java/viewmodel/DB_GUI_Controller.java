package viewmodel;

import dao.DbConnectivityClass;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Person;
import service.MyLogger;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;
import java.io.File;
import javafx.stage.FileChooser;

public class DB_GUI_Controller implements Initializable {
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_fn;
    @FXML
    private TableColumn<Person, String> tv_ln;
    @FXML
    private TableColumn<Person, String> tv_department;
    @FXML
    private TableColumn<Person, String> tv_major;
    @FXML
    private TableColumn<Person, String> tv_email;
    @FXML
    private Button deleteBtn;
    @FXML
    private Button editBtn;
    @FXML
    private Button  addBtn;
    @FXML
    private Button clearBtn;
    @FXML
    private TextField first_name, last_name, department, major, email, imageURL;
    @FXML
    ImageView img_view;
    @FXML
    MenuBar menuBar;
    @FXML
    private ComboBox<Major> majorDropdown;
    @FXML
    private MenuItem importMenuItem, exportMenuItem;
    private static final String firstNameReg = "(\\w){2,25}";
    private static final String lastNameReg = "(\\w){2,25}";
    private static final String departmentReg = "(\\w){2,25}";
    private static final String majorReg = "(\\w){2,25}";
    private static final String emailReg = "((\\w)(\\w+)(\\w))@(\\w+).(\\w+)";
    private static final String imageReg = "(?i)^.+\\.(jpg|jpeg|png|gif|bmp)$";
    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();

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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        deleteBtn.setDisable(true);
        editBtn.setDisable(true);

        tv.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean isSelected = newValue != null;
            editBtn.setDisable(!isSelected);
            deleteBtn.setDisable(!isSelected);
        });
        initializeFormValidation();
        majorDropdown.getItems().addAll(Major.values());
    }

    private void initializeFormValidation() {
        // These are listeners for the form field
        first_name.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        last_name.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        department.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        major.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        email.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        imageURL.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
        // Initial validation check method
        validateForm();
    }

    //Ensures the conditions of the RegEx pattern are met in form fields and if they are Add button is enabled. It is disabled otherwise.
    private void validateForm() {
        boolean isFormValid = firstNameValid(first_name.getText()) &&
                lastNameValid(last_name.getText()) &&
                departmentValid(department.getText()) &&
                majorValid(majorDropdown.getValue()) &&
                emailValid(email.getText()) &&
                imageValid(imageURL.getText());
        addBtn.setDisable(!isFormValid);
    }

    private boolean majorValid(Major major) {
        // Checks if major dropdown is not null and ensures one is selected
        return major != null;
    }

    @FXML
    protected void addNewRecord() {

            Person p = new Person(first_name.getText(), last_name.getText(), department.getText(),
                    major.getText(), email.getText(), imageURL.getText());
            cnUtil.insertUser(p);
            cnUtil.retrieveId(p);
            p.setId(cnUtil.retrieveId(p));
            data.add(p);
            clearForm();

    }

    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        major.setText("");
        email.setText("");
        imageURL.setText("");
        validateForm();
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
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        Person p2 = new Person(index + 1, first_name.getText(), last_name.getText(), department.getText(),
                major.getText(), email.getText(),  imageURL.getText());
        cnUtil.editUser(p.getId(), p2);
        data.remove(p);
        data.add(index, p2);
        tv.getSelectionModel().select(index);
    }

    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        cnUtil.deleteRecord(p);
        data.remove(index);
        tv.getSelectionModel().select(index);
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
        }
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
        major.setText(p.getMajor());
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
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
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

    private static enum Major {Business, CSC, CPIS}

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
    private boolean firstNameValid(String first_name) {
        return first_name.matches(firstNameReg);
    }
    private boolean lastNameValid(String last_name) {
        return last_name.matches(lastNameReg);
    }
    private boolean departmentValid(String department) {
        return department.matches(departmentReg);
    }
    private boolean majorValid(String major) {
        return major.matches(majorReg);
    }
    private boolean emailValid(String email) {
        return email.matches(emailReg);
    }
    private boolean imageValid(String imageURL) {
        return imageURL.matches(imageReg);
    }
    private void exportDataToCSV(File file) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            // Iterate through the data in your TableView
            for (Person person : data) { // Replace 'Person' with your model class
                String line = String.join(",",
                        person.getFirstName(), // Replace with actual getters
                        person.getLastName(),
                        person.getDepartment(),
                        person.getMajor(),
                        person.getEmail(),
                        person.getImageURL()
                );
                bw.write(line); // Write the CSV line
                bw.newLine();   // Move to the next line
            }
            System.out.println("CSV file exported successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(null); // Show the save dialog
        if (file != null) {
            exportDataToCSV(file);
        }
    }


    private void importDataFromCSV(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); // Assuming values are comma-separated
                if (values.length >= 6) { // Ensure there are enough values (adjust based on your model)
                    // Create a new instance of your model class (e.g., Person)
                    Person person = new Person(
                            values[0], // First Name
                            values[1], // Last Name
                            values[2], // Department
                            values[3], // Major
                            values[4], // Email
                            values[5]  // Image URL
                    );
                    // Add the object to your ObservableList
                    data.add(person);
                }
            }
            // Update the TableView with the new data
            tv.setItems(data);
            System.out.println("CSV data imported successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleImportCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showOpenDialog(null); // Show the file chooser
        if (file != null) {
            importDataFromCSV(file);
        }
    }
}