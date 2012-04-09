/*
 * NCBI Linkout generator for Dryad
 * 
 * Created on Apr 4, 2012
 * Last updated on Apr 6, 2012
 * 
 */
package org.dryad.interop;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

public class DryadPackage {

    
    int itemid;
    private Set<String> pubDOIs;
    
    
    static final Logger logger = Logger.getLogger(NCBILinkoutBuilder.class);

    
    final static String PACKAGECOLLECTIONQUERY = "SELECT collection_id FROM collection WHERE name = ?";
    
    static int getPackageCollectionID(String packageCollectionName, DBConnection dbc) throws SQLException{
        PreparedStatement p = dbc.getConnection().prepareStatement(PACKAGECOLLECTIONQUERY);
        p.setString(1, packageCollectionName);
        ResultSet rs = p.executeQuery();
        if (rs.next()){
            return rs.getInt(1);
        }
        else{
            final String message = "Could not retrieve package collection id for packageCollectionName";
            logger.fatal(message);
            throw new RuntimeException(message);
        }
    }
    
    final static String REFERENCEDBYQUERY = "SELECT metadata_field_id FROM metadatafieldregistry WHERE element='relation' AND qualifier='isreferencedby'";
    static int getIsReferencedByID(DBConnection dbc) throws SQLException{
        Statement s = dbc.getConnection().createStatement();
        ResultSet rs = s.executeQuery(REFERENCEDBYQUERY);
        if (rs.next()){
            return rs.getInt(1);
        }
        else {
            final String message = "Could not retrieve metadata type 'dc:relation.isreferencedby' from metadatafieldregistry";
            logger.fatal(message);
            throw new RuntimeException(message);
        }
    }
    
    final static String METADATAQUERY = "SELECT text_value FROM metadatavalue WHERE item_id = ? AND metadata_field_id = ?";
    public static Set<String> getPackagePublicationDOI(int packageItemID, int refbyID, DBConnection dbc) throws SQLException {
        final PreparedStatement p = dbc.getConnection().prepareStatement(METADATAQUERY);
        final Set<String> result = new HashSet<String>();
        p.setInt(1,packageItemID);
        p.setInt(2,refbyID);
        ResultSet rs = p.executeQuery();
        while (rs.next()){
            result.add(rs.getString(1));
        }
        for(String ref : result){
            logger.info("referring id = " + ref);
        }
        
        return result;
    }
    
    
    
    final static String PACKAGEITEMQUERY = "SELECT item_id FROM collection2item WHERE collection_id = ?";
    public static void getPackages(Set<DryadPackage> packages, String packageCollectionName, DBConnection dbc) throws SQLException {
        final int packageCollectionID = getPackageCollectionID(packageCollectionName,dbc);
        final int referencedByID = getIsReferencedByID(dbc);
        logger.info("dc:relation:isreferencedby = " + referencedByID);
        PreparedStatement p = dbc.getConnection().prepareStatement(PACKAGEITEMQUERY);
        p.setInt(1, packageCollectionID);
        ResultSet rs = p.executeQuery();
        while(rs.next()){
            int nextid = rs.getInt(1);
            Set<String> dois = getPackagePublicationDOI(nextid,referencedByID,dbc);
            DryadPackage newPackage = new DryadPackage(nextid,dois);
            packages.add(newPackage);
        }
    }

    DryadPackage(int id, Set<String> dois){
        itemid = id;
        pubDOIs = dois;
    }
    
    public Set<String>getPubDOIs(){
        return pubDOIs;
    }
}
