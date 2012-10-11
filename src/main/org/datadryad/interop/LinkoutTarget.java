/*
 * NCBI Linkout generator for Dryad
 *
 * Created on May 2, 2012
 * Last updated on May 2, 2012
 * 
 */
package org.datadryad.interop;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;


public class LinkoutTarget {
    
    public enum TargetType {PUBLINKS,SEQUENCELINKS}
    
    TargetType myType;
    
    final static int INDENTSIZE = 3;
    
    final static String ICONURL = "http://www.nescent.org/wg/dryad/images/7/7f/DryadLogo-Button.png";
    final static String DRYADBASE = "http://datadryad.org/discover?";
    final static String DRYADRULE = "query=" + "&lo.doi;";
    final static String DTDROOTELEMENT = "LinkSet";
    final static String DTDPUBLICID = "-//NLM//DTD LinkOut 1.0//EN";
    final static String DTDURL = "http://www.ncbi.nlm.nih.gov/entrez/linkout/doc/LinkOut.dtd";
    
    
    final Set <DryadPackage> packages = new HashSet<DryadPackage>();
    
    LinkoutTarget(TargetType tp) throws ParserConfigurationException{
        myType = tp;
    }


    void addPackage(DryadPackage pkg) {
        packages.add(pkg);
        
    }
    
    
    //NCBI seems to require query=&lo.doi;" which isn't really legal xml, so xom is out...
    void save(String targetFile) throws IOException{
        FileOutputStream s = new FileOutputStream(targetFile, false);
        OutputStreamWriter w = new OutputStreamWriter(s);
        w.append("<?xml version=" + '"' + "1.0" + '"' + "encoding=" + '"' + "UTF-8" + '"' + "?>\n");
        w.append("<!DOCTYPE " + DTDROOTELEMENT + " PUBLIC ");
        w.append('"' + DTDPUBLICID + '"');
        w.append(" " + '"' + DTDURL + '"' + ">\n");
        String root = generateLinkSet();
        w.append(root);
        w.close();
    }
    
    private String generateLinkSet(){
        StringBuilder result = new StringBuilder(500*packages.size());
        result.append("<" + DTDROOTELEMENT + ">\n");
        for (DryadPackage pkg : packages){
            if (pkg.getPubDOI().length()>0){  //this is temporary, until we can lookup things in pubmed from metadata
                if (!pkg.getPub().getPMIDs().isEmpty()){  //this should be caught upstream somewhere...
                    result.append(generateComment(pkg));
                    result.append(generateLink(pkg));
                }
            }
        }
        result.append("</" + DTDROOTELEMENT + ">\n");       
        return result.toString();
    }
    
    private String generateComment(DryadPackage pkg){
        StringBuilder result = new StringBuilder(100);
        result.append(getIndent(1));
        result.append("<!-- Link for publication: ");
        result.append(pkg.getPubDOI());
        result.append("-->\n");
        return result.toString();
    }
    
    private String generateLink(DryadPackage pkg){
        StringBuilder result = new StringBuilder(500);
        result.append(getIndent(1));        
        result.append("<Link>\n");
        result.append(generateLinkId());
        result.append(generateProviderId());
        result.append(generateIconUrl());
        result.append(generateObjectSelector(pkg));
        result.append(generateObjectUrl(pkg));
        result.append(getIndent(1));        
        result.append("</Link>\n");
        return result.toString();
    }
    
    
    private String generateLinkId(){
        StringBuilder result = new StringBuilder(60);
        result.append(getIndent(2));        
        result.append("<LinkId>");
        if (myType == TargetType.PUBLINKS)
            result.append("dryad.pubmed.");
        else
            result.append("dryad.seq.");
        result.append(".yyyy-mm-dd");
        result.append("</LinkId>\n");
        return result.toString();
    }
    
    private String generateProviderId(){
        StringBuilder result = new StringBuilder(60);
        result.append(getIndent(2));
        result.append("<ProviderId>");
        result.append("7893");
        result.append("</ProviderId>\n");
        return result.toString();
    }
    
    private String generateIconUrl(){
        StringBuilder result = new StringBuilder(60);
        result.append(getIndent(2));
        result.append("<IconUrl>");
        result.append(ICONURL);
        result.append("</IconUrl>\n");
        return result.toString();
    }
    
    
    //TODO what a package that has references from multiple databases
    private String generateObjectSelector(DryadPackage pkg){
        StringBuilder result = new StringBuilder(300);
        result.append(getIndent(2));
        result.append("<ObjectSelector>\n");
        result.append(generateDatabase(pkg));
        result.append(generateObjectList(pkg));
        result.append("</ObjectSelector>\n");
        return result.toString();
    }
    
    String generateDatabase(DryadPackage pkg){
        StringBuilder result = new StringBuilder(30);
        result.append(getIndent(3));
        result.append("<Database>");
        if (TargetType.PUBLINKS == myType){
            result.append("PubMed");
        }
        else {
            result.append("TBD");
        }
        result.append("</Database>\n");
        return result.toString();
    }
    
    private String generateObjectList(DryadPackage pkg){
        StringBuilder result = new StringBuilder(120);
        result.append(getIndent(3));
        result.append("<ObjectList>\n");
        final Publication pub = pkg.getPub();
        if (pub != null){
            Set<String> pmids = pub.getPMIDs();
            for(String pmid : pmids){
                String objIdElement = generateObjIdElement(pmid);
                result.append(objIdElement);
            }
        }
        result.append(getIndent(3));
        result.append("</ObjectList>\n");
        return result.toString();
    }
    
    
    private String generateObjIdElement(String dbId){
        StringBuilder result = new StringBuilder(40);
        result.append(getIndent(4));
        result.append("<ObjId>");
        result.append(dbId);
        result.append("</ObjId>\n");
        return result.toString();
    }
    
    
    
    private String generateObjectUrl(DryadPackage pkg){
        StringBuilder result = new StringBuilder(120);
        result.append(getIndent(2));
        result.append("<ObjectUrl>\n");
        result.append(generateBase());
        result.append(generateRule());
        result.append(getIndent(2));
        result.append("</ObjectUrl>\n");
        return result.toString();
    }
    
    private String generateBase(){
        StringBuilder result = new StringBuilder(30);
        result.append(getIndent(3));
        result.append("<Base>");
        result.append(DRYADBASE);
        result.append("</Base>\n");
        return result.toString();
    }
    
    private String generateRule(){
        StringBuilder result = new StringBuilder(30);
        result.append(getIndent(3));
        result.append("<Rule>");
        result.append(DRYADRULE);
        result.append("</Rule>\n");
        return result.toString();
    }
    
    final private static String indents = "     ";
    private String getIndent(int indentCount){
        if (indentCount == 0)
            return "";
        if (indentCount == 1)
            return indents.substring(0,INDENTSIZE);
        StringBuilder result = new StringBuilder(INDENTSIZE*indentCount);
        for(int i = 0; i<indentCount; i++){
            result.append(indents.substring(0,INDENTSIZE));
        }
        return result.toString();
    }
    
    
    
}
