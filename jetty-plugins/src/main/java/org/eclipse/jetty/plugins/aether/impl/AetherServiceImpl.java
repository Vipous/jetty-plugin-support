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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;

import org.eclipse.jetty.plugins.aether.AetherService;
import org.eclipse.jetty.plugins.aether.util.Booter;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/* ------------------------------------------------------------ */
/**
 */
public class AetherServiceImpl implements AetherService
{
    private static final String VERSION = "1.0";
    static final String GROUP_ID = "org.mortbay.jetty";
    public static final String PLUGIN_LIST_ARTIFACT_ID = "jetty-plugin-list";

    RepositorySystem _repoSystem;
    RepositorySystemSession _repoSession;

    public AetherServiceImpl()
    {
        this._repoSystem = Booter.newRepositorySystem();
        this._repoSession = Booter.newRepositorySystemSession(_repoSystem);
    }

    public List<String> listAvailablePlugins()
    {
        Artifact artifact = new DefaultArtifact(GROUP_ID,PLUGIN_LIST_ARTIFACT_ID,"pom",VERSION);
        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(artifact);

        List<DependencyNode> dependencies = Collections.emptyList();
        try
        {
            Dependency dependency = new Dependency(artifact,"runtime");

            CollectRequest collectRequest = new CollectRequest();
            collectRequest.addRepository(Booter.newRemoteNexusRepository());
            collectRequest.addRepository(Booter.newCentralRepository());
            collectRequest.setRoot(dependency);

            DependencyRequest dependencyRequest = new DependencyRequest();
            dependencyRequest.setCollectRequest(collectRequest);

            DependencyNode rootNode = _repoSystem.resolveDependencies(_repoSession,dependencyRequest).getRoot();
            dependencies = rootNode.getChildren();
        }
        catch (DependencyResolutionException e)
        {
            throw new IllegalStateException("Couldn't resolve dependencies.",e);
        }

        List<String> pluginArtifactIds = extractArtifactIds(dependencies);
        return pluginArtifactIds;
    }

    private List<String> extractArtifactIds(List<DependencyNode> dependencies)
    {
        List<String> pluginArtifactIds = new ArrayList<String>();
        for (DependencyNode dependencyNode : dependencies)
        {
            pluginArtifactIds.add(dependencyNode.getDependency().getArtifact().getArtifactId());
        }
        return pluginArtifactIds;
    }

    public JarFile getPluginJar(String pluginName)
    {
        Artifact pluginArtifact = getArtifact(pluginName);
        try
        {
            return new JarFile(pluginArtifact.getFile());
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private Artifact getArtifact(String pluginName)
    {
        String coords = GROUP_ID + ":" + pluginName + ":" + VERSION;
        System.out.println("COORDS:" + coords);
        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(new DefaultArtifact(coords));
        artifactRequest.addRepository(Booter.newCentralRepository());
        artifactRequest.addRepository(Booter.newRemoteNexusRepository());
        ArtifactResult artifactResult;
        try
        {
            artifactResult = _repoSystem.resolveArtifact(_repoSession,artifactRequest);
        }
        catch (ArtifactResolutionException e)
        {
            throw new IllegalStateException(e);
        }
        return artifactResult.getArtifact();
    }

}
