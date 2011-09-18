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

package org.eclipse.jetty.util;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Test;

/* ------------------------------------------------------------ */
/**
 */
public class Utf8MicroBenchmark
{
    byte[] bytes = new byte[6];

    @Before
    public void setup()
    {
        bytes[0] = (byte)0xC3;
        bytes[1] = (byte)0xBC;
        bytes[2] = (byte)0xC3;
        bytes[3] = (byte)0xB6;
        bytes[4] = (byte)0xC3;
        bytes[5] = (byte)0xA4;
    }

    @Test
    public void testLongText() throws Exception
    {
        for (int i = 0; i < 16; i++)
        {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 3000000; j++)
            {
                sendLongText();
            }
            long timeSpent = System.currentTimeMillis() - start;
            System.out.println("Long New: Time spent: " + timeSpent + "ms");
        }
    }
    
    @Test
    public void testLongTextOldDecoder() throws Exception
    {
        for (int i = 0; i < 16; i++)
        {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 3000000; j++)
            {
                sendLongTextOldDecoder();
            }
            long timeSpent = System.currentTimeMillis() - start;
            System.out.println("Long Old: Time spent: " + timeSpent + "ms");
        }
    }

    private void sendLongText() throws UnsupportedEncodingException
    {
        String source = "abcd012345\n\r\u0000\u00a4\u10fb\ufffdjetty";
        byte[] bytes = source.getBytes(StringUtil.__UTF8);
        Utf8StringBuffer buffer = new Utf8StringBuffer();
        for (int k = 0; k < bytes.length; k++)
            buffer.append(bytes[k]);
        assertEquals(source,buffer.toString());
    }
    
    private void sendLongTextOldDecoder() throws UnsupportedEncodingException
    {
        String source = "abcd012345\n\r\u0000\u00a4\u10fb\ufffdjetty";
        byte[] bytes = source.getBytes(StringUtil.__UTF8);
        Utf8StringBufferOldDecoder buffer = new Utf8StringBufferOldDecoder();
        for (int k = 0; k < bytes.length; k++)
            buffer.append(bytes[k]);
        assertEquals(source,buffer.toString());
    }

    @Test
    public void testGermanUmlautsBenchmark() throws Exception
    {
        for (int i = 0; i < 16; i++)
        {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 3000000; j++)
            {
                Utf8StringBuffer buffer = new Utf8StringBuffer();
                for (int k = 0; k < bytes.length; k++)
                    buffer.append(bytes[k]);
                assertEquals("ŸšŠ",buffer.toString());
            }
            long timeSpent = System.currentTimeMillis() - start;
            System.out.println("Umlauts New: Time spent: " + timeSpent + "ms");
        }
    }

    @Test
    public void testGermanUmlautsBenchmarkOldDecoder() throws Exception
    {
        for (int i = 0; i < 16; i++)
        {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 3000000; j++)
            {
                Utf8StringBufferOldDecoder buffer = new Utf8StringBufferOldDecoder();
                for (int k = 0; k < bytes.length; k++)
                    buffer.append(bytes[k]);
                assertEquals("ŸšŠ",buffer.toString());
            }
            long timeSpent = System.currentTimeMillis() - start;
            System.out.println("Old Umlauts: Time spent: " + timeSpent + "ms");
        }
    }
}
