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

package org.eclipse.jetty.plugins.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.IllegalSelectorException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.plugins.MavenService;
import org.eclipse.jetty.plugins.PluginManager;
import org.eclipse.jetty.plugins.model.AvailablePlugins;
import org.eclipse.jetty.plugins.model.Plugin;
import org.eclipse.jetty.plugins.model.io.xpp3.JettyPluginListXpp3Reader;

/* ------------------------------------------------------------ */
/**
 */
public class PluginManagerImpl implements PluginManager
{
    private String _jettyHome;

    private MavenService _aetherService;
    
    private JettyPluginListXpp3Reader _xpp3Reader = new JettyPluginListXpp3Reader();

    private HttpClient _httpClient = new HttpClient();
    
    public PluginManagerImpl(MavenService aetherService, String jettyHome)
    {
        this._aetherService = aetherService;
        this._jettyHome = jettyHome;
        _httpClient.setTimeout(20000);
        try {
			_httpClient.start();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
    }

    /* ------------------------------------------------------------ */
    /**
     * @see org.eclipse.jetty.plugins.PluginManager#listAvailablePlugins()
     */
    public List<String> listAvailablePlugins()
    {
    	AvailablePlugins availablePlugins = null;
    	ContentExchange httpExchange = new ContentExchange();
    	httpExchange.setURL("https://raw.github.com/jetty-project/jetty-plugin-support/master/jetty-plugin-model/src/main/resources/plugins.xml");
    	try {
			_httpClient.send(httpExchange);
			httpExchange.waitForDone();
			byte[] responseBytes = httpExchange.getResponseContentBytes();
			InputStream is = new ByteArrayInputStream(responseBytes);
			availablePlugins = _xpp3Reader.read(is);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		} catch (XmlPullParserException e) {
			throw new IllegalStateException(e);
		}
    	
    	List<String> pluginNames = new ArrayList<String>();
    	for (Plugin plugin : availablePlugins.getPlugins()) {
			pluginNames.add(plugin.getName());
		}
    	
        return pluginNames;
    }

    /* ------------------------------------------------------------ */
    /**
     * @see org.eclipse.jetty.plugins.PluginManager#installPlugin(String)
     */
    public void installPlugin(String pluginName)
    {
        JarFile jarFile = _aetherService.getPluginJar(pluginName);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements())
        {
            JarEntry jarEntry = entries.nextElement();
            extractFileFromJar(jarFile,jarEntry);
        }
    }

    private void extractFileFromJar(JarFile jarFile, JarEntry jarEntry)
    {
        File f = new File(_jettyHome + File.separator + jarEntry.getName());
        if (jarEntry.isDirectory())
        { // if its a directory, create it
            f.mkdir();
            return;
        }
        InputStream is = null;
        FileOutputStream fos = null;
        try
        {
            is = jarFile.getInputStream(jarEntry);
            fos = new FileOutputStream(f);
            while (is.available() > 0)
            {
                fos.write(is.read());
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("IOException while extracting plugin jar: ", e);
        }
        finally
        {
            try
            {
                fos.close();
                is.close();
            }
            catch (IOException e)
            {
                throw new IllegalStateException("Couldn't close InputStream or FileOutputStream. This might be a file leak!",e);
            }
        }
    }
    
    void setHttpClient(HttpClient _httpClient) {
		this._httpClient = _httpClient;
	}
}
