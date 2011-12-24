/*
 * Created on Dec 24, 2011
 * Last updated on Dec 24, 2011
 * 
 */
package org.DataDryad.LinkoutTool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DryadDBConnection {

    
    private Connection connection;
    private static final String CONNECTION_PROPERTIES_FILENAME = "Connection.properties"; 

    public String connect() {
        return openDBFromPropertiesFile(CONNECTION_PROPERTIES_FILENAME);
    }
    
    public String openDBFromPropertiesFile(String propertyFileSpec){
        final Properties properties = new Properties();
        try {
            properties.load(this.getClass().getResourceAsStream(propertyFileSpec));
        } catch (Exception e1) {
            throw new RuntimeException("Failed to open connection properties file; path = " + CONNECTION_PROPERTIES_FILENAME);
        } 
        try {
            Class.forName("org.postgresql.Driver");
        } catch(ClassNotFoundException e){
            System.err.println("Couldn't load PSQL Driver");
            e.printStackTrace();
        }
        final String host = properties.getProperty("host");
        final String db = properties.getProperty("db");
        final String user = properties.getProperty("user");
        final String password = properties.getProperty("pw");
        try {
            connection = DriverManager.getConnection(String.format("jdbc:postgresql://%s/%s",host,db),user,password);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return "Host: " + host + " db: " + db;

    }

    public String reconnect() {
        return null;
        // TODO Auto-generated method stub
        
    }

    public void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        // TODO Auto-generated method stub
        return false;
    }
    
    public Connection getConnection(){
        return connection;
    }


}
