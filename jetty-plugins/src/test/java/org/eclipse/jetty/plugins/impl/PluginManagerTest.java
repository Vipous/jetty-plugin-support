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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.List;

import org.eclipse.jetty.plugins.MavenService;
import org.eclipse.jetty.plugins.model.AvailablePlugins;
import org.eclipse.jetty.plugins.model.Plugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/* ------------------------------------------------------------ */
/**
 */
@RunWith(MockitoJUnitRunner.class)
public class PluginManagerTest {
	@Mock
	private MavenService _mavenService;

	private PluginManagerImpl _pluginManager;

	private List<Plugin> availablePlugins = createAvailablePluginsTestData();
	private ClassLoader _classLoader = this.getClass().getClassLoader();
	private String _tmpDir;
	private File _javaTmpDir = new File(System.getProperty("java.io.tmpdir"));

	/* ------------------------------------------------------------ */
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		URL resource = this.getClass().getResource("/jetty_home");
		_tmpDir = resource.getFile();
		_pluginManager = new PluginManagerImpl(_mavenService, _tmpDir);
	}

	@Test
	public void testListAvailablePlugins() {
		when(_mavenService.listAvailablePlugins()).thenReturn(availablePlugins);
		List<Plugin> availablePlugins = _pluginManager.listAvailablePlugins();
		assertThat("jetty-jmx not found", availablePlugins.get(0).getName(),
				equalTo("jetty-jmx"));
		assertThat("jetty-jta not found", availablePlugins.get(1).getName(),
				equalTo("jetty-jta"));
	}

	@Test
	public void testInstallPlugins() throws IOException {

		String pluginName = "jetty-jmx";
		String jmxPluginJar = _classLoader.getResource(
				"jetty-jmx-7.6.0.v20120127.jar").getFile();
		String jmxPluginConfigJar = _classLoader.getResource(
				"jetty-jmx-7.6.0.v20120127-config.jar").getFile();
		File jmxPluginJarFile = new File(jmxPluginJar);
		File jmxPluginConfigJarFile = new File(jmxPluginConfigJar);

		// Need to copy it to a temp file since the implementation will move the
		// file and we need to keep the test files where they are.
		File jmxPluginTempCopy = copyToTempFile(jmxPluginJarFile);
		File jmxPluginConfigTempCopy = copyToTempFile(jmxPluginConfigJarFile);

		when(_mavenService.getPluginMetadata(pluginName)).thenReturn(
				createPluginTestData("jetty-jmx"));
		when(_mavenService.getPluginJar(pluginName)).thenReturn(
				jmxPluginTempCopy);
		when(_mavenService.getPluginConfigJar(pluginName)).thenReturn(
				jmxPluginConfigTempCopy);
		
		_pluginManager.installPlugin(pluginName);
		
		File metaInf = new File(_tmpDir
				+ File.separator + "META-INF");
		File jettyXmlConfigFile = new File(_tmpDir
				+ File.separator + "start.d" + File.separator
				+ "20-jetty-jmx.xml");
		File jettyJmxJarFile = new File(
				_tmpDir + File.separator + "lib" + File.separator
						+ "jetty-jmx-7.6.0.v20120127.jar");
		assertThat("META-INF should be skipped",metaInf.exists(), not(true));
		assertTrue("20-jetty-jmx.xml does not exist", jettyXmlConfigFile.exists());
		assertTrue("jetty-jmx-7.6.0.v20120127.jar does not exist", jettyJmxJarFile.exists());
	}

	public File copyToTempFile(File sourceFile) throws IOException {
		File destFile = new File(_javaTmpDir + File.separator
				+ sourceFile.getName());
		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
		return destFile;
	}

	private List<Plugin> createAvailablePluginsTestData() {
		AvailablePlugins availablePlugins = new AvailablePlugins();
		availablePlugins.addPlugin(createPluginTestData("jetty-jmx"));
		availablePlugins.addPlugin(createPluginTestData("jetty-jta"));
		return availablePlugins.getPlugins();
	}

	private Plugin createPluginTestData(String pluginName) {
		Plugin plugin = new Plugin();
		plugin.setName(pluginName);
		plugin.setInstallJar(true);
		plugin.setInstallConfigJar(true);
		return plugin;
	}

}
