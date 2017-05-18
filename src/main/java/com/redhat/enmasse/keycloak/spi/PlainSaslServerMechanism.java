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

import java.nio.charset.StandardCharsets;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;

public class PlainSaslServerMechanism implements SaslServerMechanism {
    @Override
    public String getName() {
        return "PLAIN";
    }

    @Override
    public Instance newInstance(final KeycloakSession keycloakSession,
                                final String hostname,
                                final Config.Scope config)
    {
        return new Instance()
        {
            private boolean complete;
            private boolean authenticated;
            private RuntimeException error;

            @Override
            public byte[] processResponse(byte[] response) throws IllegalArgumentException
            {
                if(error != null) {
                    throw error;
                }

                int authzidNullPosition = findNullPosition(response, 0);
                if (authzidNullPosition < 0) {
                    error = new IllegalArgumentException("Invalid PLAIN encoding, authzid null terminator not found");
                    throw error;
                }

                int authcidNullPosition = findNullPosition(response, authzidNullPosition + 1);
                if (authcidNullPosition < 0) {
                    error = new IllegalArgumentException("Invalid PLAIN encoding, authcid null terminator not found");
                    throw error;
                }

                String username = new String(response, authzidNullPosition + 1, authcidNullPosition - authzidNullPosition - 1, StandardCharsets.UTF_8);
                int passwordLen = response.length - authcidNullPosition - 1;
                String password = new String(response, authcidNullPosition + 1, passwordLen, StandardCharsets.UTF_8);

                final RealmModel realm = keycloakSession.realms().getRealmByName(hostname);
                final UserModel user = keycloakSession.userStorageManager().getUserByUsername(username, realm);
                if(keycloakSession.userCredentialManager().isValid(realm,
                                                                   user,
                                                                   UserCredentialModel.password(password))) {

                    authenticated = true;
                    complete = true;
                    return null;
                } else {
                    authenticated = false;
                    complete = true;
                    return null;
                }
            }

            @Override
            public boolean isComplete()
            {
                return complete;
            }

            @Override
            public boolean isAuthenticated()
            {
                return authenticated;
            }

            private int findNullPosition(byte[] response, int startPosition) {
                int position = startPosition;
                while (position < response.length) {
                    if (response[position] == (byte) 0) {
                        return position;
                    }
                    position++;
                }
                return -1;
            }

        };
    }
}
