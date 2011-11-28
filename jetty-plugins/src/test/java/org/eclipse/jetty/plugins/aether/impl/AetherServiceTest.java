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

package org.eclipse.jetty.plugins.aether.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;

import org.eclipse.jetty.plugins.aether.util.Booter;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/* ------------------------------------------------------------ */
/**
 */
public class AetherServiceTest
{
    private RepositorySystem _repoSystem = Booter.newRepositorySystem();
    private RepositorySystemSession _repoSession = Booter.newRepositorySystemSession(_repoSystem);

    AetherServiceImpl _aetherService = new AetherServiceImpl(_repoSystem,_repoSession);
    private static final String JTA_PLUGIN_ARTIFACT_ID = "jetty-plugin-jta";

    /* ------------------------------------------------------------ */
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        installArtifact(AetherServiceImpl.PLUGIN_LIST_ARTIFACT_ID,"/pom.xml");
        installArtifact(JTA_PLUGIN_ARTIFACT_ID,"/jta.jar");
        installArtifact("jetty-plugin-jmx","/jta.jar");
    }

    @Test
    public void testListAvailablePlugins() throws InstallationException, DependencyCollectionException, ArtifactResolutionException,
            DependencyResolutionException
    {
        List<String> availablePlugins = _aetherService.listAvailablePlugins();
        assertTrue(availablePlugins.size() == 2);
        assertTrue(availablePlugins.contains(JTA_PLUGIN_ARTIFACT_ID));
        assertTrue(availablePlugins.contains("jetty-plugin-jmx"));
    }

    @Test
    public void testGetPluginJar() throws IOException{
        JarFile plugin = new JarFile(new File(this.getClass().getResource("/jta.jar").getFile()));
        JarFile jarFile = _aetherService.getPluginJar(JTA_PLUGIN_ARTIFACT_ID);
        assertEquals(plugin.size(),jarFile.size());
    }

    private void installArtifact(String artifactId, String jarFile) throws InstallationException
    {
        Artifact artifact = new DefaultArtifact(AetherServiceImpl.GROUP_ID,artifactId,"jar","0.1-SNAPSHOT");
        artifact = artifact.setFile(new File(this.getClass().getResource(jarFile).getFile()));
        install(artifact);
    }

    private void install(Artifact artifact) throws InstallationException
    {
        InstallRequest installRequest = new InstallRequest();
        installRequest.addArtifact(artifact);
        _repoSystem.install(_repoSession,installRequest);
    }
}
