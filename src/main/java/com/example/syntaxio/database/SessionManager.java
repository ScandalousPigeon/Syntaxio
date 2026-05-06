package com.example.syntaxio.database;

import com.example.syntaxio.model.User;
import com.example.syntaxio.ui.util.PasswordHasher;

import java.util.Optional;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private SqliteUserDAO userDAO;
    
    private SessionManager() {
        userDAO = new SqliteUserDAO();
    }
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public boolean login(String username, String password) {
        if (userDAO.verifyLogin(username, password)) {
            Optional<User> userOpt = userDAO.findUserByUsername(username);
            if (userOpt.isPresent()) {
                currentUser = userOpt.get();
                return true;
            }
        }
        return false;
    }
    
    public boolean signup(String username, String password) {
        if (userDAO.userExists(username)) {
            return false;
        }
        
        String hashedPassword = PasswordHasher.hashPassword(password);
        User newUser = new User(username, hashedPassword);
        return userDAO.addUser(newUser);
    }
    
    public void logout() {
        currentUser = null;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public void refreshCurrentUser() {
        if (currentUser != null) {
            Optional<User> refreshed = userDAO.findUserById(currentUser.getId());
            refreshed.ifPresent(user -> currentUser = user);
        }
    }
    
    public SqliteUserDAO getUserDAO() {
        return userDAO;
    }    
}
