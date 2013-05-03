/*
 * NCBI Linkout generator for Dryad
 *
 * This file created on May 4, 2012
 * Last updated on May 4, 2012
 * 
 */

package org.datadryad.interop;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestLinkoutTarget {
    
    private LinkoutTarget testPubTarget;
    private LinkoutTarget testSequenceTarget;
    private DryadPackage testPackage1;
    private DryadPackage testPackage2;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        testPubTarget = new PubMedTarget();
        testSequenceTarget = new OtherTarget();
        testPackage1 = new DryadPackage(0, null,null,null,null);
        testPackage2 = new DryadPackage(1, null,null,null,null);
        
        testPubTarget.addPackage(testPackage1);
        testPubTarget.addPackage(testPackage2);

        testSequenceTarget.addPackage(testPackage1);
        testSequenceTarget.addPackage(testPackage2);
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testSave() throws Exception{
        testPubTarget.save("pubtest1.xml");
        testSequenceTarget.save("seqtest1.xml");
    }
    
    @Test
    public void testGenerateLink() throws Exception{
        
    }

}
