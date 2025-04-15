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
import java.util.HashMap;
import java.util.Map;

@Component(service = UserDataService.class)
public class UserDataService {
    
    private static final Logger log = LoggerFactory.getLogger(UserDataService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

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
                // if (jsonStr != null && !jsonStr.isEmpty()) {
                //     // Convert JSON string to Map
                //     ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(jsonStr);
                //     jsonNode.fields().forEachRemaining(entry -> 
                //         userData.put(entry.getKey(), entry.getValue().asText(""))
                //     );
                //     log.debug("Found user data for mobile number: {}", mobileNumber);
                // }
            } else {
                log.warn("No user found for mobile number: {}", mobileNumber);
            }

            rs.close();
        } catch (Exception e) {
            log.error("Error fetching user data from database for mobile number: {}", mobileNumber, e);
        }

        return userData;
    }

    public boolean saveUserData(Map<String, String> formData) {
        String query = "INSERT INTO user_profile (mobileNumber, userProfileJson) VALUES (?, ?) " +
                      "ON DUPLICATE KEY UPDATE userProfileJson = VALUES(userProfileJson)";

        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            String mobileNumber = formData.get("mobileNumber");
            
            // Convert Map to JSON string
            ObjectNode jsonNode = objectMapper.createObjectNode();
            formData.forEach((key, value) -> {
                if (value != null) {
                    // Handle different data types
                    try {
                        switch (key) {
                            case "nriCheck":
                            case "tobaccoCheck":
                            case "countryCode":
                            case "medicalHistoryOthers":
                            case "medicalHistoryHeart":
                                jsonNode.put(key, Integer.parseInt(value));
                                break;
                            case "mobileNumber":
                                jsonNode.put(key, Long.parseLong(value));
                                break;
                            case "annualIncome":
                            case "existingLifeCover":
                            case "height":
                            case "weight":
                                jsonNode.put(key, Double.parseDouble(value));
                                break;
                            default:
                                jsonNode.put(key, value);
                        }
                    } catch (NumberFormatException e) {
                        // If parsing fails, store as string
                        jsonNode.put(key, value);
                    }
                }
            });

            // Handle nested objects
            ObjectNode termsAndConditions = objectMapper.createObjectNode();
            termsAndConditions.put("approvalcheckbox", formData.getOrDefault("approvalcheckbox", ""));
            jsonNode.set("termsandconditions1744275661352", termsAndConditions);

            // Handle empty arrays
            jsonNode.putArray("panelcontainer1744093626683").addObject();
            jsonNode.putArray("panelAdditionalFeaturesRepeatable").addObject();

            String jsonString = objectMapper.writeValueAsString(jsonNode);
            
            stmt.setString(1, mobileNumber);
            stmt.setString(2, jsonString);

            int rowsAffected = stmt.executeUpdate();
            log.info("Saved/Updated user data for mobile number: {}. Rows affected: {}", 
                    mobileNumber, rowsAffected);
            
            return rowsAffected > 0;
        } catch (Exception e) {
            log.error("Error saving user data for mobile number: {}", formData.get("mobileNumber"), e);
            return false;
        }
    }
} 