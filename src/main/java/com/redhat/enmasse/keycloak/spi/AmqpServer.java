/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.redhat.enmasse.keycloak.spi;

import io.vertx.core.AbstractVerticle;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonSender;
import io.vertx.proton.ProtonServer;
import io.vertx.proton.ProtonSession;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;

import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;


public class AmqpServer extends AbstractVerticle {

    private final String hostname;
    private final int port;
    private volatile ProtonServer server;
    private KeycloakSessionFactory keycloakSessionFactory;

    public AmqpServer(String hostname, int port, final Config.Scope scope)
    {
        this.hostname = hostname;
        this.port = port;
    }

    private void connectHandler(ProtonConnection connection) {
        connection.setContainer("keycloak-sasl");
        connection.openHandler(conn -> {
            System.err.println("Connection opened");
        }).closeHandler(conn -> {
            connection.close();
            connection.disconnect();
            System.err.println("Connection closed");
        }).disconnectHandler(protonConnection -> {
            connection.disconnect();
            System.err.println("Disconnected");
        }).open();

        connection.sessionOpenHandler(ProtonSession::open);
        connection.senderOpenHandler(sender -> senderOpenHandler(connection, sender));
    }

    private void senderOpenHandler(ProtonConnection connection, ProtonSender sender) {
        sender.setSource(sender.getRemoteSource());
        Source source = (Source) sender.getRemoteSource();
        Target target = (Target) sender.getTarget();
        try {
            sender.open();
        } catch (Exception e) {
            e.printStackTrace();
            sender.close();
        }
    }

    @Override
    public void start() {
        server = ProtonServer.create(vertx);
        server.saslAuthenticatorFactory(() -> new SaslAuthenticator(keycloakSessionFactory));
        server.connectHandler(this::connectHandler);
        System.err.println("Starting server on "+hostname+":"+ port);
        server.listen(port, hostname, event -> {
            if(event.succeeded())
            {
                System.err.println("success!!!!");
            }
            else if(event.failed())
            {
                event.cause().printStackTrace();
            }
            else
            {
                System.err.println("??????!?!!?!?!?!");
            }
        });
    }

    @Override
    public void stop() {
        if (server != null) {
            server.close();
        }
    }

    public void setKeycloakSessionFactory(final KeycloakSessionFactory keycloakSessionFactory)
    {
        this.keycloakSessionFactory = keycloakSessionFactory;
    }
}
