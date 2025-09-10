package pl.bpiatek.linkshortenerapigateway;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
class GatewayFilterConfiguration {

    @Bean
    @Order(-1)
    public GlobalFilter authenticationFilter() {
        return (exchange, chain) -> {
            var enrichedExchangeMono = exchange.getPrincipal()
                    .map(principal -> {
                        if (principal instanceof JwtAuthenticationToken token) {
                            var jwt = token.getToken();

                            var userId = jwt.getSubject();
                            var roles = jwt.getClaimAsStringList("roles");
                            var rolesHeaderValue = (roles != null) ? String.join(",", roles) : "";

                            var enrichedRequest = exchange.getRequest().mutate()
                                    .header("X-User-Id", userId)
                                    .header("X-User-Role", rolesHeaderValue)
                                    .build();

                            return exchange.mutate().request(enrichedRequest).build();
                        }
                        return exchange;
                    })
                    .defaultIfEmpty(exchange);

            return enrichedExchangeMono.flatMap(chain::filter);
        };
    }
}