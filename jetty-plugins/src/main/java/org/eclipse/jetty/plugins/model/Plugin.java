package org.eclipse.jetty.plugins.model;

import java.io.File;

public class Plugin {
	private String name;

	private boolean isInstallJar = false;
	private boolean isInstallWar = false;

	private File jar;
	private File configJar;
	private File war;

	public Plugin(String name, File configJar) {
		this.name = name;
		this.configJar = configJar;
	}
	
	public String getName() {
		return name;
	}

	public File getConfigJar() {
		return configJar;
	}

	public File getJar() {
		return jar;
	}
	
	public void setJar(File jar) {
		this.isInstallJar = true;
		this.jar = jar;
	}

	public File getWar() {
		return war;
	}
	
	public void setWar(File war) {
		this.isInstallWar= true;
		this.war = war;
	}

	public boolean isInstallJar() {
		return isInstallJar;
	}

	public boolean isInstallWar() {
		return isInstallWar;
	}
}
