package config;

import controller.domain.Time;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {
    
    @Bean
    public Time defaultGameTime() {
        return new Time(10, 0);
    }
    
    @Bean
    public Time defaultTimeAddedPerMove() {
        return new Time(0, 0);
    }
    
}
