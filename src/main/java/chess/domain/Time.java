package chess.domain;

import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.Duration;

public class Time implements Serializable, Cloneable {
    
    private Duration timeRemaining;
    public static final Duration precision = Duration.ofMillis(10);
    
    public Time(int minutes, int seconds) {
        timeRemaining = Duration.ofMinutes(minutes).plusSeconds(seconds);
    }
    
    public Time() {
        this(0, 0);
    }
    
    public Time(Time other) {
        this(other.timeRemaining);
    }
    
    private Time(Duration duration) {
        this.timeRemaining = duration;
    }
    
    private Time(int hours, int minutes, int seconds) {
        this(60 * hours + minutes, seconds);
    }
    
    public static Time fromString(String s) {
        if (StringUtils.isEmpty(s)) {
            throw new NumberFormatException("Empty string");
        }
        
        int[] timeComponents = {0, 0, 0};
        
        String[] numbers = s.replaceAll("\\s+", "").split(":");
        int len = numbers.length;
        if (len > 3) {
            throw new NumberFormatException("Invalid time format. Use at most three numbers separated with colon");
        }
        
        for (int i = 3 - len; i < 3; ++i) {
            timeComponents[i] = Integer.parseInt(numbers[i + len - 3]);
            if (timeComponents[i] < 0) {
                throw new NumberFormatException("Values must be non-negative");
            }
            if (i > 0 && timeComponents[i] > 60) {
                throw new NumberFormatException("Number of minutes and seconds must be at most 60");
            }
        }
        
        return new Time(timeComponents[0], timeComponents[1], timeComponents[2]);
    }
    
    public void add(Time timeAdded) {
        timeRemaining = timeRemaining.plus(timeAdded.timeRemaining);
    }
    
    public void decrement() {
        timeRemaining = timeRemaining.minus(precision);
    }
    
    @Override
    public String toString() {
        int hours = (int) timeRemaining.toHours();
        int minutes = (int) timeRemaining.toMinutes() % 60;
        int seconds = (int) (timeRemaining.getSeconds() % 60);
        StringBuilder sb = new StringBuilder();
        
        sb.append(hours);
        sb.append(':');
        
        if (minutes < 10) {
            sb.append('0');
        }
        sb.append(minutes);
        sb.append(':');
        
        if (seconds < 10) {
            sb.append('0');
        }
        sb.append(seconds);
        
        return sb.toString();
    }
    
    @Override
    public Time clone() {
        try {
            return (Time) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
