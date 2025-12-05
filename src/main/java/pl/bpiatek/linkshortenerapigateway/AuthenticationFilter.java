package pl.bpiatek.linkshortenerapigateway;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@Order(-1)
class AuthenticationFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var httpRequest = (HttpServletRequest) request;

        if (authentication instanceof JwtAuthenticationToken token) {
            var jwt = token.getToken();
            var userId = jwt.getSubject();
            var roles = jwt.getClaimAsStringList("roles");
            var rolesHeaderValue = (roles != null) ? String.join(",", roles) : "";

            log.info("Authenticating user [{}] with roles [{}]", userId, roles);
            var enrichedRequest = new HttpServletRequestWrapper(httpRequest) {
                private final Map<String, String> customHeaders = Map.of(
                        "X-User-Id", userId,
                        "X-User-Role", rolesHeaderValue
                );

                @Override
                public String getHeader(String name) {
                    return customHeaders.getOrDefault(name, super.getHeader(name));
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    var names = new HashSet<>(customHeaders.keySet());
                    names.addAll(Collections.list(super.getHeaderNames()));
                    return Collections.enumeration(names);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if (customHeaders.containsKey(name)) {
                        return Collections.enumeration(Collections.singletonList(customHeaders.get(name)));
                    }
                    return super.getHeaders(name);
                }
            };

            chain.doFilter(enrichedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}