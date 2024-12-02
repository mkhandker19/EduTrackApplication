package dao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Person;
import service.MyLogger;
import java.sql.*;
public class DbConnectivityClass {
    final static String DB_NAME = "CSC311_BD_TEMP";
    MyLogger lg = new MyLogger();
    final static String SQL_SERVER_URL = "jdbc:mysql://csc311khandkerserver.mysql.database.azure.com";
    final static String DB_URL = "jdbc:mysql://csc311khandkerserver.mysql.database.azure.com/" + DB_NAME;
    final static String USERNAME = "csc311admin";
    final static String PASSWORD = "farmingdale24@";

    private final ObservableList<Person> data = FXCollections.observableArrayList();

    public ObservableList<Person> getData() {
        connectToDatabase();
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM users";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String department = resultSet.getString("department");
                String major = resultSet.getString("major");
                String email = resultSet.getString("email");
                String imageURL = resultSet.getString("imageURL");
                data.add(new Person(id, first_name, last_name, department, major, email, imageURL));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public boolean connectToDatabase() {
        try (Connection conn = DriverManager.getConnection(SQL_SERVER_URL, USERNAME, PASSWORD)) {
            Statement statement = conn.createStatement();
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);

            try (Connection dbConn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
                String sql = """
                        CREATE TABLE IF NOT EXISTS users (
                            id INT(10) NOT NULL PRIMARY KEY AUTO_INCREMENT,
                            first_name VARCHAR(200) NOT NULL,
                            last_name VARCHAR(200),
                            department VARCHAR(200),
                            major VARCHAR(200),
                            email VARCHAR(200) NOT NULL UNIQUE,
                            imageURL VARCHAR(200),
                            password VARCHAR(200) NOT NULL
                        )
                        """;
                dbConn.createStatement().executeUpdate(sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void insertUser(Person person) {
        connectToDatabase();
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO users (first_name, last_name, password) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, person.getFirstName());
            preparedStatement.setString(2, person.getLastName());
            preparedStatement.executeUpdate();
            lg.makeLog("A new user was inserted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean validateLogin(String username, String password) {
        connectToDatabase();
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM users WHERE first_name = ? AND password = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isUsernameExists(String username) {
        connectToDatabase();
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT COUNT(*) FROM users WHERE first_name = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public int retrieveId(Person person) {
        connectToDatabase();
        int id = -1;
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT id FROM users WHERE email=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, person.getEmail());

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt("id");
            }

            resultSet.close();
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public void editUser(int id, Person person) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "UPDATE users SET first_name=?, last_name=?, department=?, major=?, email=?, imageURL=? WHERE id=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, person.getFirstName());
            preparedStatement.setString(2, person.getLastName());
            preparedStatement.setString(3, person.getDepartment());
            preparedStatement.setString(4, person.getMajor());
            preparedStatement.setString(5, person.getEmail());
            preparedStatement.setString(6, person.getImageURL());
            preparedStatement.setInt(7, id);

            preparedStatement.executeUpdate();
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteRecord(int id) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "DELETE FROM users WHERE id=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, id);

            preparedStatement.executeUpdate();
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
