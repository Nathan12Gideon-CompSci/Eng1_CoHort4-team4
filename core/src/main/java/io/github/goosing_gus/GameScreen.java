package io.github.goosing_gus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends ScreenAdapter {
    private static final float WORLD_WIDTH = 16f;
    private static final float WORLD_HEIGHT = 9f;
    private static final float ROUND_SECONDS = 300f;   // 5 minutes of play time

    private final Batch batch;
    private final Texture bgdTexture = new Texture(Gdx.files.internal("bgd2.jpg"));
    private final Texture playerTexture = new Texture(Gdx.files.internal("player.png"));
    private final Viewport gameViewport =  new ExtendViewport(16f, 9f);
    private final Vector2 inputMovement = new Vector2();

    // --- timer and pause (new) ---
    private final GameTimer timer = new GameTimer(ROUND_SECONDS);
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final Viewport uiViewport = new ScreenViewport();   // pixel-based, for text
    private boolean paused = true;                              // every round starts paused

    private final Player player = new Player(
        WORLD_WIDTH / 2, WORLD_HEIGHT / 2,
        gameViewport,
        playerTexture);


    public GameScreen(Main game) {

        this.batch = game.getBatch();

        bgdTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        font.getData().setScale(2f);
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        uiViewport.update(width, height, true);
    }

    @Override
    public void show() {
        resetGame();
    }

    private void resetGame() {
        player.reset(WORLD_WIDTH / 2, WORLD_HEIGHT / 2);
        timer.reset();
        paused = true;
    }

    // true only while the round is running (not paused, not finished)
    private boolean isPlaying() {
        return !paused && !timer.isFinished();
    }

    private void handlePauseKey() {
        if (timer.isFinished()) return;          // after the round ended, ESC does nothing
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            if (paused) {
                timer.pause();
            } else {
                timer.start();
            }
        }
    }

    private void processInput() {
        inputMovement.setZero();
        if(Gdx.input.isKeyPressed(Input.Keys.W)) {
            inputMovement.y += 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)) {
            inputMovement.y -= 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A)) {
            inputMovement.x -= 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
            inputMovement.x += 1;
        }
        //normalise movement
        inputMovement.nor();
        player.changeDirection(inputMovement);
    }

    @Override
    public void render(float delta) {
        handlePauseKey();
        if (isPlaying()) {
            processInput();
        }
        updateLogic(delta);

        ScreenUtils.clear(Color.GRAY);

        // game world
        gameViewport.apply();
        batch.setProjectionMatrix(gameViewport.getCamera().combined);
        batch.begin();
        drawBackground();
        player.draw(batch);
        batch.end();

        // text on top (timer, pause, time up)
        drawUi();
    }

    private void updateLogic(float deltaTime) {
        Gdx.graphics.getDeltaTime();
        timer.update(deltaTime);          // only counts down while running
        if (isPlaying()) {
            player.update(deltaTime);     // player and geese freeze when paused
        }
    }

    private void drawUi() {
        uiViewport.apply();
        batch.setProjectionMatrix(uiViewport.getCamera().combined);
        batch.begin();

        float w = uiViewport.getWorldWidth();
        float h = uiViewport.getWorldHeight();

        font.draw(batch, timer.getDisplay(), 20, h - 20);

        if (timer.isFinished()) {
            drawCentered("TIME'S UP!", w, h);
        } else if (paused) {
            drawCentered("PAUSED - press ESC to play", w, h);
        }
        batch.end();
    }

    private void drawCentered(String text, float w, float h) {
        layout.setText(font, text);
        font.draw(batch, text, (w - layout.width) / 2, (h + layout.height) / 2);
    }

    private void drawBackground() {
        float u2 = gameViewport.getWorldWidth() / WORLD_WIDTH;
        float v2 = gameViewport.getWorldHeight() / WORLD_HEIGHT;

        batch.draw(bgdTexture,
            0, 0,
            gameViewport.getWorldWidth(), gameViewport.getWorldHeight(),
            0, 0,
            u2, v2);
    }

    public void dispose() {
        bgdTexture.dispose();
        playerTexture.dispose();
        font.dispose();
    }
}
