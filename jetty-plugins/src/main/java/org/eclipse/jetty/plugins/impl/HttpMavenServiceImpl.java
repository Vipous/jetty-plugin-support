package org.eclipse.jetty.plugins.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.jetty.plugins.MavenService;
import org.eclipse.jetty.plugins.model.Plugin;
import org.eclipse.jetty.plugins.model.io.xpp3.JettyPluginListXpp3Reader;

public class HttpMavenServiceImpl implements MavenService {
	private static final String PLUGINS_XML_URL = "https://raw.github.com/jetty-project/jetty-plugin-support/master/jetty-plugin-model/src/main/resources/plugins.xml";
	private static final String REPOSITORY_URL = "http://repo2.maven.org/maven2/";
	private static final String GROUP_ID = "org/eclipse/jetty";
	private static final String VERSION = "7.6.0.v20120127"; // TODO: should be
																// automatically
																// set

	private String _pluginsXmlUrl = PLUGINS_XML_URL;
	private String _repositoryUrl = REPOSITORY_URL;
	private String _groupId = GROUP_ID;
	private String _version = VERSION;

	private JettyPluginListXpp3Reader _xpp3Reader = new JettyPluginListXpp3Reader();

	public Plugin getPluginMetadata(String pluginName) {
		if (pluginName == null)
			throw new IllegalArgumentException("pluginName parameter null");

		List<Plugin> plugins = listAvailablePlugins();
		for (Plugin plugin : plugins) {
			if (pluginName.equals(plugin.getName()))
				return plugin;
		}

		throw new IllegalArgumentException("Unknown Plugin: " + pluginName
				+ " not found in " + _pluginsXmlUrl);
	}

	public List<Plugin> listAvailablePlugins() {
		try {
			URL url = new URL(_pluginsXmlUrl);
			URLConnection connection = url.openConnection();
			return _xpp3Reader.read(connection.getInputStream()).getPlugins();
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} catch (XmlPullParserException e) {
			throw new IllegalStateException(e);
		}
	}

	public File getJar(Plugin plugin) {
		String url = getPluginPrefix(plugin) + ".jar";
		return getFile(url);
	}

	public File getPluginConfigJar(Plugin plugin) {
		String url = getPluginPrefix(plugin) + "-config.jar";
		return getFile(url);
	}

	public File getPluginJar(Plugin plugin) {
		String url = getPluginPrefix(plugin) + "-plugin.jar";
		return getFile(url);
	}

	public File getPluginWar(Plugin plugin) {
		String url = getPluginPrefix(plugin) + ".war";
		return getFile(url);
	}

	private String getPluginPrefix(Plugin plugin) {
		if (plugin.getRepositoryUrl() != null)
			setRepositoryUrl(plugin.getRepositoryUrl());
		if (plugin.getGroupId() != null)
			setGroupId(plugin.getGroupId());
		if (plugin.getVersion() != null)
			setVersion(plugin.getVersion());

		return _repositoryUrl + _groupId + "/" + plugin.getName() + "/"
				+ _version + "/" + plugin.getName() + "-" + _version;
	}

	private File getFile(String urlString) {
		String fileName = urlString.substring(urlString.lastIndexOf("/") + 1);
		URL url;
		try {
			url = new URL(urlString);
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

	public void setPluginsXmlUrl(String pluginsXmlUrl) {
		this._pluginsXmlUrl = pluginsXmlUrl;
	}

}
