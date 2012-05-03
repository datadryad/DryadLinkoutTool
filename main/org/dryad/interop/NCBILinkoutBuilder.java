/*
 * NCBI Linkout generator for Dryad
 *
 * Created on Feb 17, 2012
 * Last updated on May 3, 2012
 * 
 */
package org.dryad.interop;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.dryad.interop.DBConnectionImpl;
import org.dryad.interop.DBConnection;
import nu.xom.*;

import org.xml.sax.SAXException;

public class NCBILinkoutBuilder {

    DBConnection dbc;
    
    static final String PACKAGECOLLECTIONNAME = "Dryad Data Packages";
    
    final Set<DryadPackage> dryadPackages = new HashSet<DryadPackage>();
    final Set<Publication> publications = new HashSet<Publication>();
    
    static final String NCBIEntrezPrefix = "";
    
    static final String NCBIDatabasePrefix = "http://www.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pubmed&db=";
    
    static final Map<String,String> NCBIDatabaseNames = new HashMap<String,String>(); //name (suffix) -> Abbreviation
    
    static final Map<String,String> doiToPMID = new HashMap<String,String>();
        
    static final Logger logger = Logger.getLogger(NCBILinkoutBuilder.class);
    
    /**
     * @param args
     * @throws SQLException 
     */
    public static void main(String[] args) throws Exception {
        NCBILinkoutBuilder builder = new NCBILinkoutBuilder();
        builder.process();
    }

    private void process() throws Exception{
        initNCBIDatabaseCollection();
        dbc = getConnection();
        int noDOI = 0;
        int noPMID = 0;
        DryadPackage.getPackages(dryadPackages,PACKAGECOLLECTIONNAME,dbc);
        for(DryadPackage dpackage : dryadPackages){
            Set<String>doi_set = dpackage.getPubDOIs();
            if (doi_set.size() == 0){
                noDOI++;
            }
            else{
                for (String doi : doi_set){
                    Publication pub = new Publication(doi);
                    publications.add(pub);
                    pub.lookupPMID();
                    if (pub.getPMIDs().size() == 0)
                        noPMID++;
                }
            }
        }
        logger.info("Found " + dryadPackages.size() + " packages");
        logger.info("Found " + noDOI + " packages with no DOIs");
        logger.info("Found " + noPMID + " publications with DOIs that resolved to no PMIDs");
        dbc.disconnect();
        int queryCount = 0;
        int hitCount = 0;
        for(Publication pub : publications){
            if (pub.getPMIDs().size() >0){
                for (String pmid : pub.getPMIDs()){
                    for (String dbName : NCBIDatabaseNames.keySet()){
                        String query = NCBIDatabasePrefix + NCBIDatabaseNames.get(dbName) + "&id=" + pmid;
                        Document d = queryNCBI(query);
                        if (d != null){
                            boolean processResult = processQueryReturn(d, query, pub);
                            if (processResult && pub.hasPMIDLinks(pmid)){
                                hitCount++;
                            }
                        }
                        queryCount++;
                        if (queryCount % 10 == 0){
                            logger.info("Processed " + queryCount + " queries, with " + hitCount + " returning linklist results");
                        }
                        
                    }
                }
            }
        }
        //captured everything in a dryad article, now generate the xml linkout file
        LinkoutTarget target = new LinkoutTarget();
        for (Publication pub : publications){
            target.addPublication(pub);
        }
    }
    
    
    private Document queryNCBI(String query) throws  ValidityException, ParsingException, IOException{
            Builder builder = new Builder();
            Document d = builder.build(query);
            return d;
    }
    
    private boolean processQueryReturn(Document d, String query, Publication pub){
        Element root = d.getRootElement();
        if (root.getChildCount()>0 && "eLinkResult".equals(root.getLocalName())){
            return processELinkResult(root,query,pub);
        }
        else
            return false;
    }
    
    
    
    private boolean processELinkResult(Node eLinkElement,String query, Publication pub){
        boolean valid = true;
        for(int i = 0; i<eLinkElement.getChildCount() && valid; i++){
            final Node nChild = eLinkElement.getChild(i);
            if (nChild instanceof Element){
                Element child = (Element)nChild;
                if ("LinkSet".equals(child.getLocalName()) && child.getChildCount()>0){
                    valid = processLinkSetElement(child,query,pub);
                }
            }
            else if (nChild instanceof Text || nChild instanceof Comment){
                //ignore
            }
        }
        return valid;
    }
    
    private boolean processLinkSetElement(Node linkSetElement, String query, Publication pub){
        boolean valid = true;
        for(int i = 0; i<linkSetElement.getChildCount() && valid; i++){
            final Node nChild = linkSetElement.getChild(i);
            if (nChild instanceof Element){
                final Element child = (Element)nChild;
                if ("DbFrom".equals(child.getLocalName())){
                    String sourceDB = checkDbFrom(child);
                    if (!"pubmed".equals(sourceDB)){
                        logger.warn("Source DB is not pubmed..." + query);
                    }
                valid = (sourceDB != null);
                }
                else if ("IdList".equals(child.getLocalName())){
                    valid = checkIdList(child);
                }
                else if ("LinkSetDb".equals(child.getLocalName())){
                    valid = processLinkSetDb(child,query,pub);
                }
                else 
                    System.out.println("LinkSet child name = " + child.getLocalName() + " Child count = " + child.getChildCount()); 
            }
        }
        return valid;
    }
    
    
    private String checkDbFrom(Node dbFromElement){
        if (dbFromElement.getChildCount()>=1){
            if (dbFromElement.getChild(0) instanceof Text){
                Text child = (Text)dbFromElement.getChild(0);
                return child.getValue();
            }
            System.out.println("Bad DbFrom element: " + dbFromElement);
            return null;
        }
        System.out.println("Bad DbFrom element child count: " + dbFromElement.getChildCount());
        return null;
    }
    
    private boolean checkIdList(Node idListElement){
        return true;
    }
    
    
    private boolean processLinkSetDb(Node linkSetDbElement, String query, Publication pub){
        boolean valid = true;
        for (int i = 0; i<linkSetDbElement.getChildCount() && valid; i++){
            final Node nChild = linkSetDbElement.getChild(i);
            if (nChild instanceof Element){
                final Element child = (Element)nChild;
                if ("DbTo".equals(child.getLocalName())){
                    String targetDB = checkDbTo(child);
                    if (!"pubmed".equals(targetDB))
                        System.out.println("targetDB: " + targetDB);
                    valid = (targetDB != null);
                }
                else if ("LinkName".equals(child.getLocalName())){
                    String linkName = checkLinkName(child);
                    System.out.println("Link Name: " + linkName);
                    valid = (linkName != null);
                }
                else if ("Link".equals(child.getLocalName())){

                }
                else 
                    System.out.println("LinkSetDb child name = " + child.getLocalName() + " Child count = " + child.getChildCount());            
            }
            else if (nChild instanceof Text || nChild instanceof Comment){
            //ignore
            }
        }
        return valid;
    }

    private String checkDbTo(Node dbToElement){
        if (dbToElement.getChildCount()>= 1){
            if (dbToElement.getChild(0) instanceof Text){
                Text child = (Text)dbToElement.getChild(0);
                return child.getValue();
            }
            System.out.println("Bad DbTo element: " + dbToElement);
            return null;
        }
        System.out.println("Bad DbTo element child count: " + dbToElement.getChildCount());
        return null;
    }
    
    private String checkLinkName(Node linkNameElement){
        if (linkNameElement.getChildCount()>=1){
            if (linkNameElement.getChild(0) instanceof Text){
                Text child = (Text)linkNameElement.getChild(0);
                return child.getValue();
            }
            System.out.println("Bad LinkName element: " + linkNameElement);
            return null;
        }
        System.out.println("Bad LinkName element child count: " + linkNameElement.getChildCount());
        return null;
    }
    
    
    private DBConnection getConnection(){
        DBConnection result = new DBConnectionImpl();
        result.connect();
        return result;
    }
    
    //could be done statically
    private void initNCBIDatabaseCollection(){
        NCBIDatabaseNames.put("gene","Gene");
        NCBIDatabaseNames.put("nucleotide","Nucleotide");
        NCBIDatabaseNames.put("est","NucEST");
        NCBIDatabaseNames.put("gss","NucGSS");
        NCBIDatabaseNames.put("pubmed","PubMed");
        NCBIDatabaseNames.put("protein","Protein");
        NCBIDatabaseNames.put("taxonomy","Taxonomy");
        NCBIDatabaseNames.put("bioproject","BioProject");
    }
    
}
