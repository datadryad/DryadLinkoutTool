/*
 * 
 * Created on Dec 24, 2011
 * Last updated on Dec 30, 2011
 * 
 */
package org.DataDryad.LinkoutTool;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.dspace.authorize.AuthorizeException;

import org.dspace.content.Collection;
import org.dspace.content.DCDate;
import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.crosswalk.StreamIngestionCrosswalk;
import org.dspace.core.ConfigurationManager;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;
import org.dspace.doi.CDLDataCiteService;
import org.dspace.embargo.EmbargoManager;
import org.dspace.identifier.DOIIdentifierProvider;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class LinkoutTool extends AbstractCurationTask{
    
    Map<String,Set<String>> articleDataMap = new HashMap<String,Set<String>>();  //articleID -> datapackageID
    
    static Logger logger = Logger.getLogger(LinkoutTool.class.getName());

    @Override
    public void init(Curator curator, String taskID) throws IOException{
        super.init(curator, taskID);
    }

    
//    private void process(){
//        DryadDBConnection c = new DryadDBConnection();
//        String dbID = c.connect();
//        if (dbID == null){
//            logger.fatal("Could not open Dryad Database connection");
//            System.exit(0);
//        }
//        fillArticleDataMap(articleDataMap,c);
//    }

    
    private void fillArticleDataMap(Map<String,Set<String>> dataMap, DryadDBConnection c){
        
    }

    @Override
    public int perform(DSpaceObject arg0) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }
    
    
}
