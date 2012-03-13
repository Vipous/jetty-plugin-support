package org.eclipse.jetty.plugins.impl;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jetty.plugins.MavenService;
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
	private MavenService _mavenService = new HttpMavenServiceImpl();

	private static final String JETTY_JMX_PLUGIN_NAME = "jetty-jmx";

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testListAvailablePlugins() {
		List<String> pluginNames = _mavenService.listAvailablePlugins();
		assertThat(pluginNames.size(), greaterThan(1));
	}

	@Test
	public void testGetPluginJar() throws IOException {
		Plugin plugin = _mavenService.getPlugin(JETTY_JMX_PLUGIN_NAME);
		assertThat("jetty-jmx should contain a jar", plugin.getJar(),
				is(not(nullValue())));
		assertThat("jetty-jmx should contain a config-jar",
				plugin.getConfigJar(), is(notNullValue()));
		assertThat("jetty-jmx should not contain a war", plugin.getWar(),
				is(nullValue()));
	}

	@Test
	public void testGetConfigJar() throws IOException {
		Plugin plugin = _mavenService.getPlugin(JETTY_JMX_PLUGIN_NAME);
		File configJar = plugin.getConfigJar();
		assertThat(configJar, is(not(nullValue())));
	}

}
