/*
 * NCBI Linkout generator for Dryad
 *
 * Created on Oct 18, 2012
 * Last updated on Oct 18, 2012
 * 
 */
package org.datadryad.interop;

import java.util.Date;
import java.util.Set;

public class PubMedTarget extends LinkoutTarget {

    String generateLinkSet(){
        final StringBuilder result = new StringBuilder(500*packages.size());
        result.append("<" + DTDROOTELEMENT + ">\n");
        result.append(generateLink());
        result.append("</" + DTDROOTELEMENT + ">\n");       
        return result.toString();
    }

    protected String generateLinkId(){
        final StringBuilder result = new StringBuilder(60);
        result.append(getIndent(2));        
        result.append("<LinkId>");
        result.append("dryad.pubmed.");
        result.append(DATEF.format(new Date()));
        result.append("</LinkId>\n");
        return result.toString();
    }
    
    private String generateLink(){
        final StringBuilder result = new StringBuilder(500);
        result.append(getIndent(1));        
        result.append("<Link>\n");
        result.append(generateLinkId());
        result.append(generateProviderId());
        result.append(generateIconUrl());
        result.append(generateObjectSelector());
        result.append(generateObjectUrl());
        result.append(getIndent(1));        
        result.append("</Link>\n");
        return result.toString();
    }

    String generateObjectUrl(){
        final StringBuilder result = new StringBuilder(120);
        result.append(getIndent(2));
        result.append("<ObjectUrl>\n");
        result.append(generateBase());
        result.append(generateRule());
        result.append(getIndent(2));
        result.append("</ObjectUrl>\n");
        return result.toString();
    }

    String generateRule(){
        final StringBuilder result = new StringBuilder(30);
        result.append(getIndent(3));
        result.append("<Rule>");
        result.append(DRYADRULE);
        result.append("</Rule>\n");
        return result.toString();
    }
    

    private String generateObjectSelector(){
        final StringBuilder result = new StringBuilder(300);
        result.append(getIndent(2));
        result.append("<ObjectSelector>\n");
        result.append(generateDatabase("PubMed"));
        result.append(generateObjectList());
        result.append(getIndent(2));
        result.append("</ObjectSelector>\n");
        return result.toString();
    }

    
    String generateBase(){
        StringBuilder result = new StringBuilder(30);
        result.append(getIndent(3));
        result.append("<Base>");
        result.append(DRYADBASE);
        result.append("</Base>\n");
        return result.toString();
    }

    
    private String generateObjectList(){
        final StringBuilder result = new StringBuilder(120);
        result.append(getIndent(3));
        result.append("<ObjectList>\n");
        for (DryadPackage pkg : packages){
            if (pkg.getPubDOI().length()>0){  //this is temporary, until we can lookup things in pubmed from metadata
                if (!pkg.getPMIDs().isEmpty()){  //this should be caught upstream somewhere...
                    Set<String> pmids = pkg.getPMIDs();
                    for(String pmid : pmids){
                        String objIdElement = generateObjIdElement(pmid);
                        result.append(objIdElement);
                        result.append("  <!-- ");
                        result.append(pkg.getPubDOI());
                        result.append(" -->\n");
                    }
                }
            }
        }
        result.append(getIndent(3));
        result.append("</ObjectList>\n");
        return result.toString();
    }

    
}
