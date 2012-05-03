/*
 * NCBI Linkout generator for Dryad
 *
 * Created on May 2, 2012
 * Last updated on May 2, 2012
 * 
 */
package org.dryad.interop;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;
import nu.xom.Text;


public class LinkoutTarget {
    
    
    LinkoutTarget() throws ParserConfigurationException{
    }


    void addPackage(DryadPackage pkg) {
        // TODO Auto-generated method stub
        
    }
    
    void save(String targetFile) throws IOException{
        Element root = new Element("linkSet");
        Document targetDocument = new Document(root);
        
        FileOutputStream s = new FileOutputStream(targetFile, false);
        Serializer serializer = new Serializer(s);
        serializer.setIndent(3);
        serializer.setMaxLength(64);
        serializer.write(targetDocument);
    }
    
    Element generateLink(DryadPackage pkg){
        Element result = new Element("link");
        Element linkId = new Element("LinkId");
        Text linkIdString = new Text(genLinkIdString());
        linkId.appendChild(linkIdString);
        return result;
    }
    
    String genLinkIdString(){
        return "dryad.pubmed";   //TODO append a date string so this looks like "dryad.pubmed.2011-10-31"
    }
}
