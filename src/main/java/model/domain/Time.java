package model.domain;

import java.time.Duration;

public class Time implements java.io.Serializable {
    
    private static final long serialVersionUID = -2030169515375322789L;
    
    private Duration d;
    public static final Duration precision = Duration.ofMillis(10);
    
    public Time(int minutes, int seconds) {
        d = Duration.ofMinutes(minutes).plusSeconds(seconds);
    }
    
    public Time() {
        this(0, 0);
    }
    
    public Time(Time other) {
        this(other.d);
    }
    
    private Time(Duration d) {
        this.d = d;
    }
    
    public void add(Time timeAdded) {
        d = d.plus(timeAdded.d);
    }
    
    public void decrement() {
        d = d.minus(precision);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int minutes = (int) d.toMinutes();
        if (minutes < 10) {
            sb.append('0');
        }
        sb.append(minutes);
        sb.append(':');
        int seconds = (int) (d.getSeconds() % 60);
        if (seconds < 10) {
            sb.append('0');
        }
        sb.append(seconds);
        return sb.toString();
    }
}
