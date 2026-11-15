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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Handles the game screen, which is launched when the game starts.
 */
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
    private static final Color SELECTED_FILL_COLOR = new Color(0.42f, 0.30f, 0.14f, 1f);

    // Pause menu style
    private static final float MENU_TEXT_SCALE = 2f;
    private static final float MENU_TITLE_SCALE = 3.5f;
    private static final float MENU_BUTTON_W = 380f;
    private static final float MENU_BUTTON_H = 64f;
    private static final float MENU_GAP = 18f;
    private static final int ACTION_CONTINUE = 0;
    private static final int ACTION_RESTART = 1;
    private static final int ACTION_QUIT = 2;

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
    private boolean started = false;                            // false until the player starts the first round

    // --- pause menu ---
    private final Rectangle[] menuRects = {new Rectangle(), new Rectangle(), new Rectangle()};
    private final Vector2 mouse = new Vector2();
    private int menuIndex = 0;

    // --- hidden events ---
    private final PitEvent pit = new PitEvent(3f, 2f, 2f, 2f, pixelTexture);
    private final LakeEvent lake = new LakeEvent(9f, 5f, 4f, 2.5f, pixelTexture);
    private int eventsTriggered = 0;

    // --- items ---
    private static final int ITEM_COUNT = 2;
    private int itemsUsed = 0;

    private Map<String, Integer> items = new HashMap<>();

    private float energyDrinkStartTime = 500f;
    private boolean energyDrinkInUse = false;
    private float energyDrinkDuration = 5f;

    // --- player ---
    private final Player player = new Player(
        WORLD_WIDTH / 2, WORLD_HEIGHT / 2,
        gameViewport,
        playerTexture);


    public GameScreen(Main game) {

        this.batch = game.getBatch();

        font.getData().setScale(HUD_SCALE);

        // add items to map
        items.put("Energy drink", 1);
        items.put("Stress ball", 1);
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
        itemsUsed = 0;
        items.replaceAll((k, v) -> 1);
        paused = true;
        menuIndex = 0;
    }

    // true only while the round is running (not paused, not finished)
    private boolean isPlaying() {
        return !paused && !timer.isFinished();
    }

    // the menu is shown before the first round, while paused, and after the round ended
    private boolean isMenuVisible() {
        return paused || timer.isFinished();
    }

    private void resumeGame() {
        paused = false;
        started = true;
        timer.start();
    }

    private void pauseGame() {
        paused = true;
        menuIndex = 0;
        timer.pause();
    }

    private void restartGame() {
        resetGame();
        resumeGame();
    }

    private void handlePauseKey() {
        if (timer.isFinished()) return;          // after the round ended, ESC does nothing
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (paused) {
                resumeGame();
            } else {
                pauseGame();
            }
        }
    }

    // ---------- pause menu ----------

    private int[] menuActions() {
        if (timer.isFinished()) {
            return new int[] {ACTION_RESTART, ACTION_QUIT};
        }
        return new int[] {ACTION_CONTINUE, ACTION_RESTART, ACTION_QUIT};
    }

    private String menuLabel(int action) {
        switch (action) {
            case ACTION_CONTINUE: return started ? "Continue Game" : "Start Game";
            case ACTION_RESTART:  return "Restart Game";
            default:              return "Quit";
        }
    }

    private String menuTitle() {
        if (timer.isFinished()) return "TIME'S UP!";
        if (!started) return "GOOSING GUS";
        return "PAUSED";
    }

    // Positions the buttons in the centre of the screen
    private void updateMenuRects(int count) {
        float w = uiViewport.getWorldWidth();
        float h = uiViewport.getWorldHeight();
        float totalH = count * MENU_BUTTON_H + (count - 1) * MENU_GAP;
        float top = h / 2f + totalH / 2f - 30f;
        for (int i = 0; i < count; i++) {
            menuRects[i].set((w - MENU_BUTTON_W) / 2f,
                top - (i + 1) * MENU_BUTTON_H - i * MENU_GAP,
                MENU_BUTTON_W, MENU_BUTTON_H);
        }
    }

    private void handleMenuInput() {
        int[] actions = menuActions();
        int count = actions.length;
        if (menuIndex >= count) menuIndex = count - 1;
        updateMenuRects(count);

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            menuIndex = (menuIndex + count - 1) % count;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            menuIndex = (menuIndex + 1) % count;
        }

        // mouse hover and click
        mouse.set(Gdx.input.getX(), Gdx.input.getY());
        uiViewport.unproject(mouse);
        if (Gdx.input.getDeltaX() != 0 || Gdx.input.getDeltaY() != 0) {
            for (int i = 0; i < count; i++) {
                if (menuRects[i].contains(mouse)) menuIndex = i;
            }
        }

        boolean clicked = false;
        if (Gdx.input.justTouched()) {
            for (int i = 0; i < count; i++) {
                if (menuRects[i].contains(mouse)) {
                    menuIndex = i;
                    clicked = true;
                }
            }
        }
        // SPACE is not used here, it is the pit climbing key
        boolean confirmed = Gdx.input.isKeyJustPressed(Input.Keys.ENTER);

        if (clicked || confirmed) {
            runMenuAction(actions[menuIndex]);
        }
    }

    private void runMenuAction(int action) {
        switch (action) {
            case ACTION_CONTINUE:
                resumeGame();
                break;
            case ACTION_RESTART:
                restartGame();
                break;
            default:
                Gdx.app.exit();
        }
    }

    // ---------- game ----------

    private void processInput() {
        inputMovement.setZero();
        if (!pit.isTrapped()) {                  // cannot move while stuck in the pit
            // movement
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

            // items
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_1)) {
                useItem("Energy drink");
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_2)) {
                useItem("Stress ball");
            }
        }
        //normalise movement
        inputMovement.nor();
        player.changeDirection(inputMovement);


    }

    private void useItem(String item) {
        if (item.equals("Energy drink") && items.get("Energy drink") >= 1) {
            energyDrinkInUse = true;
            energyDrinkStartTime = timer.getRemaining();
            items.put("Energy drink", items.get("Energy drink") - 1);
            itemsUsed++;
        }
        if (item.equals("Stress ball") && items.get("Stress ball") >= 1) {
            player.setPatience(player.getPatience() + 30);
            items.put("Stress ball", items.get("Stress ball") - 1);
            System.out.println(items.get("Stress ball"));
            itemsUsed++;
        }
    }

    @Override
    public void render(float delta) {
        handlePauseKey();
        if (isMenuVisible()) {
            handleMenuInput();
        }
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

        // HUD, pit scene and menu on top
        drawUi();
    }

    private void updateLogic(float deltaTime) {
        timer.update(deltaTime);          // only counts down while running
        if (isPlaying()) {
            player.update(deltaTime);     // player and geese freeze when paused
            updateEvents();
        }
        if(energyDrinkInUse && !lake.overlaps(player)) {
            player.setSpeedMultiplier(2);
            if(timer.getRemaining() < energyDrinkStartTime - energyDrinkDuration) {
                energyDrinkInUse = false;
                player.setSpeedMultiplier(1);
            }
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

        // the pit is its own map: it covers the whole screen while the player is inside
        if (pit.isTrapped()) {
            drawPitScene(w, h);
        }

        // grey screen behind the menu
        if (isMenuVisible()) {
            batch.setColor(0.35f, 0.35f, 0.35f, 0.78f);
            batch.draw(pixelTexture, 0, 0, w, h);
            batch.setColor(Color.WHITE);
        }

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
        drawBox(timerBoxX, timerBoxY, timerBoxW, timerBoxH, FILL_COLOR);
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

        // no. of items used
        float itemsUsedY = iconY - 70f - boxH;
        drawLabelBox("Items used: " + itemsUsed, MARGIN, itemsUsedY,
            boxWidth("Items used: 0"), boxH);

        // item list
        float itemListY = iconY - 260f - boxH;
        drawLabelBox("Items:\n" +
                "1 - Energy drink (" + items.get("Energy drink") + ")\n" +
                "2 - Stress ball (" + items.get("Stress ball") + ")",
            MARGIN, itemListY,
            boxWidth("Items:\n1 - Energy drink(0)\n 2 - Stress ball(0)"),
            boxH * (ITEM_COUNT + 1));

        // patience level
        float patienceY = iconY - 400f - boxH;
        drawLabelBox("Patience level: " + Math.round(player.getPatience()), MARGIN, patienceY,
            boxWidth("Patience level: 100"), boxH);



        // menu
        if (isMenuVisible()) {
            drawMenu(w, h);
        }
        batch.end();
    }

    // The dirt pit: a shaft with a 30-rung ladder, the player climbs one rung per SPACE press
    private void drawPitScene(float w, float h) {
        // soil
        batch.setColor(0.22f, 0.13f, 0.07f, 1f);
        batch.draw(pixelTexture, 0, 0, w, h);
        Random rnd = new Random(7);
        for (int i = 0; i < 160; i++) {
            float size = 6f + rnd.nextFloat() * 14f;
            float shade = 0.12f + rnd.nextFloat() * 0.16f;
            batch.setColor(shade + 0.08f, shade, shade * 0.5f, 1f);
            batch.draw(pixelTexture, rnd.nextFloat() * w, rnd.nextFloat() * h, size, size);
        }

        // shaft
        float shaftW = w * 0.36f;
        float shaftX = (w - shaftW) / 2f;
        batch.setColor(0.40f, 0.25f, 0.12f, 1f);
        batch.draw(pixelTexture, shaftX, 0, shaftW, h);
        for (int i = 0; i < 70; i++) {
            float size = 6f + rnd.nextFloat() * 12f;
            float shade = 0.28f + rnd.nextFloat() * 0.14f;
            batch.setColor(shade + 0.10f, shade, shade * 0.45f, 1f);
            batch.draw(pixelTexture, shaftX + rnd.nextFloat() * (shaftW - size), rnd.nextFloat() * h, size, size);
        }

        // sky at the top of the shaft
        batch.setColor(0.55f, 0.80f, 1f, 1f);
        batch.draw(pixelTexture, shaftX + shaftW * 0.2f, h * 0.94f, shaftW * 0.6f, h * 0.06f);

        // ladder
        float cx = w / 2f;
        float half = shaftW * 0.16f;
        float railW = 10f;
        float y0 = h * 0.16f;
        float y1 = h * 0.88f;
        int total = pit.getTotalJumps();
        int jumps = pit.getJumps();
        batch.setColor(0.35f, 0.20f, 0.08f, 1f);
        batch.draw(pixelTexture, cx - half - railW / 2f, y0 - 10f, railW, y1 - y0 + 20f);
        batch.draw(pixelTexture, cx + half - railW / 2f, y0 - 10f, railW, y1 - y0 + 20f);
        for (int i = 1; i <= total; i++) {
            float ry = y0 + (y1 - y0) * i / total;
            if (i <= jumps) {
                batch.setColor(0.88f, 0.68f, 0.36f, 1f);   // rungs already climbed
            } else {
                batch.setColor(0.55f, 0.35f, 0.15f, 1f);
            }
            batch.draw(pixelTexture, cx - half, ry - 3f, 2f * half, 6f);
        }

        // player on the ladder
        float ph = h * 0.15f;
        float pw = ph * playerTexture.getWidth() / playerTexture.getHeight();
        float py = y0 + (y1 - y0) * jumps / total;
        batch.setColor(Color.WHITE);
        batch.draw(playerTexture, cx - pw / 2f, py, pw, ph);

        // text
        font.getData().setScale(MENU_TEXT_SCALE);
        font.setColor(Color.WHITE);
        String text = "Press SPACE to climb: " + jumps + " / " + total;
        layout.setText(font, text);
        font.draw(batch, text, (w - layout.width) / 2f, h * 0.11f);
        font.getData().setScale(HUD_SCALE);
        batch.setColor(Color.WHITE);
    }

    private void drawMenu(float w, float h) {
        int[] actions = menuActions();
        int count = actions.length;
        updateMenuRects(count);

        // title above the first button
        font.getData().setScale(MENU_TITLE_SCALE);
        font.setColor(Color.WHITE);
        layout.setText(font, menuTitle());
        float titleY = menuRects[0].y + MENU_BUTTON_H + 40f + layout.height;
        font.draw(batch, layout, (w - layout.width) / 2f, titleY);

        // buttons
        font.getData().setScale(MENU_TEXT_SCALE);
        for (int i = 0; i < count; i++) {
            Rectangle r = menuRects[i];
            drawBox(r.x, r.y, r.width, r.height, i == menuIndex ? SELECTED_FILL_COLOR : FILL_COLOR);
            String label = menuLabel(actions[i]);
            layout.setText(font, label);
            font.setColor(Color.WHITE);
            font.draw(batch, label, r.x + (r.width - layout.width) / 2f,
                r.y + (r.height + layout.height) / 2f);
        }

        // hint under the buttons
        font.getData().setScale(HUD_SCALE);
        String hint = timer.isFinished()
            ? "W/S or arrows to choose, Enter to select"
            : "W/S or arrows to choose, Enter to select, ESC to continue";
        layout.setText(font, hint);
        Rectangle last = menuRects[count - 1];
        font.draw(batch, hint, (w - layout.width) / 2f, last.y - 24f);
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

    // Box with beige border and dark outline; (x, y) is the bottom-left corner
    private void drawBox(float x, float y, float w, float h, Color fill) {
        batch.setColor(OUTLINE_COLOR);
        batch.draw(pixelTexture, x, y, w, h);
        batch.setColor(BORDER_COLOR);
        batch.draw(pixelTexture, x + OUTLINE, y + OUTLINE, w - 2 * OUTLINE, h - 2 * OUTLINE);
        batch.setColor(fill);
        batch.draw(pixelTexture, x + OUTLINE + BORDER, y + OUTLINE + BORDER,
            w - 2 * (OUTLINE + BORDER), h - 2 * (OUTLINE + BORDER));
        batch.setColor(Color.WHITE);
    }

    private void drawLabelBox(String text, float x, float y, float w, float h) {
        drawBox(x, y, w, h, FILL_COLOR);
        font.setColor(Color.WHITE);
        font.draw(batch, text, x + OUTLINE + BORDER + PAD, y + h - OUTLINE - BORDER - PAD);
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
