package org.jetty.plugin.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.codehaus.myproject.model.AvailablePlugins;
import org.codehaus.myproject.model.Plugin;
import org.codehaus.myproject.model.io.xpp3.JettyPluginListXpp3Writer;
import org.junit.Ignore;
import org.junit.Test;

public class GeneratePluginListTest {

	JettyPluginListXpp3Writer xpp3Writer = new JettyPluginListXpp3Writer();

	/**
	 * I use it to create an example xml file for the jetty-plugin-model. Do not
	 * enable it as it will create an xml file if you don't need that. :)
	 * 
	 * @throws IOException
	 */
	@Test
	@Ignore
	public void createListTest() throws IOException {
		AvailablePlugins availablePlugins = new AvailablePlugins();

		availablePlugins.addPlugin(createPlugin("jetty-jmx", "eclipse"));
		availablePlugins.addPlugin(createPlugin("jetty-jta", "eclipse"));

		File file = new File("src/main/resources/plugins.xml");
		FileOutputStream fos = new FileOutputStream(file);
		xpp3Writer.write(fos, availablePlugins);
	}

	private Plugin createPlugin(String name, String license) {
		Plugin plugin = new Plugin();
		plugin.setName(name);
		plugin.setLicense(license);
		return plugin;
	}
}
