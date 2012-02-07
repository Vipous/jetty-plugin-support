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
		if(pluginName==null)
			throw new IllegalArgumentException("pluginName parameter null");
		
		List<Plugin> plugins = listAvailablePlugins();
		for (Plugin plugin : plugins) {
			if(pluginName.equals(plugin.getName()))
				return plugin;
		}
		
		throw new IllegalArgumentException("Unknown Plugin");
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

	public File getPluginJar(String pluginName) {
		String url = getPluginPrefix(pluginName) + ".jar";
		return getFile(url);
	}

	public File getPluginConfigJar(String pluginName) {
		String url = getPluginPrefix(pluginName) + "-config.jar";
		return getFile(url);
	}
	
	public File getPluginWar(String pluginName) {
		String url = getPluginPrefix(pluginName) + ".war";
		return getFile(url);
	}
	
	private String getPluginPrefix(String pluginName) {
		return _repositoryUrl + _groupId + "/" + pluginName + "/" + _version
				+ "/" + pluginName + "-" + _version;
	}
	
	private File getFile(String url){
		ContentExchange exchange = new ContentExchange();
		exchange.setURL(url);
		String fileName = url.substring(url.lastIndexOf("/") + 1);
		try {
			_httpClient.send(exchange);
			exchange.waitForDone();
			byte[] responseBytes = exchange.getResponseContentBytes();
			File tempFile = new File(System.getProperty("java.io.tmpdir"),fileName);
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
		this._groupId = groupId;
	}

	public void setRepositoryUrl(String repositoryUrl) {
		this._repositoryUrl = repositoryUrl;
	}

	public void setVersion(String version) {
		this._version = version;
	}

	void setHttpClient(HttpClient httpClient) {
		this._httpClient = httpClient;
	}

}
