/*
 * NCBI Linkout generator for Dryad
 *
 * Created on Oct 18, 2012
 * Last updated on Oct 24, 2012
 * 
 */
package org.datadryad.interop;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * This class handles links to other databases (e.g., genbank, taxonomy, etc.) 
 */
public class OtherTarget extends LinkoutTarget {
    final static String RESOURCEBASE = "http://datadryad.org/resource/";

    private final static long OBJECTLIMIT = 100000L;
    private final static long BYTE_LIMIT = 16 * 1024 * 1024L; // 16MB

    private int linkCount = 0;   //incremented for each generated Link
    private long byteCount = BYTE_LIMIT; // updated on append() call
    private long objectCount = OBJECTLIMIT; // incremented on addObjectLink()
    private String targetFile;
    private OutputStreamWriter writer;
    private FileOutputStream outputStream;
    private int fileCount = 0;

    private DryadPackage currentPackage;
    private String dbName;

    private void startFile() throws IOException {
        if(writer != null) {
            endFile();
        }
        byteCount = 0L;
        objectCount = 0L;
        fileCount++;
        String nextFileName = String.format("%s%06d.xml", targetFile, fileCount);
        outputStream = new FileOutputStream(nextFileName, false);
        writer = new OutputStreamWriter(outputStream);
        
        append(generateHeader()); // <xml> and <dtd>
        append(openLinkSet()); // <LinkSet>
    }

    private void append(final String text) throws IOException {
        writer.append(text);
        byteCount += text.length();
    }

    private boolean fileFull() {
        return byteCount >= BYTE_LIMIT || objectCount >= OBJECTLIMIT;
    }

    private void endFile() throws IOException {
        append(closeLinkSet());
        writer.flush();
        writer.close();
        outputStream.flush();
        outputStream.close();
        outputStream = null;
        writer = null;
    }

    private void startPackage() throws IOException {
        String linkHeader = generateLinkHeader(generateProviderId(), generateIconUrl());
        append(linkHeader);
    }

    private void endPackage() throws IOException {
        String linkFooter = generateLinkFooter();
        append(linkFooter);
    }

    private void startObjectSelector() throws IOException {
        String objectSelectorHeader = generateObjectSelectorHeader();
        append(objectSelectorHeader);
    }

    private void endObjectSelector() throws IOException {
        String objectSelectorFooter = generateObjectSelectorFooter();
        append(objectSelectorFooter);
    }

    private void startObjectList() throws IOException {
        String objectListHeader = generateObjectListHeader();
        append(objectListHeader);
    }

    private void endObjectList() throws IOException {
        String objectListFooter = generateObjectListFooter();
        append(objectListFooter);
    }

    @Override
    void save(String targetFile) throws IOException{
        this.targetFile = targetFile;
        startFile();
        for (DryadPackage pkg : packages) {
            if (pkg.hasSeqLinks()){
                this.currentPackage = pkg;
                // Loop over sequence databases
                for (String sequenceDBName : pkg.getSeqDBs()) {
                    this.dbName = sequenceDBName;
                    // Get the object IDs for the current package and NCBI database
                    List<String> objectIds = objectIdsForCurentPackageAndDatabase();
                    // Only create a <Link> if we have 1 or more objects
                    if(objectIds.size() > 0) { 
                        startPackage(); // generates link header
                        startObjectSelector();
                        startObjectList();
                        boolean changedFileOnLastObject = false;
                        for(int i=0;i< objectIds.size();i++) {
                            String objectId = objectIds.get(i);
                            // Add object before checking if full
                            // because we may be "full" after the opening <ObjectList> tag
                            // and an empty <ObjectList> is not valid
                            addObjectLink(objectId);
                            // Check if we've reached the object limit or size limit
                            // This is not super precise, because full only returns true
                            // if we're over the limit - for simplicity.
                            // NCBI FTP's file size limit is 32MB, so we set our files
                            // to limit much lower
                            if(fileFull()) {
                                // Close current stack
                                endObjectList();
                                endObjectSelector();
                                generateLinkFooter();
                                endPackage();
                                endFile();
                                startFile();
                                if(i == objectIds.size() - 1) {
                                    // To prevent writing an opening ObjectList tag
                                    // if it will be empty
                                    changedFileOnLastObject = true;
                                }
                                // Start new stack, but only if there are more
                                // objects to write for this database
                                if(!changedFileOnLastObject) {
                                    startPackage();
                                    startObjectSelector();
                                    startObjectList();
                                }
                            }
                        }
                        // Add the closing tags, but only if we're in the middle
                        // of a list

                        if(!changedFileOnLastObject) {
                            endObjectList();
                            endObjectSelector();
                            endPackage();
                        }
                    }
                }
            }
        }
        endFile();
    }

    private void addObjectLink(String objectId) throws IOException {
        String objectTag = generateObjIdElement(objectId);
        append(objectTag);
        append("\n");
        objectCount++;
    }

    private List<String> objectIdsForCurentPackageAndDatabase() {
        List<String> objectIds = new ArrayList<String>();
        for (SequenceRecord sr : this.currentPackage.getSeqLinksforDB(this.dbName)){
            objectIds.add(sr.getID());
        }
        return objectIds;
    }

    private String generateHeader(){
        final StringBuilder header = new StringBuilder();
        header.append("<?xml version=" + '"' + "1.0" + '"' + " encoding=" + '"' + "UTF-8" + '"' + "?>\n");
        header.append("<!DOCTYPE " + DTDROOTELEMENT + " PUBLIC ");
        header.append('"' + DTDPUBLICID + '"');
        header.append(" " + '"' + DTDURL + '"' + ">\n");
        return header.toString();
    }


    String openLinkSet(){
        return "<" + DTDROOTELEMENT + ">\n";
    }

    String closeLinkSet(){
        return "</" + DTDROOTELEMENT + ">\n";       
    }

    private String generateLinkHeader(String provider, String iconURL) {
        final StringBuilder result = new StringBuilder(500);
        result.append(getIndent(1));
        result.append("<Link>\n");
        result.append(generateLinkId());
        result.append(provider);
        result.append(iconURL);
        return result.toString();
    }
    
    private String generateLinkFooter() {
        final StringBuilder result = new StringBuilder(500);
        result.append(generateObjectUrl(currentPackage));
        result.append(getIndent(1));        
        result.append("</Link>\n");
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

    private String generateObjectSelectorHeader() {
        final StringBuilder result = new StringBuilder(300);
        result.append(getIndent(2));
        result.append("<ObjectSelector>\n");
        result.append(generateDatabase(this.dbName));
        return result.toString();
    }

    private String generateObjectSelectorFooter() {
        final StringBuilder result = new StringBuilder(300);
        result.append(getIndent(2));
        result.append("</ObjectSelector>\n");
        return result.toString();
    }

    private String generateObjectListHeader() {
        final StringBuilder result = new StringBuilder(300);
        result.append(getIndent(3));
        result.append("<ObjectList>\n");
        return result.toString();
    }

    private String generateObjectListFooter() {
        final StringBuilder result = new StringBuilder(300);
        result.append(getIndent(3));
        result.append("</ObjectList>\n");
        return result.toString();
    }

    String generateObjectUrl(DryadPackage pkg){
        final StringBuilder result = new StringBuilder(120);
        result.append(getIndent(2));
        result.append("<ObjectUrl>\n");
        result.append(generateBase()); 
        result.append(generateRule(pkg));
        result.append(generateSubjectType());
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
        result.append(StringEscapeUtils.escapeHtml(pkg.getDOI()));
        result.append("</Rule>\n");
        return result.toString();
    }

}
