package io.github.goosing_gus;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

/** Hidden event: the lake is revealed when the player touches it; walking in it slows the player down. */
public class LakeEvent extends GameObject {

    private static final float SLOW_FACTOR = 0.5f;   // 50% speed (SR_LAKESLOW)

    private boolean triggered = false;

    public LakeEvent(float x, float y, float w, float h, Texture pixel) {
        super(x, y, w, h, pixel);
    }

    /** Returns true only the first time the player touches the lake (for the event counter). */
    public boolean checkTrigger(Player player) {
        if (triggered || !overlaps(player)) return false;
        triggered = true;
        return true;
    }

    /** Slow the player inside the lake, normal speed outside (SR_LAKEEXIT). */
    public void applyTo(Player player) {
        player.setSpeedMultiplier(overlaps(player) ? SLOW_FACTOR : 1f);
    }

    public void reset() { triggered = false; }

    @Override
    void update(float deltaTime) { }

    @Override
    public void draw(Batch batch) {
        if (!triggered || texture == null) return;   // hidden until triggered
        batch.setColor(0.15f, 0.4f, 0.8f, 1f);       // blue water
        batch.draw(texture, rect.x, rect.y, rect.width, rect.height);
        batch.setColor(1f, 1f, 1f, 1f);
    }
}
