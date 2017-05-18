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
import io.vertx.proton.ProtonServer;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;


public class AmqpServer extends AbstractVerticle {

    private static final Logger LOG = Logger.getLogger(AmqpServer.class);

    private final String hostname;
    private final int port;
    private final Config.Scope config;
    private volatile ProtonServer server;
    private KeycloakSessionFactory keycloakSessionFactory;

    public AmqpServer(String hostname, int port, final Config.Scope config) {
        this.hostname = hostname;
        this.port = port;
        this.config = config;
    }

    private void connectHandler(ProtonConnection connection) {
        connection.setContainer("keycloak-sasl");
        connection.openHandler(conn -> {
        }).closeHandler(conn -> {
            connection.close();
            connection.disconnect();
        }).disconnectHandler(protonConnection -> {
            connection.disconnect();
        }).open();

    }

    @Override
    public void start() {
        server = ProtonServer.create(vertx);
        server.saslAuthenticatorFactory(() -> new SaslAuthenticator(keycloakSessionFactory, config));
        server.connectHandler(this::connectHandler);
        LOG.info("Starting server on "+hostname+":"+ port);
        server.listen(port, hostname, event -> {
            if(event.failed())
            {
                LOG.error("Unable to listen for AMQP on "+hostname+":" + port, event.cause());
            }

        });
    }

    @Override
    public void stop() {
        if (server != null) {
            server.close();
        }
    }

    void setKeycloakSessionFactory(final KeycloakSessionFactory keycloakSessionFactory)
    {
        this.keycloakSessionFactory = keycloakSessionFactory;
    }
}
