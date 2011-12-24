/*
 * 
 * Created on Dec 24, 2011
 * Last updated on Dec 24, 2011
 * 
 */
package org.DataDryad.LinkoutTool;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LinkoutTool {
    
    Map<String,Set<String>> articleDataMap = new HashMap<String,Set<String>>();  //articleID -> datapackageID

    /**
     * @param args
     */
    public static void main(String[] args) {
        LinkoutTool t = new LinkoutTool();
        t.process();
        // TODO Auto-generated method stub

    }
    
    private void process(){
        fillArticleDataMap(articleDataMap);
    }

    
    private void fillArticleDataMap(Map<String,Set<String>> dataMap){
        
    }
    
    
}
