package com.hdfclife.core.service;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component(service = DatabaseConnectionService.class, immediate = true)
@Designate(ocd = DatabaseConnectionService.Config.class)
public class DatabaseConnectionService {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionService.class);
    
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private Connection connection;

    @ObjectClassDefinition(name = "HDFC Life - Database Connection Configuration")
    public @interface Config {
        
        @AttributeDefinition(name = "Database URL", description = "JDBC URL for MySQL database")
        String db_url() default "jdbc:mysql://localhost:3306/hdfclife";
        
        @AttributeDefinition(name = "Database Username", description = "MySQL database username")
        String db_username() default "root";
        
        @AttributeDefinition(name = "Database Password", description = "MySQL database password")
        String db_password() default "";
    }

    @Activate
    protected void activate(Config config) {
        this.dbUrl = config.db_url();
        this.dbUsername = config.db_username();
        this.dbPassword = config.db_password();
        initializeConnection();
    }

    @Deactivate
    protected void deactivate() {
        closeConnection();
    }

    private void initializeConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                log.info("Database connection established successfully");
            }
        } catch (SQLException | ClassNotFoundException e) {
            log.error("Failed to initialize database connection", e);
        }
    }

    private void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                log.info("Database connection closed successfully");
            }
        } catch (SQLException e) {
            log.error("Error closing database connection", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            initializeConnection();
        }
        return connection;
    }
} 