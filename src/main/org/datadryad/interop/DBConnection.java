/*
 * Created on Feb 17, 2012
 * Last updated on Feb 17, 2012
 * 
 */
package org.datadryad.interop;

import java.sql.Connection;

public interface DBConnection {
    void connect();
    
    String reconnect();
    
    void disconnect();
    
    boolean isConnected();
    
    Connection getConnection();

}
