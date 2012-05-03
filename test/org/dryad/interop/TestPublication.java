/*
 * NCBI Linkout generator for Dryad
 * 
 * Created on Apr 6, 2012
 * Last updated on Apr 6, 2012
 * 
 */
package org.dryad.interop;

import static org.junit.Assert.*;

import java.util.Set;

import org.apache.log4j.Logger;
import org.dryad.interop.Publication;
import org.dryad.interop.NCBILinkoutBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestPublication {
    
    static final String testdoi = "doi:10.1111/j.1469-7580.2009.01108.x";
    static final String brokendoi = "doi: 10.1111/j.1365-2699.2008.02015.x";

    

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testDryadArticle() {
        final Publication testArticle = new Publication(testdoi);
        assertEquals(testdoi,testArticle.getDOI());
    }

    @Test
    public void testLookupPMID() {
        final Publication testArticle = new Publication(testdoi);
        testArticle.lookupPMID();
        Set<String> pmtest = testArticle.getPMIDs();
        Assert.assertNotNull(pmtest);
        Assert.assertFalse(pmtest.isEmpty());
        for (String id : pmtest){
            Assert.assertEquals("19549004", id);
        }
        final Publication brokenArticle = new Publication(brokendoi);
        brokenArticle.lookupPMID();
        pmtest = brokenArticle.getPMIDs();
        Assert.assertNotNull(pmtest);
        Assert.assertFalse(pmtest.isEmpty());
        for (String id : pmtest){
            System.out.println("id = " + id);
        }
        
    }

}
