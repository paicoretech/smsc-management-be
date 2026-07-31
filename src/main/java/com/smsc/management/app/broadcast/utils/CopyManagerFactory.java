package com.smsc.management.app.broadcast.utils;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class CopyManagerFactory {
    public CopyManager create(Connection connection) throws SQLException {
        return new CopyManager(connection.unwrap(BaseConnection.class));
    }
}