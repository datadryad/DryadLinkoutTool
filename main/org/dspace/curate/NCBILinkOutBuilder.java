/*
 * 
 * Created on Dec 24, 2011
 * Last updated on Dec 30, 2011
 * 
 */
package org.dspace.curate;

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

public class NCBILinkOutBuilder extends AbstractCurationTask{
    
    Map<String,Set<String>> articleDataMap = null;  //articleID -> set of datapackageID
    
    static Logger logger = Logger.getLogger(NCBILinkOutBuilder.class.getName());

    @Override
    public void init(Curator curator, String taskID) throws IOException{
        super.init(curator, taskID);
        
        // init article to data package mapping
        articleDataMap = new HashMap<String,Set<String>>();
    }


    
    @Override
    public int perform(DSpaceObject arg0) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }
    
    
}
