/*
 * NCBI Linkout generator for Dryad
 *
 * Created on Feb 17, 2012
 * Last updated on Apr 2, 2012
 * 
 */
package org.dryad.interop;

import java.io.IOException;
import java.io.InputStream;
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class NCBILinkoutBuilder {

    DBConnection dbc;
    
    static final String PACKAGECOLLECTIONNAME = "Dryad Data Packages";
    
    final Set<DryadPackage> dryadPackages = new HashSet<DryadPackage>();
    final Set<DryadArticle> dryadArticles = new HashSet<DryadArticle>();
    
    static final String NCBIEntrezPrefix = "";
    
    static final String NCBIDatabasePrefix = "http://www.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pubmed&db=";
    
    static final Map<String,String> NCBIDatabaseNames = new HashMap<String,String>(); //name (suffix) -> Abbreviation
    
    static final Map<String,String> doiToPMID = new HashMap<String,String>();
    
    static final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    
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
                    DryadArticle art = new DryadArticle(doi);
                    dryadArticles.add(art);
                    art.lookupPMID();
                    if (art.getPMIDs().size() == 0)
                        noPMID++;
                }
            }
        }
        logger.info("Found " + dryadPackages.size() + " packages");
        logger.info("Found " + noDOI + " packages with no DOIs");
        logger.info("Found " + noPMID + " packages with DOIs that resolved to no PMIDs");
        dbc.disconnect();
        int queryCount = 0;
        int hitCount = 0;
        for(DryadArticle art : dryadArticles){
            if (art.getPMIDs().size() >0){
                for (String pmid : art.getPMIDs()){
                    for (String dbName : NCBIDatabaseNames.keySet()){
                        String query = NCBIDatabasePrefix + NCBIDatabaseNames.get(dbName) + "&id=" + pmid;
                        Document d = queryNCBI(query);
                        if (d != null){
                            boolean processResult = processQueryReturn(d, query, art);
                            if (processResult){
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
        LinkoutTarget target = new LinkoutTarget(builderFactory);
        for (DryadArticle art : dryadArticles){
            target.addArticle(art);
        }
    }
    
    
    private Document queryNCBI(String query) throws IOException, ParserConfigurationException, SAXException{
        InputStream s = null;
        try {
            URL u = new URL(query);
            s = u.openStream();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document d = builder.parse(s);
            return d;
        }
        finally{
            if (s != null)
                    s.close();
        }
    }
    
    private boolean processQueryReturn(Document d, String query, DryadArticle art){
        boolean valid=true;
        for(Node docChild = d.getFirstChild();docChild != null && valid; docChild=docChild.getNextSibling()){
            if ("eLinkResult".equals(docChild.getNodeName()) && docChild.hasChildNodes()){
                valid = processELinkResult(docChild,query,art);
            }
        }
        return valid;
    }
    
    
    
    private boolean processELinkResult(Node eLinkElement,String query, DryadArticle art){
        boolean valid = true;
        for(Node child = eLinkElement.getFirstChild();child != null; child = child.getNextSibling()){
            if (child.getNodeType() == Node.TEXT_NODE){
                //ignore
            }
            else if ("LinkSet".equals(child.getNodeName()) && child.hasChildNodes()){
                valid = processLinkSetElement(child,query,art);
            }
        }
        return valid;
    }
    
    private boolean processLinkSetElement(Node linkSetElement, String query, DryadArticle art){
        boolean valid = true;
        for(Node child = linkSetElement.getFirstChild();child != null; child = child.getNextSibling()){
            if (child.getNodeType() == Node.TEXT_NODE){
                //ignore
            }
            else if ("DbFrom".equals(child.getNodeName())){
                String sourceDB = checkDbFrom(child);
                if (!"pubmed".equals(sourceDB)){
                    logger.warn("Source DB is not pubmed..." + query);
                }
                valid = (sourceDB != null);
            }
            else if ("IdList".equals(child.getNodeName())){
                valid = checkIdList(child);
            }
            else if ("LinkSetDb".equals(child.getNodeName())){
                valid = processLinkSetDb(child,query,art);
            }
            else 
                System.out.println("LinkSet child name = " + child.getNodeName() + " Child count = " + child.getChildNodes().getLength());            
        }
        return valid;
    }
    
    
    private String checkDbFrom(Node dbFromElement){
        if (dbFromElement.hasChildNodes()){
            if (dbFromElement.getChildNodes().getLength()==1){
                Node child = dbFromElement.getFirstChild();
                return(child.getTextContent());
            }
            System.out.println("Bad DbFrom element child count: " + dbFromElement.getChildNodes().getLength());
            return null;
        }
        System.out.println("Bad DbFrom element: " + dbFromElement);
        return null;
    }
    
    private boolean checkIdList(Node idListElement){
        return true;
    }
    
    
    private boolean processLinkSetDb(Node linkSetDbElement, String query, DryadArticle art){
        boolean valid = true;
        for (Node child = linkSetDbElement.getFirstChild();child != null;child =child.getNextSibling()){
            if (child.getNodeType() == Node.TEXT_NODE){
                //ignore
            }
            else if ("DbTo".equals(child.getNodeName())){
                String targetDB = checkDbTo(child);
                if (!"pubmed".equals(targetDB))
                    System.out.println("targetDB: " + targetDB);
                valid = (targetDB != null);
            }
            else if ("LinkName".equals(child.getNodeName())){
                String linkName = checkLinkName(child);
                System.out.println("Link Name: " + linkName);
                valid = (linkName != null);
            }
            else if ("Link".equals(child.getNodeName())){
                
            }
            else 
                System.out.println("LinkSetDb child name = " + child.getNodeName() + " Child count = " + child.getChildNodes().getLength());            
        }
        return valid;
    }
    
    private String checkDbTo(Node dbToElement){
        if (dbToElement.hasChildNodes()){
            if (dbToElement.getChildNodes().getLength()==1){
                Node child = dbToElement.getFirstChild();
                return(child.getTextContent());
            }
            System.out.println("Bad DbTo element child count: " + dbToElement.getChildNodes().getLength());
            return null;
        }
        System.out.println("Bad DbTo element: " + dbToElement);
        return null;
    }
    
    private String checkLinkName(Node LinkNameElement){
        if (LinkNameElement.hasChildNodes()){
            if (LinkNameElement.getChildNodes().getLength()==1){
                Node child = LinkNameElement.getFirstChild();
                return(child.getTextContent());
            }
            System.out.println("Bad LinkName element child count: " + LinkNameElement.getChildNodes().getLength());
            return null;
        }
        System.out.println("Bad LinkName element: " + LinkNameElement);
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
