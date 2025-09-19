package pl.bpiatek.linkshortenerapigateway;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableConfigurationProperties(MonitoringUserProperties.class)
@Configuration
class SecurityConfig {

    @Bean
    public UserDetailsService inMemoryUserDetailsManager(
            MonitoringUserProperties properties,
            PasswordEncoder passwordEncoder
    ) {
        var monitoringUser = User.builder()
                .username(properties.name())
                .password(passwordEncoder.encode(properties.password()))
                .roles("MONITORING")
                .build();
        return new InMemoryUserDetailsManager(monitoringUser);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/users/auth/register",
                                "/users/auth/login",
                                "/users/auth/refresh",
                                "/users/.well-known/jwks.json",
                                "/links"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));


        return http.build();
    }
}
