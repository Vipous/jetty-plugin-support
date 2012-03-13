package org.eclipse.jetty.plugins.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.plugins.MavenService;
import org.eclipse.jetty.plugins.model.Plugin;
import org.eclipse.jetty.plugins.util.RepositoryParser;
import org.eclipse.jetty.plugins.util.StreamUtils;

public class HttpMavenServiceImpl implements MavenService {
	private static final String REPOSITORY_URL = "http://repo2.maven.org/maven2/";
	private static final String GROUP_ID = "org/eclipse/jetty";
	private static final String VERSION = "7.6.0.v20120127"; // TODO: should be
																// automatically
																// set
	private String _repositoryUrl = REPOSITORY_URL;
	private String _groupId = GROUP_ID;
	private String _version = VERSION;

	public List<String> listAvailablePlugins() {
		List<String> availablePlugins = new ArrayList<String>();

		String moduleListing = fetchDirectoryListingOfJettyModules();
		List<String> modules = RepositoryParser
				.parseLinksInDirectoryListing(moduleListing);

		for (String module : modules) {
			String listing = fetchModuleDirectoryListing(module);
			if (RepositoryParser.isModuleAPlugin(listing)) {
				availablePlugins.add(module);
			}
		}
		System.out.println(availablePlugins);
		return availablePlugins;
	}

	private String fetchDirectoryListingOfJettyModules() {
		try {
			URL url = new URL(REPOSITORY_URL + GROUP_ID);
			URLConnection connection = url.openConnection();
			InputStream inputStream = connection.getInputStream();
			return StreamUtils.inputStreamToString(inputStream);
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private String fetchModuleDirectoryListing(String module) {
		try {
			URL configJar = new URL(getModuleDirectory(module));
			URLConnection connection = configJar.openConnection();
			InputStream inputStream = connection.getInputStream();
			return StreamUtils.inputStreamToString(inputStream);
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			// Honestly, I'm not a friend of ignoring exceptions as it might
			// hide something important. In this case however it "usually"
			// just means: THIS IS NOT A PLUGIN! However it still might hide
			// things. If that'll be the case, I hope I'm not the one who
			// has to debug my own code. ;)
			return "not a plugin";
		}
	}

	public Plugin getPlugin(String pluginName) {
		File configJar = getFile(getModulePrefix(pluginName) + "-config.jar");
		Plugin plugin = new Plugin(pluginName, configJar);
		plugin = appendJarToPlugin(pluginName, plugin);
		plugin = appendWarToPlugin(pluginName, plugin);
		return plugin;
	}

	private Plugin appendJarToPlugin(String pluginName, Plugin plugin) {
		try {
			File jar = getFile(getModulePrefix(pluginName) + ".jar");
			plugin.setJar(jar);
		} catch (IllegalStateException e) {
			System.out.println("No jar file found for " + pluginName
					+ " This is usually normal. Exception: " + e.getMessage());
		}
		return plugin;
	}

	private Plugin appendWarToPlugin(String pluginName, Plugin plugin) {
		try {
			File war = getFile(getModulePrefix(pluginName) + ".war");
			plugin.setWar(war);
		} catch (IllegalStateException e) {
			System.out.println("No war file found for " + pluginName
					+ " This is usually normal. Exception: " + e.getMessage());
		}
		return plugin;
	}

	private String getModuleDirectory(String pluginName) {
		return _repositoryUrl + _groupId + "/" + pluginName + "/" + _version
				+ "/";
	}

	private String getModulePrefix(String pluginName) {
		return getModuleDirectory(pluginName) + pluginName + "-" + _version;
	}

	private File getFile(String urlString) {
		String fileName = urlString.substring(urlString.lastIndexOf("/") + 1);
		try {
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
			InputStream inputStream = connection.getInputStream();
			File tempFile = new File(System.getProperty("java.io.tmpdir"),
					fileName);
			OutputStream out = new FileOutputStream(tempFile);
			byte buf[] = new byte[1024];
			int len;
			while ((len = inputStream.read(buf)) > 0)
				out.write(buf, 0, len);
			out.close();
			inputStream.close();
			return tempFile;
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public void setGroupId(String groupId) {
		this._groupId = groupId.replace(".", "/");
	}

	public void setRepositoryUrl(String repositoryUrl) {
		this._repositoryUrl = repositoryUrl;
	}

	public void setVersion(String version) {
		this._version = version;
	}

}
