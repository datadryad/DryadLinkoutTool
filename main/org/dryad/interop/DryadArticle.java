/*
 * NCBI Linkout generator for Dryad
 * 
 * Created on Apr 6, 2012
 * Last updated on Apr 6, 2012
 * 
 */
package org.dryad.interop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
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

public class DryadArticle {
    
    private String doi;
    private Set<String>pmids;
    
    final static String PMIDQUERYURI = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=";
    final static String PMIDQUERYSUFFIX = "[doi]"; 
    
    static final Logger logger = Logger.getLogger(DryadArticle.class);

    public DryadArticle(String newDOI){
        doi = newDOI;
    }

    public void lookupPMID() {
        try {
            URL lookupURL;
            if (doi.charAt(4) == ' '){
                lookupURL = new URL(PMIDQUERYURI+doi.substring(5)+PMIDQUERYSUFFIX);                
            }
            else {
                lookupURL = new URL(PMIDQUERYURI+doi.substring(4)+PMIDQUERYSUFFIX);
            }
            pmids = processPubmedXML(lookupURL);
        } catch (MalformedURLException e) {
            final String message = "Article's DOI " + doi + " could not be parsed into a valid NCBI esearch URL";
            logger.warn(message);
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
                logger.warn("No id's found for " + doi);
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
            Node idItem = nl.item(i);
            final String content = idItem.getTextContent();
            if (content != null && content.length()>4)
                result.add(content);
        }
        return result;
        
    }
    
    public String getDOI(){
        return doi;
    }
    
    public Set<String>getPMIDs(){
        return pmids;
    }
    
}
