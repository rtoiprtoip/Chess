package chess.config;

import chess.domain.Time;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {
    
    @Bean
    public Time defaultGameTime() {
        return Time.fromString("10:00");
    }
    
    @Bean
    public Time defaultTimeAddedPerMove() {
        return Time.fromString("0:00");
    }
    
}
