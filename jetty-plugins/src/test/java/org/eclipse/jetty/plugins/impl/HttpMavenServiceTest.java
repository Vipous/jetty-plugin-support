package org.eclipse.jetty.plugins.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.eclipse.jetty.plugins.model.Plugin;
import org.junit.Before;
import org.junit.Test;

/**
 * This is currently more an integration test downloading real stuff from real
 * maven repositories. Actually it's preferred to have a real unit test or at
 * least a local repository server. But since HttpClient.send(exchange) has an
 * api which is really hard to mock, I will leave that excercise for later.
 * 
 * However this tests should be disabled for the general build and ci.
 * 
 * @author tbecker
 * 
 */
public class HttpMavenServiceTest {
	private HttpMavenServiceImpl _mavenService = new HttpMavenServiceImpl();

	private static final String JETTY_JMX_PLUGIN_NAME = "jetty-jmx";

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testListAvailablePlugins() {
	}

	@Test
	public void testGetPluginJar() throws IOException {
		Plugin plugin = _mavenService.getPluginMetadata(JETTY_JMX_PLUGIN_NAME);
		File pluginJar = _mavenService.getPluginJar(plugin);
		assertThat(pluginJar, is(not(nullValue())));
	}

	@Test
	public void testGetConfigJar() throws IOException {
		Plugin plugin = _mavenService.getPluginMetadata(JETTY_JMX_PLUGIN_NAME);
		File configJar = _mavenService.getPluginConfigJar(plugin);
		assertThat(configJar, is(not(nullValue())));
	}

}
