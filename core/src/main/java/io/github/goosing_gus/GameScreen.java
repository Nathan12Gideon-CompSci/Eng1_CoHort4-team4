package io.github.goosing_gus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends ScreenAdapter {
    private static final float WORLD_WIDTH = 16f;
    private static final float WORLD_HEIGHT = 9f;
    private static final float ROUND_SECONDS = 300f;   // 5 minutes of play time
    private static final int TOTAL_EVENTS = 2;         // hidden events in Assessment 1 (5 in Assessment 2)

    // HUD style: dark fill, beige border, dark outline
    private static final float HUD_SCALE = 1.5f;
    private static final float TIMER_SCALE = 5f;
    private static final float TIMER_ICON_HEIGHT = 130f;
    private static final float OUTLINE = 3f;
    private static final float BORDER = 4f;
    private static final float PAD = 8f;
    private static final float MARGIN = 16f;
    private static final Color OUTLINE_COLOR = new Color(0.10f, 0.07f, 0.04f, 1f);
    private static final Color BORDER_COLOR = new Color(0.78f, 0.68f, 0.48f, 1f);
    private static final Color FILL_COLOR = new Color(0.13f, 0.12f, 0.14f, 1f);

    // 14 x 15 pixel-art stopwatch: o = outline, g = gold, w = face, h = hand, . = empty
    private static final String[] WATCH_ART = {
        ".....oooo.....",
        ".....oggo.....",
        "......oo......",
        "....oooooo....",
        "..ooggggggoo..",
        ".oggwwwwwwggo.",
        ".ogwwwwwwwwgo.",
        "ogwwwwwhwwwwgo",
        "ogwwwwwhwwwwgo",
        "ogwwwwwhhhwwgo",
        "ogwwwwwwwwwwgo",
        ".ogwwwwwwwwgo.",
        ".oggwwwwwwggo.",
        "..ooggggggoo..",
        "....oooooo...."
    };

    private final Batch batch;
    private final Texture bgdTexture = new Texture(Gdx.files.internal("bgd2.jpg"));
    private final Texture playerTexture = new Texture(Gdx.files.internal("player.png"));
    private final Texture pixelTexture = createPixelTexture();   // 1x1 white, tinted when drawn
    private final Texture watchTexture = createWatchTexture();
    private final Viewport gameViewport =  new ExtendViewport(16f, 9f);
    private final Vector2 inputMovement = new Vector2();

    // --- timer and pause ---
    private final GameTimer timer = new GameTimer(ROUND_SECONDS);
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final Viewport uiViewport = new ScreenViewport();   // pixel-based, for text
    private boolean paused = true;                              // every round starts paused

    // --- quit button ---
    private final Rectangle quitButton = new Rectangle();
    private final Vector2 mouse = new Vector2();

    // --- hidden events ---
    private final PitEvent pit = new PitEvent(3f, 2f, 1.5f, 1.5f, pixelTexture);
    private final LakeEvent lake = new LakeEvent(9f, 5f, 4f, 2.5f, pixelTexture);
    private int eventsTriggered = 0;

    private final Player player = new Player(
        WORLD_WIDTH / 2, WORLD_HEIGHT / 2,
        gameViewport,
        playerTexture);


    public GameScreen(Main game) {

        this.batch = game.getBatch();

        font.getData().setScale(HUD_SCALE);
    }

    private static Texture createPixelTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    // Builds the stopwatch icon from WATCH_ART, no image file needed
    private static Texture createWatchTexture() {
        int rows = WATCH_ART.length;
        int cols = WATCH_ART[0].length();
        Pixmap pixmap = new Pixmap(cols, rows, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                char c = WATCH_ART[y].charAt(x);
                switch (c) {
                    case 'o': pixmap.setColor(0.16f, 0.10f, 0.05f, 1f); break;
                    case 'g': pixmap.setColor(0.92f, 0.66f, 0.16f, 1f); break;
                    case 'w': pixmap.setColor(0.98f, 0.95f, 0.85f, 1f); break;
                    case 'h': pixmap.setColor(0.16f, 0.10f, 0.05f, 1f); break;
                    default: continue;
                }
                pixmap.drawPixel(x, y);
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();
        return texture;
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
        pit.reset();
        lake.reset();
        eventsTriggered = 0;
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

    // Quit: click the Quit button any time, or press Q while paused / after the round ended
    private void handleQuit() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q) && !isPlaying()) {
            Gdx.app.exit();
        }
        if (Gdx.input.justTouched()) {
            mouse.set(Gdx.input.getX(), Gdx.input.getY());
            uiViewport.unproject(mouse);
            if (quitButton.contains(mouse)) {
                Gdx.app.exit();
            }
        }
    }

    private void processInput() {
        inputMovement.setZero();
        if (!pit.isTrapped()) {                  // cannot move while stuck in the pit
            if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
                inputMovement.y += 1;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                inputMovement.y -= 1;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                inputMovement.x -= 1;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                inputMovement.x += 1;
            }
        }
        //normalise movement
        inputMovement.nor();
        player.changeDirection(inputMovement);
    }

    @Override
    public void render(float delta) {
        handlePauseKey();
        handleQuit();
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
        lake.draw(batch);
        pit.draw(batch);
        player.draw(batch);
        batch.end();

        // HUD on top
        drawUi();
    }

    private void updateLogic(float deltaTime) {
        timer.update(deltaTime);          // only counts down while running
        if (isPlaying()) {
            player.update(deltaTime);     // player and geese freeze when paused
            updateEvents();
        }
    }

    private void updateEvents() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            pit.pressJump();
        }
        if (pit.checkTrigger(player)) {
            eventsTriggered++;
        }
        if (lake.checkTrigger(player)) {
            eventsTriggered++;
        }
        lake.applyTo(player);
    }

    private void drawUi() {
        uiViewport.apply();
        batch.setProjectionMatrix(uiViewport.getCamera().combined);
        batch.begin();

        float w = uiViewport.getWorldWidth();
        float h = uiViewport.getWorldHeight();

        // top-left: large stopwatch timer
        font.getData().setScale(TIMER_SCALE);
        layout.setText(font, "00:00");
        float timerTextW = layout.width;
        float timerTextH = layout.height;
        float timerPad = PAD * 2;
        float timerBoxH = timerTextH + 2 * (OUTLINE + BORDER + timerPad);
        float iconH = TIMER_ICON_HEIGHT;
        float iconW = iconH * 14f / 15f;
        float iconY = h - MARGIN - iconH;
        float timerBoxX = MARGIN + iconW * 0.55f;
        float timerBoxY = iconY + (iconH - timerBoxH) / 2f;
        float textX = MARGIN + iconW + PAD;
        float timerBoxW = (textX - timerBoxX) + timerTextW + OUTLINE + BORDER + timerPad;
        drawBox(timerBoxX, timerBoxY, timerBoxW, timerBoxH);
        font.setColor(Color.WHITE);
        font.draw(batch, timer.getDisplay(), textX, timerBoxY + timerBoxH - OUTLINE - BORDER - timerPad);
        font.getData().setScale(HUD_SCALE);
        batch.setColor(Color.WHITE);
        batch.draw(watchTexture, MARGIN, iconY, iconW, iconH);

        // under it: hidden events left
        float boxH = boxHeight();
        int eventsLeft = Math.max(0, TOTAL_EVENTS - eventsTriggered);
        float eventsY = iconY - 10f - boxH;
        drawLabelBox("Hidden events left: " + eventsLeft, MARGIN, eventsY,
            boxWidth("Hidden events left: 9"), boxH);

        // top-right: quit button
        float quitW = boxWidth("Quit");
        quitButton.set(w - MARGIN - quitW, h - MARGIN - boxH, quitW, boxH);
        drawLabelBox("Quit", quitButton.x, quitButton.y, quitButton.width, quitButton.height);


        // centre messages
        if (timer.isFinished()) {
            drawCentered("TIME'S UP!  Press Q to quit", w, h);
        } else if (paused) {
            drawCentered("PAUSED - ESC to play, Q to quit", w, h);
        } else if (pit.isTrapped()) {
            drawCentered("You fell into a pit! Press SPACE to jump out (" + pit.getJumpsLeft() + " left)", w, h);
        }
        batch.end();
    }

    private float textWidth(String text) {
        layout.setText(font, text);
        return layout.width;
    }

    private float boxWidth(String measureText) {
        return textWidth(measureText) + 2 * (OUTLINE + BORDER + PAD);
    }

    private float boxHeight() {
        layout.setText(font, "00:00");
        return layout.height + 2 * (OUTLINE + BORDER + PAD);
    }

    // Dark box with beige border and dark outline; (x, y) is the bottom-left corner
    private void drawBox(float x, float y, float w, float h) {
        batch.setColor(OUTLINE_COLOR);
        batch.draw(pixelTexture, x, y, w, h);
        batch.setColor(BORDER_COLOR);
        batch.draw(pixelTexture, x + OUTLINE, y + OUTLINE, w - 2 * OUTLINE, h - 2 * OUTLINE);
        batch.setColor(FILL_COLOR);
        batch.draw(pixelTexture, x + OUTLINE + BORDER, y + OUTLINE + BORDER,
            w - 2 * (OUTLINE + BORDER), h - 2 * (OUTLINE + BORDER));
        batch.setColor(Color.WHITE);
    }

    private void drawLabelBox(String text, float x, float y, float w, float h) {
        drawBox(x, y, w, h);
        font.setColor(Color.WHITE);
        font.draw(batch, text, x + OUTLINE + BORDER + PAD, y + h - OUTLINE - BORDER - PAD);
    }

    private void drawCentered(String text, float w, float h) {
        layout.setText(font, text);
        font.draw(batch, text, (w - layout.width) / 2, (h + layout.height) / 2);
    }

    // Stretched over the whole world, so there is no repeated strip at the edge
    private void drawBackground() {
        batch.draw(bgdTexture, 0, 0, gameViewport.getWorldWidth(), gameViewport.getWorldHeight());
    }

    public void dispose() {
        bgdTexture.dispose();
        playerTexture.dispose();
        pixelTexture.dispose();
        watchTexture.dispose();
        font.dispose();
    }
}
