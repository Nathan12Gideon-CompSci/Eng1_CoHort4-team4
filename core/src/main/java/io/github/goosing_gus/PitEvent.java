package io.github.goosing_gus;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

/** Hidden event: the pit is revealed when the player touches it; SPACE must be pressed 10 times to climb out. */
public class PitEvent extends GameObject {

    private static final int JUMPS_TO_ESCAPE = 10;

    private boolean triggered = false;   // revealed once touched (SR_EVENTHIDDEN)
    private boolean trapped = false;
    private int jumps = 0;

    public PitEvent(float x, float y, float w, float h, Texture pixel) {
        super(x, y, w, h, pixel);
    }

    /** Returns true only the first time the player touches the pit (SR_PITTRAP, SR_EVENTONCE). */
    public boolean checkTrigger(Player player) {
        if (triggered || !overlaps(player)) return false;
        triggered = true;
        trapped = true;
        jumps = 0;
        return true;
    }

    /** One SPACE press while in the pit (SR_PITESCAPE). */
    public void pressJump() {
        if (!trapped) return;
        jumps++;
        if (jumps >= JUMPS_TO_ESCAPE) {
            trapped = false;
        }
    }

    public boolean isTrapped() { return trapped; }

    public int getJumpsLeft() { return JUMPS_TO_ESCAPE - jumps; }

    public void reset() {
        triggered = false;
        trapped = false;
        jumps = 0;
    }

    @Override
    void update(float deltaTime) { }

    @Override
    public void draw(Batch batch) {
        if (!triggered || texture == null) return;   // hidden until triggered
        batch.setColor(0.25f, 0.14f, 0.06f, 1f);     // dark brown edge
        batch.draw(texture, rect.x, rect.y, rect.width, rect.height);
        batch.setColor(0.45f, 0.28f, 0.12f, 1f);     // brown dirt
        batch.draw(texture, rect.x + 0.1f, rect.y + 0.1f, rect.width - 0.2f, rect.height - 0.2f);
        batch.setColor(1f, 1f, 1f, 1f);
    }
}
