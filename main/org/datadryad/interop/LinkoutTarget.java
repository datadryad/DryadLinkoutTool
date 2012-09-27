/*
 * NCBI Linkout generator for Dryad
 *
 * Created on May 2, 2012
 * Last updated on May 2, 2012
 * 
 */
package org.datadryad.interop;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nu.xom.Builder;
import nu.xom.Comment;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;
import nu.xom.Text;


public class LinkoutTarget {
    
    public enum TargetType {PUBLINKS,SEQUENCELINKS}
    
    TargetType myType;
    
    
    final String ICONURL = "http://www.nescent.org/wg/dryad/images/7/7f/DryadLogo-Button.png";
    final String DRYADBASE = "http://datadryad.org/discover?";
    final String DRYADRULE = "query=&lo.doi;";
    
    
    final Set <DryadPackage> packages = new HashSet<DryadPackage>();
    
    LinkoutTarget(TargetType tp) throws ParserConfigurationException{
        myType = tp;
    }


    void addPackage(DryadPackage pkg) {
        packages.add(pkg);
        
    }
    
    void save(String targetFile) throws IOException{
        Element root = generateLinkSet();
        Document targetDocument = new Document(root);
        
        FileOutputStream s = new FileOutputStream(targetFile, false);
        Serializer serializer = new Serializer(s);
        serializer.setIndent(3);
        serializer.setMaxLength(72);
        serializer.write(targetDocument);
    }
    
    Element generateLinkSet(){
        Element result = new Element("linkSet");
        for (DryadPackage pkg : packages){
            result.appendChild(generateComment(pkg));
            result.appendChild(generateLink(pkg));
        }
        return result;
    }
    
    Comment generateComment(DryadPackage pkg){
        return new Comment("Link for publication: " + pkg.getPubDOI());
    }
    
    Element generateLink(DryadPackage pkg){
        Element result = new Element("link");
        result.appendChild(generateLinkId());
        result.appendChild(generateProviderId());
        result.appendChild(generateIconUrl());
        result.appendChild(generateObjectSelector(pkg));
        result.appendChild(generateObjectUrl(pkg));
        return result;
    }
    
    
    Element generateLinkId(){
        Element result = new Element("LinkId");
        if (myType == TargetType.PUBLINKS)
            result.appendChild("dryad.pubmed");
        else
            result.appendChild("dryad.seq");
        return result;
    }
    
    Element generateProviderId(){
        Element result = new Element("ProviderId");
        result.appendChild("7893");
        return result;
    }
    
    Element generateIconUrl(){
        Element result = new Element("IconUrl");
        result.appendChild(ICONURL);
        return result;
    }
    
    
    //TODO what a package that has references from multiple databases
    Element generateObjectSelector(DryadPackage pkg){
        Element result = new Element("ObjectSelector");
        result.appendChild(generateDatabase(pkg));
        result.appendChild(generateObjectList(pkg));
        return result;
    }
    
    Element generateDatabase(DryadPackage pkg){
        Element result = new Element("Database");
        if (TargetType.PUBLINKS == myType){
            result.appendChild("PubMed");
        }
        else {
            result.appendChild("TBD");
        }
        return result;
    }
    
    Element generateObjectList(DryadPackage pkg){
        Element result = new Element("ObjectList");
        final Publication pub = pkg.getPub();
        if (pub != null){
            Set<String> pmids = pub.getPMIDs();
            for(String pmid : pmids){
                result.appendChild("ObjId");
            }
        }
        return result;
    }
    
    Element generateObjectUrl(DryadPackage pkg){
        Element result = new Element("ObjectUrl");
        result.appendChild(generateBase());
        result.appendChild(generateRule());
        return result;
    }
    
    Element generateBase(){
        Element result = new Element("Base");
        result.appendChild(DRYADBASE);
        return result;
    }
    
    Element generateRule(){
        Element result = new Element("Rule");
        result.appendChild(DRYADRULE);
        return result;
    }
    
    
    
    
    
    
}
