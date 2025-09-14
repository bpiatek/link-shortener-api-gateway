package pl.bpiatek.linkshortenerapigateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "monitoring.user")
record MonitoringUserProperties(String name, String password) {
}