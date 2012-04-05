/*
 * 
 * Created on Feb 17, 2012
 * Last updated on Feb 17, 2012
 * 
 */
package org.dryad.interop;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.dryad.interop.DBConnectionImpl;
import org.dryad.interop.DBConnection;
import org.dspace.content.Collection;

public class NCBILinkoutBuilder {

    DBConnection dbc;
    
    static final String PACKAGECOLLECTIONNAME = "Dryad Data Packages";
    
    Set<DryadPackage> dryadPackages = new HashSet<DryadPackage>();
    static final Logger logger = Logger.getLogger(NCBILinkoutBuilder.class);
    
    /**
     * @param args
     * @throws SQLException 
     */
    public static void main(String[] args) throws SQLException {
        BasicConfigurator.configure();
        NCBILinkoutBuilder builder = new NCBILinkoutBuilder();
        builder.process();
    }

    private void process() throws SQLException{
        dbc = getConnection();
        DryadPackage.getPackages(dryadPackages,PACKAGECOLLECTIONNAME,dbc);
        logger.info("Found " + dryadPackages.size() + " packages");
        dbc.disconnect();
        
    }
    
    
    
    
    private DBConnection getConnection(){
        DBConnection result = new DBConnectionImpl();
        result.connect();
        return result;
    }
    
}
