/*
 * NCBI Linkout generator for Dryad
 * 
 * Created on Apr 4, 2012
 * Last updated on Oct 24, 2012
 * 
 */
package org.datadryad.interop;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import org.apache.log4j.Logger;

public class DryadPackage {

    
    int itemid;
    private String publicationDOI;
    private String packageDOI;
    //TODO if there is no DOI, these fields should be filled to facilitate direct search
    private String journal;
    private String date;
    private String volume;
    private String issue;
    private String firstPage;
    private String authorName;
    private Publication publication;
    
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

    
    final static String IDENTIFIERQUERY = "SELECT metadata_field_id FROM metadatafieldregistry WHERE element='identifier' AND qualifier IS NULL";
    static int getIdentiferID(DBConnection dbc) throws SQLException{
        Statement s = dbc.getConnection().createStatement();
        ResultSet rs = s.executeQuery(IDENTIFIERQUERY);
        if (rs.next()){
            return rs.getInt(1);
        }
        else {
            final String message = "Could not retrieve metadata type 'dc:indentifier' from metadatafieldregistry";
            logger.fatal(message);
            throw new RuntimeException(message);
        }
    }

    final static String METADATAQUERY = "SELECT text_value FROM metadatavalue WHERE item_id = ? AND metadata_field_id = ?";
    public static String getPackageDOI(int packageItemID, int relationID, DBConnection dbc) throws SQLException {
        final PreparedStatement p = dbc.getConnection().prepareStatement(METADATAQUERY);
        String result;
        p.setInt(1,packageItemID);
        p.setInt(2,relationID);
        ResultSet rs = p.executeQuery();
        if (rs.next()){
            result = rs.getString(1);
            if (rs.next()){
                String second = rs.getString(1);
                if (!second.equals(result))
                    logger.error("Package ID " + packageItemID + " ref by id = " + relationID + " returned more than one DOI");
            }
        }
        else
            result = "";
        return result;
    }
    
    
    
    final static String PACKAGEITEMQUERY = "SELECT item_id FROM collection2item WHERE collection_id = ?";
    public static void getPackages(Set<DryadPackage> packages, String packageCollectionName, DBConnection dbc) throws SQLException {
        final int packageCollectionID = getPackageCollectionID(packageCollectionName,dbc);
        final int referencedByID = getIsReferencedByID(dbc);
        final int identifierID = getIdentiferID(dbc);
        logger.info("dc:relation:isreferencedby = " + referencedByID);
        logger.info("dc:identifier = " + identifierID);
        PreparedStatement p = dbc.getConnection().prepareStatement(PACKAGEITEMQUERY);
        p.setInt(1, packageCollectionID);
        ResultSet rs = p.executeQuery();
        while(rs.next()){
            int nextid = rs.getInt(1);
            String pub = getPackageDOI(nextid,referencedByID,dbc);
            String myDoi = getPackageDOI(nextid,identifierID,dbc);
            DryadPackage newPackage = new DryadPackage(nextid,pub,myDoi);
            packages.add(newPackage);
        }
    }

    DryadPackage(int id, String pub, String pkgDOI){
        itemid = id;
        publicationDOI = pub;
        packageDOI = pkgDOI;
    }
    
    public void setPublication(Publication pub){
        publication = pub;
    }
    
    public String getPubDOI(){
        return publicationDOI;
    }
    
    public String getDOI(){
        return packageDOI;
    }
   

    public Publication directLookup() {
        // TODO Auto-generated method stub
        return null;
    }

    public Publication getPub() {
        return publication;
    }
}
