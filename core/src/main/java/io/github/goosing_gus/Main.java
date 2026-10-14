package io.github.goosing_gus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.Color;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {

    SpriteBatch spriteBatch;
    FitViewport viewport;

    Texture redSquareTexture;

    Sprite redSquareSprite;

    @Override
    public void create() {
        setScreen(new FirstScreen());

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        redSquareTexture = new Texture("redSquare.png");

        redSquareSprite = new Sprite(redSquareTexture); // Initialize the sprite based on the texture
        redSquareSprite.setSize(1, 1);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // true centers the camera
    }

    @Override
    public void render() {
        // organize code into three methods
        input();
        logic();
        draw();
    }

    private void input() {
        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            redSquareSprite.translateX(-speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            redSquareSprite.translateX(speed * delta);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            redSquareSprite.translateY(speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            redSquareSprite.translateY(-speed * delta);
        }
    }

    private void logic() {

    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        redSquareSprite.draw(spriteBatch);

        spriteBatch.end();
    }
}

