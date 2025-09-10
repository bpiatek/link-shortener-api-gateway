package pl.bpiatek.linkshortenerapigateway;

import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(-1)
public class AuthenticationWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    var authentication = securityContext.getAuthentication();

                    if (authentication instanceof JwtAuthenticationToken token) {
                        var jwt = token.getToken();
                        var userId = jwt.getSubject();
                        var roles = jwt.getClaimAsStringList("roles");
                        var rolesHeaderValue = (roles != null) ? String.join(",", roles) : "";

                        var mutatedRequest = exchange.getRequest().mutate()
                                .header("X-User-Id", userId)
                                .header("X-User-Role", rolesHeaderValue)
                                .build();

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }

                    return chain.filter(exchange);
                });
    }
}