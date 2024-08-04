/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.bg.fon.mas.auth_server.config;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import rs.ac.bg.fon.mas.auth_server.model.Client;
import rs.ac.bg.fon.mas.auth_server.repository.ClientRepository;

/**
 *
 * @author Predrag
 */
@Configuration
public class RegisteredClientConfig {

    private final ClientRepository clientRepository;
    private static final Logger logger = LoggerFactory.getLogger(RegisteredClientConfig.class);
    
    public RegisteredClientConfig(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }
    
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        return new RegisteredClientRepository() {
            @Override
            public RegisteredClient findById(String id) {
                Optional<Client> client = clientRepository.findById(Long.valueOf(id));
                logger.debug(client.get().toString());
                return client.map(this::toRegisteredClient).orElse(null);
            }

            @Override
            public RegisteredClient findByClientId(String clientId) {
                logger.debug("Client ID: " + clientId);
                Optional<Client> client = clientRepository.findByClientId(clientId);
                
                if (client.isEmpty())
                    return null;
                
                logger.debug("Client ID: " + client.get());
                return client.map(this::toRegisteredClient).orElse(null);
            }

            private RegisteredClient toRegisteredClient(Client client) {
                Set<String> scopesSet = client.getScopes();

                Set<AuthorizationGrantType> grantTypesSet = client.getAuthorizationGrantTypes().stream()
                        .map(AuthorizationGrantType::new) // Pretvara string u AuthorizationGrantType
                        .collect(Collectors.toSet()); // Skuplja u set

                ClientSettings clientSettings = ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .build();
                return RegisteredClient.withId(client.getId().toString())
                        .clientId(client.getClientId())
                        .clientSecret(client.getClientSecret())
                        .scopes(scopes -> scopes.addAll(scopesSet)) // Postavljanje više vrednosti za scope
                        .redirectUris(ru -> ru.addAll(client.getRedirectUris()))
                        .authorizationGrantTypes(grantTypes -> grantTypes.addAll(grantTypesSet))
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .clientSettings(clientSettings)
                        .build();
            }

            @Override
            public void save(RegisteredClient registeredClient) {
                Optional<Client> c = clientRepository.findByClientId(registeredClient.getClientId());
                if (c.isPresent()) {
                    return;
                }
                Client client = new Client();
                client.setClientId(registeredClient.getClientId());
                client.setClientSecret(registeredClient.getClientSecret());
                client.setClientName(registeredClient.getClientName());
                client.setScopes(registeredClient.getScopes());
                client.setRedirectUris(registeredClient.getRedirectUris());
                client.setAuthorizationGrantTypes(registeredClient.getAuthorizationGrantTypes().stream()
                        .map(AuthorizationGrantType::getValue).collect(Collectors.toSet()));
                clientRepository.save(client);
            }
        };
    }
    
}
