package service;

import java.util.prefs.Preferences;

public class UserSession {

    private static UserSession instance;

    private String userName;
    private String password;
    private String privileges;

    // Private constructor initializes the session and stores it in Preferences
    private UserSession(String userName, String password, String privileges) {
        this.userName = userName;
        this.password = password;
        this.privileges = privileges;

        // Store session data in Preferences
        Preferences userPreferences = Preferences.userRoot();
        userPreferences.put("USERNAME", userName);
        userPreferences.put("PASSWORD", password);
        userPreferences.put("PRIVILEGES", privileges);
    }

    // Thread-safe Singleton instance creation
    public static synchronized UserSession getInstance(String userName, String password, String privileges) {
        if (instance == null) {
            instance = new UserSession(userName, password, privileges);
        }
        return instance;
    }

    // Overloaded method with default privileges ("NONE")
    public static synchronized UserSession getInstance(String userName, String password) {
        return getInstance(userName, password, "NONE");
    }

    // Synchronized Getter for UserName
    public synchronized String getUserName() {
        return this.userName;
    }

    // Synchronized Getter for Password
    public synchronized String getPassword() {
        return this.password;
    }

    // Synchronized Getter for Privileges
    public synchronized String getPrivileges() {
        return this.privileges;
    }

    // Thread-safe clean user session
    public synchronized void cleanUserSession() {
        // Clear in-memory data
        this.userName = "";
        this.password = "";
        this.privileges = "";

        // Clear data in Preferences
        Preferences userPreferences = Preferences.userRoot();
        userPreferences.remove("USERNAME");
        userPreferences.remove("PASSWORD");
        userPreferences.remove("PRIVILEGES");

        // Reset the instance
        instance = null;
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "userName='" + this.userName + '\'' +
                ", privileges='" + this.privileges + '\'' +
                '}';
    }
}
