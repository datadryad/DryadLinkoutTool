/*
 * EthOntos - a tool for comparative methods using ontologies
 * Copyright 2004-2005 Peter E. Midford
 * 
 * Created on Apr 4, 2012
 * Last updated on Apr 4, 2012
 * 
 */
package org.dryad.interop;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class DryadPackage {

    
    int itemid;
    String doi;
    
    final static String PACKAGECOLLECTIONQUERY = "SELECT collection_id FROM collection WHERE name = ?";
    
    static int getPackageCollectionID(String packageCollectionName, DBConnection dbc) throws SQLException{
        PreparedStatement p = dbc.getConnection().prepareStatement(PACKAGECOLLECTIONQUERY);
        p.setString(1, packageCollectionName);
        ResultSet rs = p.executeQuery();
        if (rs.next()){
            return rs.getInt(1);
        }
        else
            return -1;
    }
    
    final static String PACKAGEITEMQUERY = "SELECT item_id FROM collection2item WHERE collection_id = ?";
    public static void getPackages(Set<DryadPackage> packages, String packageCollectionName, DBConnection dbc) throws SQLException {
        final int packageCollectionID = getPackageCollectionID(packageCollectionName,dbc);
        PreparedStatement p = dbc.getConnection().prepareStatement(PACKAGEITEMQUERY);
        p.setInt(1, packageCollectionID);
        ResultSet rs = p.executeQuery();
        while(rs.next()){
            int nextid = rs.getInt(1);
            DryadPackage newPackage = new DryadPackage(nextid);
            packages.add(newPackage);
        }
        // TODO Auto-generated method stub
        
    }

    DryadPackage(int id){
        itemid = id;
    }
    
    
}
