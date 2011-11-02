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

package org.eclipse.jetty.client;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ConnectHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/* ------------------------------------------------------------ */
/**
 * This UnitTest class executes two tests. Both will send a http request to https://google.com through a misbehaving proxy server.
 *
 * The first test runs against a proxy which simply closes the connection (as nginx does) for a connect request.
 * The second proxy server always responds with a 500 error.
 *
 * The expected result for both tests is an exception and the HttpExchange should have status HttpExchange.STATUS_EXCEPTED.
 */
public class HttpsViaBrokenHttpProxyTest
{

    private Server proxyClosingConnectionOnConnectRequest = new Server();
    private Server proxyResponding500Error = new Server();
    private HttpClient client = new HttpClient();

    /* ------------------------------------------------------------ */
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUpBeforeClass() throws Exception
    {
        // setup proxies with different behaviour
        proxyClosingConnectionOnConnectRequest.addConnector(new SelectChannelConnector());
        proxyClosingConnectionOnConnectRequest.setHandler(new ConnectHandlerClosingConnection());
        proxyClosingConnectionOnConnectRequest.start();
        int proxyClosingConnectionPort = proxyClosingConnectionOnConnectRequest.getConnectors()[0].getLocalPort();

        proxyResponding500Error.addConnector(new SelectChannelConnector());
        proxyResponding500Error.setHandler(new ConnectHandlerResponding500Error());
        proxyResponding500Error.start();

        client.setProxy(new Address("localhost",proxyClosingConnectionPort));
        client.start();
    }

    /* ------------------------------------------------------------ */
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDownAfterClass() throws Exception
    {
        client.stop();
        proxyClosingConnectionOnConnectRequest.stop();
    }

    @Test
    public void httpsViaProxyThatClosesConnectionOnConnectRequestTest()
    {
        sendRequestThroughProxy();
    }

    private void sendRequestThroughProxy()
    {
        String url = "https://google.com";
        try
        {
            ContentExchange exchange = new ContentExchange();
            exchange.setURL(url);
            client.send(exchange);
            exchange.waitForDone();
            assertEquals("Excepted status awaited",HttpExchange.STATUS_EXCEPTED,exchange.getStatus());
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    private class ConnectHandlerClosingConnection extends ConnectHandler
    {
        @Override
        protected void handleConnect(Request baseRequest, HttpServletRequest request, HttpServletResponse response, String serverAddress)
                throws ServletException, IOException
        {
            HttpConnection.getCurrentConnection().getEndPoint().close();
        }
    }

    @Test
    public void httpsViaProxyThatReturns500ErrorTest() throws Exception
    {
        int proxyPort = proxyResponding500Error.getConnectors()[0].getLocalPort();
        client.setProxy(new Address("localhost",proxyPort));
        sendRequestThroughProxy();
    }

    private class ConnectHandlerResponding500Error extends ConnectHandler
    {
        @Override
        protected void handleConnect(Request baseRequest, HttpServletRequest request, HttpServletResponse response, String serverAddress)
                throws ServletException, IOException
        {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
    }
}
