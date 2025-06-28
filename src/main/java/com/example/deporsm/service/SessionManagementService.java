package com.example.deporsm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing user sessions, including terminating sessions for deactivated users
 */
@Service
public class SessionManagementService {

    @Autowired
    private SessionRegistry sessionRegistry;

    /**
     * Terminates all active sessions for a specific user by their email
     * @param userEmail The email of the user whose sessions should be terminated
     */
    public void terminateUserSessions(String userEmail) {
        System.out.println("🔒 Attempting to terminate sessions for user: " + userEmail);
        
        // Get all principals (logged in users)
        List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
        
        for (Object principal : allPrincipals) {
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                
                // Check if this is the user we want to terminate
                if (userDetails.getUsername().equals(userEmail)) {
                    // Get all sessions for this user
                    List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                    
                    for (SessionInformation session : sessions) {
                        if (!session.isExpired()) {
                            System.out.println("🔒 Expiring session: " + session.getSessionId() + " for user: " + userEmail);
                            session.expireNow();
                        }
                    }
                    
                    System.out.println("✅ Terminated " + sessions.size() + " session(s) for user: " + userEmail);
                    break;
                }
            }
        }
    }

    /**
     * Terminates all active sessions for a specific user by their user ID
     * @param userId The ID of the user whose sessions should be terminated
     */
    public void terminateUserSessionsById(Integer userId) {
        System.out.println("🔒 Attempting to terminate sessions for user ID: " + userId);
        
        // Get all principals (logged in users)
        List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
        
        for (Object principal : allPrincipals) {
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                
                // For our custom UserDetails implementation, we need to check if it contains the user ID
                // This assumes the UserDetails implementation stores the user ID somehow
                // We'll use the username (email) to find and terminate sessions
                List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                
                for (SessionInformation session : sessions) {
                    if (!session.isExpired()) {
                        System.out.println("🔒 Expiring session: " + session.getSessionId() + " for user ID: " + userId);
                        session.expireNow();
                    }
                }
            }
        }
    }

    /**
     * Gets the count of active sessions for a specific user
     * @param userEmail The email of the user
     * @return The number of active sessions
     */
    public int getActiveSessionCount(String userEmail) {
        List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
        
        for (Object principal : allPrincipals) {
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                
                if (userDetails.getUsername().equals(userEmail)) {
                    List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                    return (int) sessions.stream().filter(session -> !session.isExpired()).count();
                }
            }
        }
        
        return 0;
    }

    /**
     * Gets information about all active sessions
     * @return List of session information
     */
    public List<Object> getAllActivePrincipals() {
        return sessionRegistry.getAllPrincipals();
    }
}
