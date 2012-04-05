/*
 * EthOntos - a tool for comparative methods using ontologies
 * Copyright 2004-2005 Peter E. Midford
 * 
 * Created on Dec 27, 2011
 * Last updated on Dec 27, 2011
 * 
 */
package org.DataDryad.LinkoutTool;

import static org.junit.Assert.*;

import org.dryad.interop.DBConnection;
import org.dryad.interop.DBConnectionImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestDBConnection {

    
    private DBConnection testConnection;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        testConnection = new DBConnectionImpl();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConnect() {
        String s = testConnection.connect();
        System.out.println(s); 
    }

    @Test
    public void testOpenDBFromPropertiesFile() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testReconnect() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testDisconnect() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testIsConnected() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testGetConnection() {
        fail("Not yet implemented"); // TODO
    }

}
