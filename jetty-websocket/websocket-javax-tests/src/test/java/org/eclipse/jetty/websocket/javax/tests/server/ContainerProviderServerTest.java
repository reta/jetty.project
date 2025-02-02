//
// ========================================================================
// Copyright (c) 1995 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v. 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package org.eclipse.jetty.websocket.javax.tests.server;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.websocket.server.ServerEndpoint;

import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.eclipse.jetty.websocket.javax.tests.EventSocket;
import org.eclipse.jetty.websocket.javax.tests.WSServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static javax.websocket.CloseReason.CloseCodes.NORMAL_CLOSURE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContainerProviderServerTest
{
    @ServerEndpoint("/echo")
    public static class MySocket
    {
        @OnOpen
        public void onOpen()
        {
            WebSocketContainer client = ContainerProvider.getWebSocketContainer();
            assertNotNull(client);
        }
    }

    private WSServer server;

    @BeforeEach
    public void startServer() throws Exception
    {
        Path testdir = MavenTestingUtils.getTargetTestingPath(ContainerProviderServerTest.class.getName());
        server = new WSServer(testdir);
        WSServer.WebApp app = server.createWebApp("app");
        app.createWebInf();
        app.copyClass(MySocket.class);
        app.deploy();

        server.start();
    }

    @AfterEach
    public void stopServer() throws Exception
    {
        server.stop();
    }

    @Test
    public void testJavaxWsContainerInServer() throws Exception
    {
        WebSocketContainer client = ContainerProvider.getWebSocketContainer();
        EventSocket clientSocket = new EventSocket();
        Session session = client.connectToServer(clientSocket, server.getWsUri().resolve("/app/echo"));
        session.close(new CloseReason(NORMAL_CLOSURE, null));
        assertTrue(clientSocket.closeLatch.await(5, TimeUnit.SECONDS));
        assertThat(clientSocket.closeReason.getCloseCode(), is(NORMAL_CLOSURE));
        assertNull(clientSocket.error);
    }
}
