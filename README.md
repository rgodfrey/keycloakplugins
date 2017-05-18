Keycloak AMQP Plugin
====================

This plugin is tested to work against Keycloak 3.0 Final

Building
--------

Ensure you have JDK 8 (or newer), Maven 3.1.1 (or newer) and Git installed

    java -version
    mvn -version
    git --version
    
First clone the repository:
    
    git clone https://github.com/rgodfrey/keycloakplugins.git
    cd keycloakplugins
    
To build run:

    mvn package
    
You will then need to install the plugin into your Keycloak providers directory

    cp target/keycloak-plugins-1.0-SNAPSHOT.jar ${KEYCLOAK_HOME}/providers
    
To configure the plugin you will want to add something like the following to your keycloak XML configuration (e.g. ${KEYCLOAK_HOME)/standalone/configuration/standalone.xml)

    <spi name="amqp">
        <provider name="amqp-server" enabled="true">
            <properties>
                <property name="host" value="localhost"/>
                <property name="port" value="5677"/>
                <property name="defaultDomain" value="enmasse"/>
            </properties>
      </provider>
    </spi>

within the keycloak subsystem definition (e.g.just below where &lt;spi name="publicKeyStorage"&gt; is configured)
    
Then you can run keycloak from ${KEYCLOAK_HOME}, e.g.

    bin/standalone.sh

Note that the plugin will take the host from the sasl-init frame as the name of the keycloak domain you wish to authenticate against.
