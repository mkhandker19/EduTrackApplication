package service;

import java.util.concurrent.locks.ReentrantLock;

public class UserSession {

    private static volatile UserSession instance; // Volatile ensures visibility across threads
    private static final ReentrantLock lock = new ReentrantLock(); // Lock for finer control

    private String userName;
    private String password;
    private String privileges;

    private UserSession(String userName, String password, String privileges) {
        this.userName = userName;
        this.password = password; // Avoid storing plaintext passwords if possible
        this.privileges = privileges;
    }

    // Thread-safe Singleton Instance Method
    public static UserSession getInstance(String userName, String password, String privileges) {
        if (instance == null) {
            synchronized (UserSession.class) { // Class-level lock
                if (instance == null) { // Double-checked locking
                    instance = new UserSession(userName, password, privileges);
                }
            }
        }
        return instance;
    }

    public static UserSession getInstance(String userName, String password) {
        return getInstance(userName, password, "NONE");
    }

    // Synchronized Getter for UserName
    public synchronized String getUserName() {
        return this.userName;
    }

    // Synchronized Getter for Password (Consider Avoiding Plaintext Retrieval)
    public synchronized String getPassword() {
        return this.password;
    }

    // Synchronized Getter for Privileges
    public synchronized String getPrivileges() {
        return this.privileges;
    }

    // Clean Session Safely
    public void cleanUserSession() {
        lock.lock(); // Explicit lock for safety
        try {
            this.userName = null;
            this.password = null;
            this.privileges = null;
            instance = null; // Reset instance
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "userName='" + this.userName + '\'' +
                ", privileges=" + this.privileges +
                '}';
    }
}
