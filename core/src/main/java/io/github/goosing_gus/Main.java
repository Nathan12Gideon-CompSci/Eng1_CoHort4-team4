package io.github.goosing_gus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.Color;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {

    SpriteBatch spriteBatch;
    FitViewport viewport;
    private BitmapFont font;
    private Batch batch;


    Texture redSquareTexture;

    Sprite redSquareSprite;

    @Override
    public void create() {
        batch = new SpriteBatch();
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("PixelifySans-VariableFont_wght.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParams = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParams.size = 24;
        fontParams.color = Color.WHITE;
        font = generator.generateFont(fontParams);
        generator.dispose();


        redSquareTexture = new Texture("redSquare.png");

        redSquareSprite = new Sprite(redSquareTexture); // Initialize the sprite based on the texture
        redSquareSprite.setSize(1, 1);

        setScreen(new ControlsScreen(this));


    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // true centers the camera
    }

//    @Override
//    public void render() {
//        // organize code into three methods
//        input();
//        logic();
//        draw();
//    }
//
//    private void input() {
//        float speed = 4f;
//        float delta = Gdx.graphics.getDeltaTime();
//
//        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
//            redSquareSprite.translateX(-speed * delta);
//        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
//            redSquareSprite.translateX(speed * delta);
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
//            redSquareSprite.translateY(speed * delta);
//        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
//            redSquareSprite.translateY(-speed * delta);
//        }
//    }
//
//    private void logic() {
//
//    }
//
//    private void draw() {
//        ScreenUtils.clear(Color.BLACK);
//        viewport.apply();
//        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
//        spriteBatch.begin();
//
//        redSquareSprite.draw(spriteBatch);
//
//        spriteBatch.end();
//
//    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        batch.dispose();
        super.dispose();
        font.dispose();
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public Batch getBatch() {
        return batch;
    }

    public BitmapFont getFont() {
        return font;
    }


}

