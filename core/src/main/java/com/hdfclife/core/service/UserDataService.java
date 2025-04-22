package com.hdfclife.core.service;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Component(service = UserDataService.class)
public class UserDataService {
    
    private static final Logger log = LoggerFactory.getLogger(UserDataService.class);

    @Reference
    private DatabaseConnectionService dbService;

    public String getUserData(String mobileNumber) {
        String userData = "";
        String query = "SELECT userProfileJson FROM user_profile WHERE mobileNumber = ?";

        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, mobileNumber);
            log.info("Executing query for mobile number: {}", mobileNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userData = rs.getString("userProfileJson");
            } else {
                log.warn("No user found for mobile number: {}", mobileNumber);
            }
            rs.close();
        } catch (Exception e) {
            log.error("Error fetching user data from database for mobile number: {}", mobileNumber, e);
        }
        return userData;
    }

    public void saveUserData(String userProfileJson) {
        String upsertQuery = "INSERT INTO user_profile (mobileNumber, userProfileJson) " +
                           "VALUES (?, ?) " +
                           "ON DUPLICATE KEY UPDATE userProfileJson = ?";
        log.info("Upsert query: {}", upsertQuery);

        try {
            // Parse JSON to extract mobile number
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonNode = (ObjectNode) mapper.readTree(userProfileJson);
            String mobileNumber = jsonNode.get("mobileNumber").asText();
            log.info("Mobile number from the user profile json: {}", mobileNumber);
            try (Connection conn = dbService.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(upsertQuery)) {
                log.info("DB connection successful");
                
                stmt.setString(1, mobileNumber);
                stmt.setString(2, userProfileJson);
                stmt.setString(3, userProfileJson);
                
                int rowsAffected = stmt.executeUpdate();
                log.info("Successfully saved/updated user data for mobile number: {}. Rows affected: {}", 
                        mobileNumber, rowsAffected);
            }
        } catch (Exception e) {
            log.error("Error saving user data to database", e);
        }
    }
}