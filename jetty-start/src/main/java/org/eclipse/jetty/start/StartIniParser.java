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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/* ------------------------------------------------------------ */
/**
 */
public class StartIniParser
{
    /**
     * If a start.ini is present in the CWD, then load it into the argument list.
     */
    public List<String> loadStartIni(String ini)
    {
        String jettyHome = System.getProperty("jetty.home");
        File startIniFile = ini == null?((jettyHome != null)?new File(jettyHome,"start.ini"):new File("start.ini")):new File(ini);
        if (!startIniFile.exists())
        {
            if (ini != null)
            {
                System.err.println("Warning - can't find ini file: " + ini);
            }
            // No start.ini found, skip load.
            return Collections.emptyList();
        }

        List<String> args = new ArrayList<String>();

        List<File> startIniFiles = new ArrayList<File>();
        startIniFiles.add(startIniFile);
        startIniFiles.addAll(getAdditionalStartIniFiles());

        for (File file : startIniFiles)
        {
            FileReader reader = null;
            BufferedReader buf = null;
            try
            {
                reader = new FileReader(file);
                buf = new BufferedReader(reader);

                String arg;
                while ((arg = buf.readLine()) != null)
                {
                    arg = arg.trim();
                    if (arg.length() == 0 || arg.startsWith("#"))
                    {
                        continue;
                    }
                    args.add(arg);
                }
            }
            catch (IOException e)
            {
                // usageExit(e,ERR_UNKNOWN);
            }
            finally
            {
                Main.close(buf);
                Main.close(reader);
            }
        }

        return args;
    }

    List<File> getAdditionalStartIniFiles()
    {
        String jettyHome = System.getProperty("jetty.home") == null?".":System.getProperty("jetty.home");
        File file = new File(jettyHome + "/start.d");
        if (file.exists())
            return Arrays.asList(file.listFiles(new iniExtensionFilter()));
        return Collections.emptyList();
    }

    private class iniExtensionFilter implements FilenameFilter
    {
        public boolean accept(File dir, String name)
        {
            return (name.endsWith(".ini"));
        }
    }
}
