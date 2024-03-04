package ee.buerokratt.cronmanager.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "application")
@EnableConfigurationProperties
public class ApplicationProperties {
    private String configPath;

    String allowedOrigins;

    Map<String, String> shellEnvironment;

    String appRootPath;


}
