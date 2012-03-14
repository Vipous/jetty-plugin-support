package org.eclipse.jetty.plugins.impl;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
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
		_mavenService.setRepositoryUrl("http://gravity-design.de:8080/nexus/content/repositories/releases/");
	}

	@Test
	public void testListAvailablePlugins() {
		List<String> pluginNames = _mavenService.listAvailablePlugins();
		assertThat(pluginNames.size(), is(2));
	}

	@Test
	public void testGetPluginJar() throws IOException {
		Plugin plugin = _mavenService.getPlugin(JETTY_JMX_PLUGIN_NAME);
		assertThat("jetty-jmx should contain a plugin-jar",
				plugin.getPluginJar(), is(notNullValue()));
	}

	@Test
	public void testGetConfigJar() throws IOException {
		Plugin plugin = _mavenService.getPlugin(JETTY_JMX_PLUGIN_NAME);
		File configJar = plugin.getPluginJar();
		assertThat(configJar, is(not(nullValue())));
	}

}
