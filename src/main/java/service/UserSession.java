package service;

import java.util.prefs.Preferences;

public class UserSession {

    private static UserSession instance;

    private int accountId;
    private String username;
    private String email;

    // Private constructor for session initialization
    private UserSession(int accountId, String username, String email) {
        this.accountId = accountId;
        this.username = username;
        this.email = email;

        // Store session data in Preferences (optional)
        Preferences userPreferences = Preferences.userRoot();
        userPreferences.putInt("ACCOUNT_ID", accountId);
        userPreferences.put("USERNAME", username);
        userPreferences.put("EMAIL", email);
    }

    // Thread-safe Singleton instance creation
    public static synchronized UserSession getInstance(int accountId, String username, String email) {
        if (instance == null) {
            instance = new UserSession(accountId, username, email);
            System.out.println("User session created for: " + username);
        }
        return instance;
    }

    // Synchronized Getter for accountId
    public synchronized int getAccountId() {
        return this.accountId;
    }

    // Synchronized Getter for username
    public synchronized String getUsername() {
        return this.username;
    }

    // Synchronized Getter for email
    public synchronized String getEmail() {
        return this.email;
    }

    // Clear session data (both in-memory and Preferences)
    public synchronized void clearSession() {
        // Clear in-memory data
        this.accountId = 0;
        this.username = null;
        this.email = null;

        // Clear Preferences (if used)
        Preferences userPreferences = Preferences.userRoot();
        userPreferences.remove("ACCOUNT_ID");
        userPreferences.remove("USERNAME");
        userPreferences.remove("EMAIL");

        // Reset the instance
        instance = null;
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "accountId=" + accountId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
