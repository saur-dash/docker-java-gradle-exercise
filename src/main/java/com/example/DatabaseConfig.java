package com.example;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class DatabaseConfig {

    private final String user = getRequiredSetting("DB_USER");
    private final String password = getRequiredSetting("DB_PWD");
    private final String serverName = getRequiredSetting("DB_SERVER"); // db host name, like localhost without the port
    private final String dbName = getRequiredSetting("DB_NAME");
    private final MysqlDataSource datasource = new MysqlDataSource();

    public DatabaseConfig() {
        datasource.setPassword(password);
        datasource.setUser(user);
        datasource.setServerName(serverName);
        datasource.setDatabaseName(dbName);
        datasource.setPort(3306); // default config
        datasource.setURL("jdbc:mysql://" + serverName + ":3306/" + dbName);

    }

    @Bean
    public Connection getConnection() throws SQLException {
        return datasource.getConnection();
    }

    private static String getRequiredSetting(String settingName) {
        String value = getSetting(settingName);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required setting: " + settingName + " or " + settingName + "_FILE");
        }
        return value;
    }

    private static String getSetting(String settingName) {
        String filePath = System.getenv(settingName + "_FILE");
        if (filePath != null && !filePath.isBlank()) {
            return readSettingFromFile(settingName, filePath);
        }

        String value = System.getenv(settingName);
        return value == null ? null : value.trim();
    }

    private static String readSettingFromFile(String settingName, String filePath) {
        try {
            return Files.readString(Path.of(filePath)).trim();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read setting from " + settingName + "_FILE: " + filePath, ex);
        }
    }
}
