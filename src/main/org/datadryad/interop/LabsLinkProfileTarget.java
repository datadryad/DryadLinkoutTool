/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.datadryad.interop;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

/**
 *
 * @author dan.leehr@nescent.org
 */
public class LabsLinkProfileTarget {
    private static final String DRYAD_PROVIDER_ID = "1012";
    private static final String DRYAD_RESOURCE_NAME = "Dryad Digital Repository";
    private static final String DRYAD_DESCRIPTION = "Dryad is a nonprofit organization and an international repository of data underlying scientific and medical publications.";
    private static final String DRYAD_EMAIL = "linkout@datadryad.org";

    private Document build() {
        // <providers>
        Element providersElement = new Element("providers");
        //   <provider>
        Element providerElement = new Element("provider");
        //     <id>1012</id>
        Element idElement = new Element("id");
        idElement.appendChild(DRYAD_PROVIDER_ID);
        providerElement.appendChild(idElement);

        //     <resourceName>Dryad Digital Repository</resourceName>
        Element resourceNameElement = new Element("resourceName");
        resourceNameElement.appendChild(DRYAD_RESOURCE_NAME);
        providerElement.appendChild(resourceNameElement);

        //     <description>...</description>
        Element descriptionElement = new Element("description");
        descriptionElement.appendChild(DRYAD_DESCRIPTION);
        providerElement.appendChild(descriptionElement);

        //     <email>admin@datadryad.org</email>
        Element emailElement = new Element("email");
        emailElement.appendChild(DRYAD_EMAIL);
        providerElement.appendChild(emailElement);

        //   </provider>
        providersElement.appendChild(providerElement);

        // </providers>;

        Document doc = new Document(providersElement);
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
