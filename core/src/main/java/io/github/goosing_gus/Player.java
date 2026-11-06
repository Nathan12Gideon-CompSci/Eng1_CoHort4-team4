package io.github.goosing_gus;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Player extends GameObject {

    private static final float SCALE = 1 / 320f;
    private static final float PATIENCE = 100f;
    private static final float SPEED = 4f;

    private final Viewport gameViewport;
    private float patience = PATIENCE;
    private final Vector2 moveDirection = new Vector2();

    public Player(float x, float y, Viewport gameViewport, Texture texture) {
        super(x, y, texture.getWidth() * SCALE, texture.getHeight() * SCALE, texture);
        this.gameViewport = gameViewport;
    }

    public void reset(float x, float y) {
        rect.setPosition(x, y);
        patience = PATIENCE;
    }
    @Override
    void update(float deltaTime) {
        move(deltaTime);
    }

    private void move(float deltaTime) {
        if(moveDirection.isZero()) return;

        float newX = rect.getX() + moveDirection.x * SPEED * deltaTime;
        float newY = rect.getY() + moveDirection.y * SPEED * deltaTime;

        newX = MathUtils.clamp(newX, 0, gameViewport.getWorldWidth() - rect.getWidth());
        newY = MathUtils.clamp(newY, 0, gameViewport.getWorldHeight() - rect.getHeight());

        rect.setPosition(newX, newY);
    }

    public void changeDirection(Vector2 direction) {
        moveDirection.set(direction);
    }
}
