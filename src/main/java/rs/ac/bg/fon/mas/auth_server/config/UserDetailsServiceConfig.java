/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.bg.fon.mas.auth_server.config;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import rs.ac.bg.fon.mas.auth_server.model.CustomUser;
import rs.ac.bg.fon.mas.auth_server.repository.UserRepository;

/**
 *
 * @author Predrag
 */
@Configuration
public class UserDetailsServiceConfig {
    
    private final UserRepository userRepo;

    public UserDetailsServiceConfig(UserRepository userRepo) {
        this.userRepo = userRepo;
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
    
}
