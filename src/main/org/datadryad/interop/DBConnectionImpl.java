/*
 * 
 */
package org.datadryad.interop;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class DBConnectionImpl implements DBConnection{
    private Connection connection;
    private static final String CONNECTION_PROPERTIES_FILENAME = "Connection.properties"; 

    
    static final Logger logger = Logger.getLogger(DBConnectionImpl.class);

    @Override
    public void connect() {
        openDBFromPropertiesFile(CONNECTION_PROPERTIES_FILENAME);
    }
    
    public void openDBFromPropertiesFile(String propertyFileSpec){
        final Properties properties = new Properties();
        try {
            properties.load(this.getClass().getResourceAsStream(propertyFileSpec));
        } catch (Exception e1) {
            throw new RuntimeException("Failed to open connection properties file; path = " + CONNECTION_PROPERTIES_FILENAME);
        } 
        try {
            Class.forName("org.postgresql.Driver");
        } catch(ClassNotFoundException e){
            logger.error("Couldn't load PSQL Driver",e);
        }
        final String host = properties.getProperty("host");
        final String db = properties.getProperty("db");
        final String user = properties.getProperty("user");
        final String password = properties.getProperty("pw");
        try {
            connection = DriverManager.getConnection(String.format("jdbc:postgresql://%s/%s",host,db),user,password);
        } catch (SQLException e) {
            logger.error("Connection failed: host = " + host + "; db = " + db + "; user = " + user,e);
        }
    }

    @Override
    public String reconnect() {
        return null;
        // TODO Auto-generated method stub
        
    }

    @Override
    public void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public Connection getConnection(){
        return connection;
    }

}
