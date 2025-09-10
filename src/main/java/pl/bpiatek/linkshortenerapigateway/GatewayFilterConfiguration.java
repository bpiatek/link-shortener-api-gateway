package pl.bpiatek.linkshortenerapigateway;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Configuration
class GatewayFilterConfiguration {

    @Bean
    @Order(-1) // Run with high priority
    public Filter authenticationFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {

                // 1. Get the authenticated principal from Spring Security's context.
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                HttpServletRequest httpRequest = (HttpServletRequest) request;

                // 2. Check if the user is authenticated and the token is a JWT.
                if (authentication instanceof JwtAuthenticationToken token) {
                    var jwt = token.getToken();

                    // 3. Extract the claims.
                    var userId = jwt.getSubject();
                    var roles = jwt.getClaimAsStringList("roles");
                    var rolesHeaderValue = (roles != null) ? String.join(",", roles) : "";

                    // 4. Create a custom request wrapper to add the new headers.
                    //    We can't modify the original request.
                    HttpServletRequestWrapper enrichedRequest = new HttpServletRequestWrapper(httpRequest) {
                        @Override
                        public String getHeader(String name) {
                            if ("X-User-Id".equalsIgnoreCase(name)) {
                                return userId;
                            }
                            if ("X-User-Role".equalsIgnoreCase(name)) {
                                return rolesHeaderValue;
                            }
                            return super.getHeader(name);
                        }

                        @Override
                        public Enumeration<String> getHeaderNames() {
                            List<String> names = new ArrayList<>(Collections.list(super.getHeaderNames()));
                            names.add("X-User-Id");
                            names.add("X-User-Role");
                            return Collections.enumeration(names);
                        }

                        @Override
                        public Enumeration<String> getHeaders(String name) {
                            if ("X-User-Id".equalsIgnoreCase(name)) {
                                return Collections.enumeration(Collections.singletonList(userId));
                            }
                            if ("X-User-Role".equalsIgnoreCase(name)) {
                                return Collections.enumeration(Collections.singletonList(rolesHeaderValue));
                            }
                            return super.getHeaders(name);
                        }
                    };

                    // 5. Pass the NEW, enriched request down the filter chain.
                    chain.doFilter(enrichedRequest, response);

                } else {
                    // If not authenticated, pass the original request down the chain.
                    chain.doFilter(request, response);
                }
            }
        };
    }
}