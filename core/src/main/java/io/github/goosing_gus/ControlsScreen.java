package io.github.goosing_gus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ControlsScreen extends ScreenAdapter {
    private final Main game;
    private final Batch batch;
    private final BitmapFont font;
    private final Viewport viewport = new ScreenViewport();
    private final GlyphLayout layout = new GlyphLayout();

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

        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new GameScreen(game));
            dispose();
            return;
        }
        ScreenUtils.clear(Color.BLACK);

        float centerX = viewport.getWorldWidth() / 2;
        float y = viewport.getWorldHeight() / 2 + 100;
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        layout.setText(font, "Goosing Gus");
        font.draw(batch, layout, centerX - layout.width / 2, y);
        y -= 50;

        layout.setText(font, "WASD - Move");
        font.draw(batch, layout, centerX - layout.width / 2, y);
        y -= 50;

        layout.setText(font, "Press ENTER to start");
        font.draw(batch, layout, centerX - layout.width / 2, y);
        y -= 50;

        batch.end();
    }
}
