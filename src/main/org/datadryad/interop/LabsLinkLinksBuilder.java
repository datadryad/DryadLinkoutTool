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
/**
 * Generates LabsLink XML files of Dryad Content for EuropePMC.
 * Similar to NCBILinkoutBuilder, but does not modify Dryad content.
 * Simply generates links for content in Dryad with PMID and DOI.
 * @author Dan Leehr <dan.leehr@nescent.org>
 */
public class LabsLinkLinksBuilder {

    DBConnection dbc;
    
    static final String PACKAGECOLLECTIONNAME = "Dryad Data Packages";
    static final String DEFAULTLINKSFILE = "labslinklinkout";
    static final String DEFAULTPROFILEFILE = "labslinkprofile";
    
    final Set<DryadPackage> dryadPackages = new HashSet<DryadPackage>();
    
    static final Logger logger = Logger.getLogger(LabsLinkLinksBuilder.class);
    
    /**
     * @param args
     * @throws SQLException 
     */
    public static void main(String[] args) throws Exception {
        final LabsLinkLinksBuilder builder = new LabsLinkLinksBuilder();
        String linkFileName = DEFAULTLINKSFILE;
        String profileFileName = DEFAULTPROFILEFILE;
        if (args.length == 2){
            linkFileName = args[0];
            profileFileName = args[1];
        }
        builder.readDataPackagesFromDatabase();
        builder.generateLinksFile(linkFileName);
        builder.generateProfileFile(profileFileName);
    }


    private void readDataPackagesFromDatabase() throws SQLException {
        dbc = getConnection();
        DryadPackage.getPackages(dryadPackages,PACKAGECOLLECTIONNAME,dbc);
        dbc.disconnect();
    }

    /**
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

    private void generateProfileFile(String profileFile) throws IOException {
        final LabsLinkProfileTarget target = new LabsLinkProfileTarget();
        target.save(profileFile);
    }
    
    private DBConnection getConnection(){
        DBConnection result = new DBConnectionImpl();
        result.connect();
        return result;
    }
    
    
}
