package pl.bpiatek.linkshortenerapigateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VirtualThreadCheckConfig {

    private static final Logger log = LoggerFactory.getLogger(VirtualThreadCheckConfig.class);

    @Bean
    CommandLineRunner virtualThreadCheckRunner() {
        return args -> {
            Thread t = Thread.ofVirtual().unstarted(() -> {});
            boolean virtualThreadsEnabled = t.isVirtual();

            log.info("--------------------------------------------------");
            log.info("Virtual Threads enabled: {}", virtualThreadsEnabled);
            log.info("Example thread: {}", Thread.currentThread());
            log.info("--------------------------------------------------");
        };
    }
}
