/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.datadryad.interop;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

/**
 *
 * @author dan.leehr@nescent.org
 */
public class LabsLinkLinksTarget {
    private static final String BASE_URL = "https://doi.org/";
    private static final String DRYAD_PROVIDER_ID = "1012";
    private static final String PUBMED_RECORD_SOURCE = "MED";


    private List<DryadPackage> dryadPackages;

    public LabsLinkLinksTarget() {
        dryadPackages = new ArrayList<DryadPackage>();
    }

    final public void addDryadPackage(final DryadPackage dryadPackage) {
        dryadPackages.add(dryadPackage);
    }
    
    private Document build() {
        Element linksElement = new Element("links");
        for(DryadPackage dryadPackage : dryadPackages) {
            // <link providerId="1012">
            Element linkElement = new Element("link");
            linkElement.addAttribute(new Attribute("providerId", DRYAD_PROVIDER_ID));
            //   <resource>
            Element resourceElement = new Element("resource");
            //     <title>The Title of the Resource</title>
            Element titleElement = new Element("title");
            titleElement.appendChild(dryadPackage.getTitle());
            resourceElement.appendChild(titleElement);
            //     <url>http://url.for.resource</url>
            Element urlElement = new Element("url");
            urlElement.appendChild(BASE_URL + dryadPackage.getDOI());
            resourceElement.appendChild(urlElement);
            //   </resource>
            linkElement.appendChild(resourceElement);
            //   <record>
            Element recordElement = new Element("record");
            //     <source>MED</source> // MED for PMIDs
            Element sourceElement = new Element("source");
            sourceElement.appendChild(PUBMED_RECORD_SOURCE);
            recordElement.appendChild(sourceElement);
            //     <id>12345678</id> // the PMID, numbers only
            Element idElement = new Element("id");
            idElement.appendChild(dryadPackage.getPubPMID());
            recordElement.appendChild(idElement);
            //   </record>
            linkElement.appendChild(recordElement);
            // </link>
            linksElement.appendChild(linkElement);
        }
        Document doc = new Document(linksElement);
        return doc;
    }

    public void save(String fileName) throws FileNotFoundException, IOException {
        Document doc = build();
        FileOutputStream fos = new FileOutputStream(fileName + ".xml");
        Serializer ser = new Serializer(fos);
        ser.setIndent(4);
        ser.write(doc);
    }
}
