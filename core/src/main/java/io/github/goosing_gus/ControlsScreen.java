package io.github.goosing_gus;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ControlsScreen extends ScreenAdapter {
    private final Main game;
    private final Batch batch;
    private final BitmapFont font;
    private final Viewport viewport = new ScreenViewport();

    public ControlsScreen(Main game) {
        this.game = game;
        this.batch = game.getBatch();
        this.font = game.getFont();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        font.draw(batch, "Goosing Gus", 200f, 200f);

        batch.end();
    }
}
