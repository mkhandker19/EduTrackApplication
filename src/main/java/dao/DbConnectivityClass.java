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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public void insertUser(int accountId, String firstName, String lastName, String department, String major, String email, String imageURL) {
        connectToDatabase();
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO users (account_id, first_name, last_name, department, major, email, imageURL) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, accountId); // Link to the account in the accounts table
            preparedStatement.setString(2, firstName);
            preparedStatement.setString(3, lastName);
            preparedStatement.setString(4, department);
            preparedStatement.setString(5, major);
            preparedStatement.setString(6, email);
            preparedStatement.setString(7, imageURL);
            preparedStatement.executeUpdate();

            lg.makeLog("User information inserted successfully for account ID: " + accountId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




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




}
