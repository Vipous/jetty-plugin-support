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

import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.eclipse.jetty.plugins.aether.util.Booter;
import org.eclipse.jetty.plugins.aether.util.ConsoleRepositoryListener;
import org.eclipse.jetty.plugins.aether.util.ConsoleTransferListener;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.SubArtifact;

/* ------------------------------------------------------------ */
/**
 */
public class AetherServiceTest
{
    private static final String JTA_PLUGIN_ARTIFACT_ID = "jetty-plugin-jta";

    private AetherServiceImpl _aetherService = new AetherServiceImpl();

    /* ------------------------------------------------------------ */
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        _aetherService._repoSession = newTestRepositorySystemSession(Booter.newRepositorySystem());
        installArtifact(AetherServiceImpl.PLUGIN_LIST_ARTIFACT_ID,"/jta.jar","/pom.xml");
        installArtifact(JTA_PLUGIN_ARTIFACT_ID,"/jta.jar");
        installArtifact("jetty-plugin-jmx","/jta.jar");
    }

    @Test
    public void testListAvailablePlugins() throws InstallationException, DependencyCollectionException, ArtifactResolutionException,
            DependencyResolutionException
    {
        List<String> availablePlugins = _aetherService.listAvailablePlugins();
        assertTrue("Expected to retrieve 2 plugins",availablePlugins.size() == 2);
        assertTrue(JTA_PLUGIN_ARTIFACT_ID + " missing",availablePlugins.contains(JTA_PLUGIN_ARTIFACT_ID));
        assertTrue("jetty-plugin-jmx missing", availablePlugins.contains("jetty-plugin-jmx"));
    }

    @Test
    public void testGetPluginJar() throws IOException{
        JarFile plugin = new JarFile(new File(this.getClass().getResource("/jta.jar").getFile()));
        JarFile jarFile = _aetherService.getPluginJar(JTA_PLUGIN_ARTIFACT_ID);
        assertEquals(plugin.size(),jarFile.size());
    }

    private void installArtifact(String artifactId, String jarFile, String pomFile) throws InstallationException{
        Artifact artifact = createArtifact(artifactId,jarFile);
        Artifact pom = new SubArtifact(artifact,null,"pom");
        String file = this.getClass().getResource(pomFile).getFile();
        File file2 = new File(file);
        pom = pom.setFile(file2);
        install(artifact, pom);
    }

    private void installArtifact(String artifactId, String jarFile) throws InstallationException
    {
        Artifact artifact = createArtifact(artifactId,jarFile);
        install(artifact, null);
    }

    private Artifact createArtifact(String artifactId, String jarFile)
    {
        Artifact artifact = new DefaultArtifact(AetherServiceImpl.GROUP_ID,artifactId,"jar","1.0");
        artifact = artifact.setFile(new File(this.getClass().getResource(jarFile).getFile()));
        return artifact;
    }

    private void install(Artifact artifact, Artifact pom) throws InstallationException
    {
        InstallRequest installRequest = new InstallRequest();
        installRequest.addArtifact(artifact).addArtifact(pom);
//        if(pom!=null)
//            installRequest.addArtifact(pom);
        _aetherService._repoSystem.install(_aetherService._repoSession,installRequest);
    }

    private RepositorySystemSession newTestRepositorySystemSession(RepositorySystem system)
    {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();

        LocalRepository localRepo = new LocalRepository("target/local-repo");
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));
        session.setTransferListener(new ConsoleTransferListener());
        session.setRepositoryListener(new ConsoleRepositoryListener());

        // uncomment to generate dirty trees
        // session.setDependencyGraphTransformer( null );

        return session;
    }

}
