package controller.domain;

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
    
    public void add(Time timeAdded) {
        timeRemaining = timeRemaining.plus(timeAdded.timeRemaining);
    }
    
    public void decrement() {
        timeRemaining = timeRemaining.minus(precision);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int minutes = (int) timeRemaining.toMinutes();
        if (minutes < 10) {
            sb.append('0');
        }
        sb.append(minutes);
        sb.append(':');
        int seconds = (int) (timeRemaining.getSeconds() % 60);
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
