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
    UserDetailsService inMemoryUserDetailsManager(
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
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(2)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 1. Allow all traffic to the UI Service (ls.bpiatek.pl)
                        // The UI Service handles its own login/cookie logic.
                        .requestMatchers(request -> "ls.bpiatek.pl".equals(request.getServerName()))
                        .permitAll()

                        // 2. API Domain (apidev.bpiatek.pl)
                        // Public Auth Endpoints
                        .requestMatchers(
                                "/users/auth/register",
                                "/users/auth/login",
                                "/users/auth/logout",
                                "/users/auth/refresh",
                                "/users/auth/verify",
                                "/users/auth/forgot-password",
                                "/users/auth/reset-password",
                                "/users/.well-known/jwks.json"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));


        return http.build();
    }
}
