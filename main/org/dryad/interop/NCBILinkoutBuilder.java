/*
 * NCBI Linkout generator for Dryad
 *
 * Created on Feb 17, 2012
 * Last updated on Apr 6, 2012
 * 
 */
package org.dryad.interop;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.dryad.interop.DBConnectionImpl;
import org.dryad.interop.DBConnection;

public class NCBILinkoutBuilder {

    DBConnection dbc;
    
    static final String PACKAGECOLLECTIONNAME = "Dryad Data Packages";
    
    final Set<DryadPackage> dryadPackages = new HashSet<DryadPackage>();
    final Map<String,Set<String>> articleDataMap = new HashMap<String,Set<String>>();
    
    static final String NCBIEntrezPrefix = "";
    
    static final String NCBIDatabasePrefix = "http://www.ncbi.nlm.nih.gov";
    
    static final Map<String,String> NCBIDatabaseNames = new HashMap<String,String>(); //name (suffix) -> Abbreviation
    
    static final Map<String,String> doiToPMID = new HashMap<String,String>();
    
    
    static final Logger logger = Logger.getLogger(NCBILinkoutBuilder.class);
    
    /**
     * @param args
     * @throws SQLException 
     */
    public static void main(String[] args) throws SQLException {
        NCBILinkoutBuilder builder = new NCBILinkoutBuilder();
        builder.process();
    }

    private void process() throws SQLException{
        initNCBIDatabaseCollection();
        dbc = getConnection();
        DryadPackage.getPackages(dryadPackages,PACKAGECOLLECTIONNAME,dbc);
        for(DryadPackage dpackage : dryadPackages){
            Set<String>doi_set = dpackage.getPubDOIs();
            for (String doi : doi_set){
                DryadArticle art = new DryadArticle(doi);
                art.lookupPMID();
            }
        }
        logger.info("Found " + dryadPackages.size() + " packages");
        dbc.disconnect();
        
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
