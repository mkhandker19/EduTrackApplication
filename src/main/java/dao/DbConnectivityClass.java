package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Person;
import service.MyLogger;

import java.sql.*;

public class DbConnectivityClass {
    final static String DB_NAME = "csc311_bd_temp";
    MyLogger lg = new MyLogger();
    final static String SQL_SERVER_URL = "jdbc:mysql://csc311khandkerserver.mysql.database.azure.com";
    final static String DB_URL = "jdbc:mysql://csc311khandkerserver.mysql.database.azure.com/" + DB_NAME;
    final static String USERNAME = "csc311admin";
    final static String PASSWORD = "farmingdale24@";

    private final ObservableList<Person> data = FXCollections.observableArrayList();

    /**
     * Retrieves data from the `users` table and populates the ObservableList.
     */
    public ObservableList<Person> getData() {
        data.clear(); // Clear any existing data
        connectToDatabase();
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM users";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                data.add(new Person(
                        resultSet.getInt("my_row_id"), // Auto-generated ID
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("department"),
                        resultSet.getString("major"),
                        resultSet.getString("email"),
                        resultSet.getString("imageURL")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }


    /**
     * Ensures the database and required tables exist.
     */
    public boolean connectToDatabase() {
        try (Connection conn = DriverManager.getConnection(SQL_SERVER_URL, USERNAME, PASSWORD)) {
            Statement statement = conn.createStatement();
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);

            // Create the `users` table if it doesn't exist
            String createUsersTableSQL = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    account_id INT NOT NULL,
                    first_name VARCHAR(200) NOT NULL,
                    last_name VARCHAR(200) NOT NULL,
                    department VARCHAR(200),
                    major VARCHAR(200),
                    email VARCHAR(200) NOT NULL UNIQUE,
                    imageURL VARCHAR(200),
                    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
                )
                """;
            statement.executeUpdate(createUsersTableSQL);

            // Create the `accounts` table if it doesn't exist
            String createAccountsTableSQL = """
                CREATE TABLE IF NOT EXISTS accounts (
                    account_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(200) NOT NULL UNIQUE,
                    password VARCHAR(200) NOT NULL,
                    email VARCHAR(200) NOT NULL UNIQUE
                )
                """;
            statement.executeUpdate(createAccountsTableSQL);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Inserts a new user into the `users` table.
     */
    public int insertUser(String firstName, String lastName, String department, String major, String email, String imageURL) {
        connectToDatabase();
        int generatedId = -1;
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO users (first_name, last_name, department, major, email, imageURL) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, department);
            preparedStatement.setString(4, major);
            preparedStatement.setString(5, email);
            preparedStatement.setString(6, imageURL);
            preparedStatement.executeUpdate();

            // Retrieve the auto-generated ID
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs.next()) {
                generatedId = rs.getInt(1);
            }

            lg.makeLog("User information inserted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return generatedId;
    }



    /**
     * Validates login credentials by checking the `accounts` table.
     */
    public boolean validateLogin(String username, String password) {
        connectToDatabase();
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM accounts WHERE username = ? AND password = ?";
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

    /**
     * Checks if a username already exists in the `accounts` table.
     */
    public boolean isUsernameExists(String username) {
        connectToDatabase();
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT COUNT(*) FROM accounts WHERE username = ?";
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

    /**
     * Deletes a user from the `users` table.
     */
    public void deleteRecord(int id) {
        connectToDatabase();
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "DELETE FROM users WHERE id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts a new account into the `accounts` table.
     */
    public int insertAccount(String username, String password, String email) {
        connectToDatabase();
        int accountId = -1;
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO accounts (username, password, email) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, email);
            preparedStatement.executeUpdate();

            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                accountId = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accountId;
    }

    /**
     * Retrieves the account ID for a given username.
     */
    public int getAccountId(String username) {
        connectToDatabase();
        int accountId = -1;
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT account_id FROM accounts WHERE username = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                accountId = resultSet.getInt("account_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accountId;
    }

    /**
     * Updates a user's information in the `users` table.
     */
    public void editUser(int userId, int accountId, String firstName, String lastName,
                         String department, String major, String email, String imageURL) {
        connectToDatabase();
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = """
                UPDATE users
                SET account_id = ?, first_name = ?, last_name = ?, department = ?, 
                    major = ?, email = ?, imageURL = ?
                WHERE id = ?
                """;

            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, accountId);
            preparedStatement.setString(2, firstName);
            preparedStatement.setString(3, lastName);
            preparedStatement.setString(4, department);
            preparedStatement.setString(5, major);
            preparedStatement.setString(6, email);
            preparedStatement.setString(7, imageURL);
            preparedStatement.setInt(8, userId);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updateUser(String firstName, String lastName, String department, String major, String email, String imageURL) {
        connectToDatabase();
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = """
            UPDATE users
            SET first_name = ?, last_name = ?, department = ?, major = ?, imageURL = ?
            WHERE email = ?
        """;
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, department);
            preparedStatement.setString(4, major);
            preparedStatement.setString(5, imageURL);
            preparedStatement.setString(6, email);

            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                lg.makeLog("User updated successfully: " + email);
            } else {
                lg.makeLog("No user found with email: " + email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
