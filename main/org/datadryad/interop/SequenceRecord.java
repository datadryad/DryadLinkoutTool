/*
 * 
 * Created on May 10, 2012
 * Last updated on May 10, 2012
 * 
 */
package org.datadryad.interop;

public class SequenceRecord {
    
    final private String dbName;
    final private String dbID;
    private String accession;
    
    public SequenceRecord(String db, String id){
        dbName = db;
        dbID = id;
        accession = null;   //can test for not set
    }
    
    public String getDB(){
        return dbName;
    }
    
    public String getID(){
        return dbID;
    }
    
    public void setAccession(String acc){
        accession = acc;
    }
    
    public String getAccession(){
        return accession;
    }

}
