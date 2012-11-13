/*
 * NCBI Linkout generator for Dryad
 *
 * Created on Oct 18, 2012
 * Last updated on Oct 24, 2012
 * 
 */
package org.datadryad.interop;

import java.util.Date;

/**
 * This class handles links to other databases (e.g., genbank, taxonomy, etc.) 
 */
public class OtherTarget extends LinkoutTarget {
    
    
    final static String RESOURCEBASE = "http://datadryad.org/resource/";

    private int linkCount = 0;
    
    String generateLinkSet(){
        final StringBuilder result = new StringBuilder(500*packages.size());
        result.append("<" + DTDROOTELEMENT + ">\n");
        for (DryadPackage pkg : packages){
            if (pkg.hasSeqLinks()){                           
                for (String dbName : pkg.getSeqDBs()){
                    result.append(generateLink(pkg,dbName,generateProviderId(),generateIconUrl()));
                }
            }
        }
        result.append("</" + DTDROOTELEMENT + ">\n");       
        return result.toString();
    }

    private String generateLinkId(){
        final StringBuilder result = new StringBuilder(60);
        result.append(getIndent(2));        
        result.append("<LinkId>");
        result.append("dryad.seq.");
        result.append(DATEF.format(new Date()));
        result.append(".");
        result.append(Integer.toString(linkCount++));
        result.append("</LinkId>\n");
        return result.toString();
    }
    
    

    private String generateLink(DryadPackage pkg, String db, String provider, String iconURL){
        final StringBuilder result = new StringBuilder(500);
        result.append(getIndent(1));        
        result.append("<Link>\n");
        result.append(generateLinkId());
        result.append(provider);
        result.append(iconURL);
        result.append(generateObjectSelector(pkg,db));
        result.append(generateObjectUrl(pkg));
        result.append(getIndent(1));        
        result.append("</Link>\n");
        return result.toString();
    }

    //TODO what a package that has references from multiple databases
    private String generateObjectSelector(DryadPackage pkg,String db){
        final StringBuilder result = new StringBuilder(300);
        result.append(getIndent(2));
        result.append("<ObjectSelector>\n");
        result.append(generateDatabase(db));
        result.append(generateObjectList(pkg,db));
        result.append(getIndent(2));
        result.append("</ObjectSelector>\n");
        return result.toString();
    }

    
    private String generateObjectList(DryadPackage pkg,String db){
        final StringBuilder result = new StringBuilder(120);
        result.append(getIndent(3));
        result.append("<ObjectList>\n");
        for (SequenceRecord sr : pkg.getSeqLinksforDB(db)){
            result.append(generateObjIdElement(sr.getID()));
            result.append("\n");   //insert xml comment here??
        }
        result.append(getIndent(3));
        result.append("</ObjectList>\n");
        return result.toString();
    }
    
    
    String generateObjectUrl(DryadPackage pkg){
        final StringBuilder result = new StringBuilder(120);
        result.append(getIndent(2));
        result.append("<ObjectUrl>\n");
        result.append(generateBase());  //Note that the URL base should be the same for both target types
        result.append(generateRule(pkg));
        result.append(getIndent(2));
        result.append("</ObjectUrl>\n");
        return result.toString();
    }

    String generateBase(){
        StringBuilder result = new StringBuilder(30);
        result.append(getIndent(3));
        result.append("<Base>");
        result.append(RESOURCEBASE);
        result.append("</Base>\n");
        return result.toString();
    }

    
    String generateRule(DryadPackage pkg){
        final StringBuilder result = new StringBuilder(30);
        result.append(getIndent(3));
        result.append("<Rule>");
        result.append(pkg.getDOI());
        result.append("</Rule>\n");
        return result.toString();
    }
    

}
