package pl.bpiatek.linkshortenerapigateway;

import org.springframework.beans.factory.annotation.Value;
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

@Configuration
class SecurityConfig {

    @Bean
    public UserDetailsService inMemoryUserDetailsManager(
            @Value("${monitoring.user.name}") String monitoringUsername,
            @Value("${monitoring.user.password}") String monitoringPassword,
            PasswordEncoder passwordEncoder
    ) {
        var monitoringUser = User.builder()
                .username(monitoringUsername)
                .password(passwordEncoder.encode(monitoringPassword))
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
                                "/user/auth/register",
                                "/user/auth/login",
                                "/user/auth/refresh",
                                "/user/.well-known/jwks.json"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));


        return http.build();
    }
}
