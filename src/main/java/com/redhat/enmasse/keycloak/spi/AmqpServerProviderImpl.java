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

import io.vertx.core.Vertx;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class AmqpServerProviderImpl implements AmqpServerProviderFactory
{
    private AmqpServer server;

    @Override
    public AmqpServerProviderFactory create(final KeycloakSession keycloakSession)
    {
        return this;
    }

    @Override
    public void init(final Config.Scope scope)
    {
        // TODO - get host/port from config
        server = new AmqpServer("localhost", 5677, scope);
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(server, stringAsyncResult ->  {
        });

    }

    @Override
    public void postInit(final KeycloakSessionFactory keycloakSessionFactory)
    {
        server.setKeycloakSessionFactory(keycloakSessionFactory);
    }

    @Override
    public void close()
    {
        server.stop();
    }

    @Override
    public String getId()
    {
        return "AmqpServerProviderImpl";
    }

    public static void main(String[] args)
    {
        new AmqpServerProviderImpl().init(null);
    }
}
