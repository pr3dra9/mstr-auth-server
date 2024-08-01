/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.bg.fon.mas.auth_server.config;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import rs.ac.bg.fon.mas.auth_server.model.Client;
import rs.ac.bg.fon.mas.auth_server.model.CustomUser;
import rs.ac.bg.fon.mas.auth_server.repository.ClientRepository;
import rs.ac.bg.fon.mas.auth_server.repository.UserRepository;

/**
 *
 * @author Predrag
 */
@Configuration
public class SecurityConfig {

    private final ClientRepository clientRepository;
    private final UserRepository userRepo;

    public SecurityConfig(ClientRepository clientRepository, UserRepository userRepo) {
        this.clientRepository = clientRepository;
        this.userRepo = userRepo;
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        return new RegisteredClientRepository() {
            @Override
            public RegisteredClient findById(String id) {
                Optional<Client> client = clientRepository.findById(Long.valueOf(id));
                return client.map(this::toRegisteredClient).orElse(null);
            }

            @Override
            public RegisteredClient findByClientId(String clientId) {
                Optional<Client> client = clientRepository.findByClientId(clientId);
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

    @Bean
    public UserDetailsService userService(UserRepository repo) {
        return (username) -> {
            CustomUser user = repo.findByUsername(username);
            return asUser(user);
        };
    }

    private UserDetails asUser(CustomUser user) {
        if (user == null) {
            return null;
        }

        Set<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        return User.withDefaultPasswordEncoder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(grantedAuthorities)
                .build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

}
