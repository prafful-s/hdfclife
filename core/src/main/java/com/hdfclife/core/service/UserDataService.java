package com.hdfclife.core.service;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component(service = UserDataService.class)
public class UserDataService {
    
    private static final Logger log = LoggerFactory.getLogger(UserDataService.class);

    @Reference
    private DatabaseConnectionService dbService;

    public Map<String, String> getUserData(String userId) {
        Map<String, String> userData = new HashMap<>();
        String query = "SELECT given_name, email, company, city, mobile, iban, swift_code " +
                      "FROM user_profile WHERE user_id = ?";

        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userData.put("givenName", rs.getString("given_name"));
                userData.put("email", rs.getString("email"));
                userData.put("company", rs.getString("company"));
                userData.put("city", rs.getString("city"));
                userData.put("mobile", rs.getString("mobile"));
                userData.put("iban", rs.getString("iban"));
                userData.put("swiftCode", rs.getString("swift_code"));
            }

            rs.close();
        } catch (SQLException e) {
            log.error("Error fetching user data from database", e);
        }

        return userData;
    }
} 