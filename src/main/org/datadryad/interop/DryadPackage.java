/*
 * NCBI Linkout generator for Dryad
 * 
 * Created on Apr 4, 2012
 * Last updated on Oct 24, 2012
 * 
 */
package org.datadryad.interop;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DryadPackage {

    
    private Set<String>pmids = new HashSet<String>();
    private HashMap<String,Set<SequenceRecord>> sequenceLinks = new HashMap<String,Set<SequenceRecord>>();
    
    final static String PMIDQUERYURI = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=";
    final static String PMIDQUERYSUFFIX = "[doi]"; 
    final static String DRYADDOIPREFIX = "doi:10.5061/dryad";
    final static String DRYADHTTPPREFIX = "http://dx.doi.org/10.5061/dryad";
    final static String DOIPREFIX = "doi:";
    final static String HTTPDOIPREFIX = "http://dx.doi.org/";

    
    int itemid;
    private String publicationDOI = null;
    private String publicationPMID = null;
    private String packageDOI = null;
    //TODO if there is no DOI, these fields should be filled to facilitate direct search
    private String journal;
    private String date;
    private String volume;
    private String issue;
    private String firstPage;
    private String authorName;
    
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
    static int getIsReferencedByFieldCode(DBConnection dbc) throws SQLException{
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
    static int getIdentiferFieldCode(DBConnection dbc) throws SQLException{
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
    public static Set<String> queryMetaData(int packageItemID, int relationID, DBConnection dbc) throws SQLException {
        final PreparedStatement p = dbc.getConnection().prepareStatement(METADATAQUERY);
        final Set<String>result = new HashSet<String>();
        p.setInt(1,packageItemID);
        p.setInt(2,relationID);
        ResultSet rs = p.executeQuery();
        while (rs.next()){
            final String nextString = rs.getString(1);
            result.add(nextString);
        }
        return result;
    }
    
    
    
    final static String PACKAGEITEMQUERY = "SELECT item_id FROM collection2item WHERE collection_id = ?";
    public static void getPackages(Set<DryadPackage> packages, String packageCollectionName, DBConnection dbc) throws SQLException {
        final int packageCollectionID = getPackageCollectionID(packageCollectionName,dbc);
        final int referencedByFieldCode = getIsReferencedByFieldCode(dbc);
        final int identifierFieldCode = getIdentiferFieldCode(dbc);
        logger.info("dc:relation:isreferencedby = " + referencedByFieldCode);
        logger.info("dc:identifier = " + identifierFieldCode);
        PreparedStatement p = dbc.getConnection().prepareStatement(PACKAGEITEMQUERY);
        p.setInt(1, packageCollectionID);
        ResultSet rs = p.executeQuery();
        while(rs.next()){
            final int nextid = rs.getInt(1);
            final Set<String> identifiers = queryMetaData(nextid,identifierFieldCode,dbc);
            final Set<String> myPubIDs = queryMetaData(nextid,referencedByFieldCode,dbc);  //this is every identifier linked to the package using referenedBy; doi's and PMIDs
            String myDoi = null;
            for (String doiCandidate: identifiers){
                if (doiCandidate.startsWith(DRYADDOIPREFIX) || doiCandidate.startsWith(DRYADHTTPPREFIX)){
                    if (myDoi == null){
                        myDoi = doiCandidate;
                    }
                    else {
                        logger.warn("package has multiple doi identifiers: " + myDoi + ", " + doiCandidate);
                    }
                }
            }
            if (myDoi == null){
                //logger.warn("package has no doi identifier");
            }
            String pubDOI = null;
            String pubPMID = null;
            for (String pubId : myPubIDs){
                if (pubId.startsWith("PMID") || pubId.startsWith("pmid")){
                    pubPMID = pubId;
                }
                if (pubId.startsWith(DOIPREFIX) || pubId.startsWith(HTTPDOIPREFIX)){
                    pubDOI = pubId;
                }
            }
            DryadPackage newPackage = new DryadPackage(nextid,pubDOI,pubPMID,myDoi);
            packages.add(newPackage);
        }
    }

    DryadPackage(int id, String pubDOI, String pubPMID,String pkgDOI){
        itemid = id;
        publicationDOI = pubDOI;
        publicationPMID = pubPMID;
        packageDOI = pkgDOI;
    }
    
    
    public String getPubDOI(){
        return publicationDOI;
    }
    
    public String getDOI(){
        return packageDOI;
    }
   


    public void lookupPMID(DBConnection dbc) {
        if (publicationPMID != null){
            return;  //got one already, assume it's good
        }
        //otherwise if the publicationPMID was not set when the package was loaded by getPackages, try querying NCBI using the doi
        try { 
            URL lookupURL;
            if (publicationDOI.charAt(4) == ' '){
                lookupURL = new URL(PMIDQUERYURI+publicationDOI.substring(5)+PMIDQUERYSUFFIX);                
            }
            else {
                lookupURL = new URL(PMIDQUERYURI+publicationDOI.substring(4)+PMIDQUERYSUFFIX);
            }
            pmids = processPubmedXML(lookupURL);
            if (pmids.size() >1){
                logger.debug("Publication " + publicationDOI + " has " + pmids.size() + " pmids");
            }
            if (pmids.size() == 0){
                logger.debug("Publication " + publicationDOI + " has 0 pmids");
            }
            else {
                final String rawPMID = pmids.iterator().next();  //get the first element of a what should be a singleton collection
                if (rawPMID.startsWith("PMID")){
                    publicationPMID = rawPMID;
                }
                else if (rawPMID.startsWith("pmid")){
                    publicationPMID = "PMID" + rawPMID.substring("pmid".length());
                }
                else{
                    publicationPMID = "PMID:" + rawPMID;
                }
                final int referenced_by_id = getIsReferencedByFieldCode(dbc);
                insertMetaData(itemid,referenced_by_id,dbc);
            }
        } catch (MalformedURLException e) {
            final String message = "Article's DOI " + publicationDOI + " could not be parsed into a valid NCBI esearch URL";
            logger.warn(message);
        } catch (SQLException e) {
            final String message = "Error while trying to add PMID to metadata for " + publicationDOI;
            logger.error(message,e);
        }
    }

    
    final private Set<String>emptyStringSet = new HashSet<String>();

    Set<String> processPubmedXML(URL pubmedURL){
        InputStream source = null;
        try {
            source = pubmedURL.openStream();
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            DocumentBuilder db = f.newDocumentBuilder();
            db.setErrorHandler(new DefaultHandler());
            Document d = db.parse(source);
            NodeList countNodes = d.getElementsByTagNameNS("","Count");
            if (countNodes.getLength() == 0){
                logger.warn("No id's found for " + publicationDOI);
                return emptyStringSet;
            }
            NodeList idList = d.getElementsByTagNameNS("", "IdList");
            return processPubMedIDNodes(idList);
        } catch (ParserConfigurationException e) {
            logger.fatal("Error in initializing parser");
            e.printStackTrace();
            return emptyStringSet;
        } catch (SAXException e) {
            logger.warn("Error in parsing XML returned by " + pubmedURL + "; skipping article");
            logger.warn("Exception message is: " + e.getLocalizedMessage());
            return emptyStringSet;
        } catch (IOException e) {
            logger.warn("Error opening stream for URL: " + pubmedURL + "; skipping article");
            return emptyStringSet;
        } 
        finally{
            if (source != null)
                try {
                    source.close();
                } catch (IOException e) {
                    logger.fatal("Error closing URL Stream");
                    throw new RuntimeException("This shouldn't be happening");
                }
        }
    }
 
    private Set<String> processPubMedIDNodes(NodeList nl){
        final Set<String> result = new HashSet<String>();
        for (int i=0;i<nl.getLength();i++){
            final Node idItem = nl.item(i);
            final String content = idItem.getTextContent();
            if (content != null && content.length()>4)
                result.add(content);
        }
        return result;
        
    }

    
    final static String METADATAINSERT = "INSERT INTO metadatavalue (item_id,metadata_field_id,text_value,place) VALUES(?,?,?,?)";
    public void insertMetaData(int packageItemId, int relationFieldCode, DBConnection dbc) throws SQLException {
        final PreparedStatement p = dbc.getConnection().prepareStatement(METADATAINSERT);
        final int place = getMaxPlaceValue(packageItemId, relationFieldCode, dbc)+1;
        logger.info("Inserting PMID " + publicationPMID + " for " + packageDOI + " in place " + place);
        p.setInt(1, packageItemId);
        p.setInt(2, relationFieldCode);
        p.setString(3, publicationPMID);
        p.setInt(4,place);
        p.executeUpdate();
    }
    
    final static String METADATAPLACEQUERY = "SELECT place FROM metadatavalue WHERE item_id = ? AND metadata_field_id = ?";
    private int getMaxPlaceValue(int packageItemId, int relationFieldCode,DBConnection dbc) throws SQLException {
        final PreparedStatement p = dbc.getConnection().prepareStatement(METADATAPLACEQUERY);
        p.setInt(1, packageItemId);
        p.setInt(2, relationFieldCode);
        ResultSet rs = p.executeQuery();
        int maxPlace = 0;
        while(rs.next()){
            int place = rs.getInt(1);
            if (place>maxPlace){
                maxPlace = place;
            }
        }
        return maxPlace;
    }
    
    
    public Set<String>getPMIDs(){
        return pmids;
    }

    public boolean hasPMIDLinks(String pmid) {
        // TODO Auto-generated method stub
        return false;
    }

    public void addSequenceLink(String db, String dbId){
        final SequenceRecord newLink = new SequenceRecord(db,dbId);
        if (sequenceLinks.containsKey(db)){
            final Set<SequenceRecord> s = sequenceLinks.get(db);
            s.add(newLink);
        }
        else {
            final Set<SequenceRecord> s = new HashSet<SequenceRecord>();
            sequenceLinks.put(db, s);
            s.add(newLink);
        }
    }
    

    
    public boolean hasSeqLinks(){
        return !sequenceLinks.isEmpty();
    }
    
    public Set<String> getSeqDBs(){
        return sequenceLinks.keySet();
    }
    
    public Collection<SequenceRecord> getSeqLinksforDB(String db){
        return sequenceLinks.get(db);
    }

    public boolean hasDOI(){
        return packageDOI != null && packageDOI != "";
    }
    
    public boolean hasPubDOI(){
        return publicationDOI != null && publicationDOI != "";
    }

    public boolean hasPMID(){
        return publicationPMID != null && publicationPMID.startsWith("PMID:");
    }
    
    public String getPubPMID(){
        return publicationPMID.substring("PMID:".length());
    }
    
    public void directLookup() {
        // TODO Auto-generated method stub
        
    }

    
}
