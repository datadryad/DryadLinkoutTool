/*
 * LabsLink Linkout generator for Dryad
 *
 * Created on May 3, 2013
 * 
 */
package org.datadryad.interop;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;


import org.apache.log4j.Logger;

public class LabsLinkLinksBuilder {

    DBConnection dbc;
    
    static final String PACKAGECOLLECTIONNAME = "Dryad Data Packages";
    static final String DEFAULTLINKSFILE = "labslinklinkout";
    
    final Set<DryadPackage> dryadPackages = new HashSet<DryadPackage>();
    
    static final Logger logger = Logger.getLogger(LabsLinkLinksBuilder.class);
    
    /**
     * @param args
     * @throws SQLException 
     */
    public static void main(String[] args) throws Exception {
        final LabsLinkLinksBuilder builder = new LabsLinkLinksBuilder();
        String linkFileName = DEFAULTLINKSFILE;
        if (args.length == 1){
            linkFileName = args[0];
        }
        builder.readDataPackagesFromDatabase();
        builder.generateLinksFile(linkFileName);
    }


    private void readDataPackagesFromDatabase() throws SQLException {
        dbc = getConnection();
        DryadPackage.getPackages(dryadPackages,PACKAGECOLLECTIONNAME,dbc);
        dbc.disconnect();
    }

    /**
     * @throws ParserConfigurationException
     * @throws IOException 
     */
    private void generateLinksFile(String linksFile) throws IOException {
        //captured everything in a dryad article, now generate the xml linkout file
        final LabsLinkLinksTarget target = new LabsLinkLinksTarget();
        for (DryadPackage dryadPackage : dryadPackages){
            if(dryadPackage.hasPMID() && dryadPackage.hasDOI()) {
                target.addDryadPackage(dryadPackage);
            }
        }
        target.save(linksFile);
    }
    
    private DBConnection getConnection(){
        DBConnection result = new DBConnectionImpl();
        result.connect();
        return result;
    }
    
    
}
