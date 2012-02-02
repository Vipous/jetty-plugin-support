package org.eclipse.jetty.plugins.aether.util;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.sonatype.aether.AbstractRepositoryListener;
import org.sonatype.aether.RepositoryEvent;

/**
 * A simplistic repository listener that logs events to a jetty Logger.
 */
public class LoggerRepositoryListener
    extends AbstractRepositoryListener
{
	private static final Logger LOG = Log.getLogger(LoggerRepositoryListener.class);

    @Override
    public void artifactDeployed( RepositoryEvent event )
    {
        LOG.debug( "Deployed " + event.getArtifact() + " to " + event.getRepository() );
    }

    @Override
    public void artifactDeploying( RepositoryEvent event )
    {
        LOG.debug( "Deploying " + event.getArtifact() + " to " + event.getRepository() );
    }

    @Override
    public void artifactDescriptorInvalid( RepositoryEvent event )
    {
        LOG.debug( "Invalid artifact descriptor for " + event.getArtifact() + ": "
            + event.getException().getMessage() );
    }

    @Override
    public void artifactDescriptorMissing( RepositoryEvent event )
    {
        LOG.debug( "Missing artifact descriptor for " + event.getArtifact() );
    }

    @Override
    public void artifactInstalled( RepositoryEvent event )
    {
        LOG.debug( "Installed " + event.getArtifact() + " to " + event.getFile() );
    }

    @Override
    public void artifactInstalling( RepositoryEvent event )
    {
        LOG.debug( "Installing " + event.getArtifact() + " to " + event.getFile() );
    }

    @Override
    public void artifactResolved( RepositoryEvent event )
    {
        LOG.debug( "Resolved artifact " + event.getArtifact() + " from " + event.getRepository() );
    }

    @Override
    public void artifactDownloading( RepositoryEvent event )
    {
        LOG.debug( "Downloading artifact " + event.getArtifact() + " from " + event.getRepository() );
    }

    @Override
    public void artifactDownloaded( RepositoryEvent event )
    {
        LOG.debug( "Downloaded artifact " + event.getArtifact() + " from " + event.getRepository() );
    }

    @Override
    public void artifactResolving( RepositoryEvent event )
    {
        LOG.debug( "Resolving artifact " + event.getArtifact() );
    }

    @Override
    public void metadataDeployed( RepositoryEvent event )
    {
        LOG.debug( "Deployed " + event.getMetadata() + " to " + event.getRepository() );
    }

    @Override
    public void metadataDeploying( RepositoryEvent event )
    {
        LOG.debug( "Deploying " + event.getMetadata() + " to " + event.getRepository() );
    }

    @Override
    public void metadataInstalled( RepositoryEvent event )
    {
        LOG.debug( "Installed " + event.getMetadata() + " to " + event.getFile() );
    }

    @Override
    public void metadataInstalling( RepositoryEvent event )
    {
        LOG.debug( "Installing " + event.getMetadata() + " to " + event.getFile() );
    }

    @Override
    public void metadataInvalid( RepositoryEvent event )
    {
        LOG.debug( "Invalid metadata " + event.getMetadata() );
    }

    @Override
    public void metadataResolved( RepositoryEvent event )
    {
        LOG.debug( "Resolved metadata " + event.getMetadata() + " from " + event.getRepository() );
    }

    @Override
    public void metadataResolving( RepositoryEvent event )
    {
        LOG.debug( "Resolving metadata " + event.getMetadata() + " from " + event.getRepository() );
    }

}
