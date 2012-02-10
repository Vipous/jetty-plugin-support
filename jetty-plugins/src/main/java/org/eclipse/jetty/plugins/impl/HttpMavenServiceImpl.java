package org.eclipse.jetty.plugins.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
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

	private HttpClient _httpClient = new HttpClient();
	private JettyPluginListXpp3Reader _xpp3Reader = new JettyPluginListXpp3Reader();

	public HttpMavenServiceImpl() {
		_httpClient.setTimeout(20000);
		_httpClient.setConnectTimeout(2000);
		try {
			_httpClient.start();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

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
		ContentExchange httpExchange = new ContentExchange();
		httpExchange.setURL(_pluginsXmlUrl);
		try {
			_httpClient.send(httpExchange);
			httpExchange.waitForDone();
			byte[] responseBytes = httpExchange.getResponseContentBytes();
			InputStream is = new ByteArrayInputStream(responseBytes);
			return _xpp3Reader.read(is).getPlugins();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
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

	private File getFile(String url) {
		ContentExchange exchange = new ContentExchange();
		exchange.setURL(url);
		String fileName = url.substring(url.lastIndexOf("/") + 1);
		try {
			_httpClient.send(exchange);
			exchange.waitForDone();
			if (exchange.getResponseStatus() != HttpStatus.OK_200)
				throw new IllegalStateException("HttpStatus code: "
						+ exchange.getResponseStatus() + " for url: " + url);
			byte[] responseBytes = exchange.getResponseContentBytes();
			if (responseBytes == null)
				throw new IllegalStateException("File: " + url + " is empty");
			File tempFile = new File(System.getProperty("java.io.tmpdir"),
					fileName);
			FileOutputStream fos = new FileOutputStream(tempFile);
			fos.write(responseBytes);
			return tempFile;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
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

	void setHttpClient(HttpClient httpClient) {
		this._httpClient = httpClient;
	}

}
