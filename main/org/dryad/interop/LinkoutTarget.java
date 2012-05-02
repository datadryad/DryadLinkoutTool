/*
 * NCBI Linkout generator for Dryad
 *
 * Created on May 2, 2012
 * Last updated on May 2, 2012
 * 
 */
package org.dryad.interop;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

public class LinkoutTarget {
    
    private final Document targetDocument;

    
    LinkoutTarget(DocumentBuilderFactory builderFactory) throws ParserConfigurationException{
        targetDocument = builderFactory.newDocumentBuilder().newDocument();
    }
}
