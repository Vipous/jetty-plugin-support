// ========================================================================
// Copyright (c) 2009-2009 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses.
// ========================================================================

package org.eclipse.jetty.start;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/* ------------------------------------------------------------ */
/**
 */
public class StartIniParserTest
{
    StartIniParser startIniParser = new StartIniParser();

    /* ------------------------------------------------------------ */
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        System.setProperty("jetty.home",this.getClass().getResource("/jetty.home").getFile());
    }

    /**
     * Test method for {@link org.eclipse.jetty.start.StartIniParser#loadStartIni(java.lang.String)}.
     */
    @Test
    public void testLoadStartIni()
    {
        URL startIni = this.getClass().getResource("/jetty.home/start.ini");
        String startIniFileName = startIni.getFile();
        List<String> args = startIniParser.loadStartIni(startIniFileName);
        assertEquals("Expected 9 uncommented lines in start.ini",9,args.size());
        assertEquals("First uncommented line in start.ini doesn't match expected result","OPTIONS=Server,jsp,resources,websocket,ext",args.get(0));
    }

    /**
     * Test method for {@link org.eclipse.jetty.start.StartIniParser#getAdditionalStartIniFiles().
     */
    @Test
    public void testGetAdditionalStartIniFiles()
    {
        List<File> files = startIniParser.getAdditionalStartIniFiles();
        assertEquals(2,files.size());
    }

}
