package ee.buerokratt.cronmanager.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Optional;

@Configuration
public class CORSConfiguration {

    private String[] allowedOrigins;

    @Autowired
    public CORSConfiguration(@Value("${application.allowedOrigins}") List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins.toArray(new String[0]);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(allowedOrigins);
            }
        };
    }
}
