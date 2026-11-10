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


        setScreen(new GameScreen(this));


    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // true centers the camera
    }


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

