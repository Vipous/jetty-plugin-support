// ========================================================================
// Copyright (c) 2009-2009 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terns of the Eclipse Public License v1.0
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
    private static final String SOURCE = "abcd012345somemoretextblablablabla\n\r\u0000\u00a4\u10fb\ufffdjetty";
    private static final String CHINESE_SOURCE =  "\u4E1A\u4E1B\u4E24\u4E69";
    private byte[] longTextBytes;
    private byte[] chineseTextBytes;

    @Before
    public void setup() throws UnsupportedEncodingException
    {
        bytes[0] = (byte)0xC3;
        bytes[1] = (byte)0xBC;
        bytes[2] = (byte)0xC3;
        bytes[3] = (byte)0xB6;
        bytes[4] = (byte)0xC3;
        bytes[5] = (byte)0xA4;

        longTextBytes = SOURCE.getBytes(StringUtil.__UTF8);
        chineseTextBytes = CHINESE_SOURCE.getBytes(StringUtil.__UTF8);
    }

    @Test
    public void testLongText() throws Exception
    {
        for (int i = 0; i < 16; i++)
        {
            long start = System.nanoTime();
            for (int j = 0; j < 3000000; j++)
            {
                sendLongText();
            }
            long timeSpent = System.nanoTime() - start;
            System.out.println("Long New: Time spent: " + timeSpent / 1000000 + "ms " + timeSpent + "ns");
        }
    }
    
    private void sendLongText() throws UnsupportedEncodingException
    {
        Utf8StringBuilder buffer = new Utf8StringBuilder();
        for (int k = 0; k < longTextBytes.length; k++)
            buffer.append(longTextBytes[k]);
        assertEquals(SOURCE,buffer.toString());
    }

    @Test
    public void testLongTextOldDecoder() throws Exception
    {
        for (int i = 0; i < 16; i++)
        {
            long start = System.nanoTime();
            for (int j = 0; j < 3000000; j++)
            {
                sendLongTextOldDecoder();
            }
            long timeSpent = System.nanoTime() - start;
            System.out.println("Long Old: Time spent: " + timeSpent / 1000000 + "ms " + timeSpent + "ns");
        }
    }

    private void sendLongTextOldDecoder() throws UnsupportedEncodingException
    {
        Utf8StringBuilderOldDecoder buffer = new Utf8StringBuilderOldDecoder();
        for (int k = 0; k < longTextBytes.length; k++)
            buffer.append(longTextBytes[k]);
        assertEquals(SOURCE,buffer.toString());
    }
    
    @Test
    public void testChineseText() throws Exception
    {
        for (int i = 0; i < 16; i++)
        {
            long start = System.nanoTime();
            for (int j = 0; j < 3000000; j++)
            {
                sendChineseText();
            }
            long timeSpent = System.nanoTime() - start;
            System.out.println("Chinese New: Time spent: " + timeSpent / 1000000 + "ms " + timeSpent + "ns");
        }
    }
    
    private void sendChineseText() throws UnsupportedEncodingException
    {
        Utf8StringBuilder buffer = new Utf8StringBuilder();
        for (int k = 0; k < chineseTextBytes.length; k++)
            buffer.append(chineseTextBytes[k]);
        assertEquals(CHINESE_SOURCE,buffer.toString());
    }
    
    @Test
    public void testChineseTextOldDecoder() throws Exception
    {
        for (int i = 0; i < 16; i++)
        {
            long start = System.nanoTime();
            for (int j = 0; j < 3000000; j++)
            {
                sendChineseTextOldDecoder();
            }
            long timeSpent = System.nanoTime() - start;
            System.out.println("Chinese Old: Time spent: " + timeSpent / 1000000 + "ms " + timeSpent + "ns");
        }
    }
    
    private void sendChineseTextOldDecoder() throws UnsupportedEncodingException
    {
        Utf8StringBuilderOldDecoder buffer = new Utf8StringBuilderOldDecoder();
        for (int k = 0; k < chineseTextBytes.length; k++)
            buffer.append(chineseTextBytes[k]);
        assertEquals(CHINESE_SOURCE,buffer.toString());
    }

    @Test
    public void testGermanUmlautsBenchmark() throws Exception
    {
        for (int i = 0; i < 16; i++)
        {
            long start = System.nanoTime();
            for (int j = 0; j < 3000000; j++)
            {
                Utf8StringBuilder buffer = new Utf8StringBuilder();
                for (int k = 0; k < bytes.length; k++)
                    buffer.append(bytes[k]);
                assertEquals("ŸšŠ",buffer.toString());
            }
            long timeSpent = System.nanoTime() - start;
            System.out.println("Umlauts New: Time spent: " + timeSpent / 1000000 + "ms " + timeSpent + "ns");
        }
    }

    @Test
    public void testGermanUmlautsBenchmarkOldDecoder() throws Exception
    {
        for (int i = 0; i < 16; i++)
        {
            long start = System.nanoTime();
            for (int j = 0; j < 3000000; j++)
            {
                Utf8StringBuilderOldDecoder buffer = new Utf8StringBuilderOldDecoder();
                for (int k = 0; k < bytes.length; k++)
                    buffer.append(bytes[k]);
                assertEquals("ŸšŠ",buffer.toString());
            }
            long timeSpent = System.nanoTime() - start;
            System.out.println("Old Umlauts: Time spent: " + timeSpent / 1000000 + "ms " + timeSpent + "ns");
        }
    }
}
