package io.github.goosing_gus;

public class GameTimer {
    /**
     * Handles the logic for the timer in the corner of the screen.
     */
    private final float duration;
    private float remaining;
    private boolean running;

    public GameTimer(float durationSeconds) {
        this.duration = durationSeconds;
        this.remaining = durationSeconds;
        this.running = false;          // pause mode when game start
    }

    public void start() { running = true; }
    public void pause() { running = false; }
    public void reset() {
        remaining = duration;
        running = false;
    }

    public void update(float delta) {
        if (!running) return;
        remaining -= delta;
        if (remaining <= 0) {          // stop until 0, not making it negative
            remaining = 0;
            running = false;
        }
    }

    public boolean isFinished() { return remaining <= 0; }
    public float getRemaining() { return remaining; }

    public String getDisplay() {
        int s = (int) Math.ceil(remaining);
        return String.format("%02d:%02d", s / 60, s % 60);
    }
}
