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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.jetty.plugins.MavenService;
import org.eclipse.jetty.plugins.PluginManager;
import org.eclipse.jetty.plugins.model.Plugin;

/* ------------------------------------------------------------ */
/**
 */
public class PluginManagerImpl implements PluginManager {
	private String _jettyHome;
	private MavenService _mavenService;

	private static List<String> excludes = Arrays.asList("META-INF");

	public PluginManagerImpl(MavenService mavenService, String jettyHome) {
		this._mavenService = mavenService;
		this._jettyHome = jettyHome;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @see org.eclipse.jetty.plugins.PluginManager#listAvailablePlugins()
	 */
	public List<Plugin> listAvailablePlugins() {
		return _mavenService.listAvailablePlugins();
	}

	/* ------------------------------------------------------------ */
	/**
	 * @see org.eclipse.jetty.plugins.PluginManager#installPlugin(String)
	 */
	public void installPlugin(String pluginName) {
		Plugin plugin = _mavenService.getPluginMetadata(pluginName);
		if (plugin.isInstallJar())
			installJar(plugin);
		if (plugin.isInstallConfigJar())
			installConfig(plugin);
		if (plugin.isInstallPluginJar())
			installPluginJar(plugin);
		if (plugin.isInstallWar())
			installWar(plugin);
	}

	private void installJar(Plugin plugin) {
		File file = _mavenService.getJar(plugin);
		String libDir = _jettyHome + File.separator + "lib" + File.separator;
		createDirectory(libDir);
		file.renameTo(new File(libDir, file.getName()));
	}

	private void installConfig(Plugin plugin) {
		try {
			JarFile file = new JarFile(_mavenService.getPluginConfigJar(plugin));
			extractJar(file);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private void installPluginJar(Plugin plugin) {
		try {
			JarFile file = new JarFile(_mavenService.getPluginJar(plugin));
			extractJar(file);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private void installWar(Plugin plugin) {
		File file = _mavenService.getPluginWar(plugin);
		String webappDir = _jettyHome + File.separator + "webapps"
				+ File.separator;
		createDirectory(webappDir);
		file.renameTo(new File(webappDir, file.getName()));
	}

	private void createDirectory(String directory) {
		new File(directory).mkdir();
	}

	private void extractJar(JarFile file) {
		Enumeration<JarEntry> entries = file.entries();
		while (entries.hasMoreElements())
			extractFileFromJar(file, entries.nextElement());
	}

	private void extractFileFromJar(JarFile jarFile, JarEntry jarEntry) {
		for (String exclude : excludes)
			if (jarEntry.getName().startsWith(exclude))
				return;

		File f = new File(_jettyHome + File.separator + jarEntry.getName());
		if (jarEntry.isDirectory()) { // if its a directory, create it
			f.mkdir();
			return;
		}
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			is = jarFile.getInputStream(jarEntry);
			fos = new FileOutputStream(f);
			while (is.available() > 0) {
				fos.write(is.read());
			}
		} catch (IOException e) {
			throw new IllegalStateException(
					"IOException while extracting plugin jar: ", e);
		} finally {
			try {
				fos.close();
				is.close();
			} catch (IOException e) {
				throw new IllegalStateException(
						"Couldn't close InputStream or FileOutputStream. This might be a file leak!",
						e);
			}
		}
	}
}
