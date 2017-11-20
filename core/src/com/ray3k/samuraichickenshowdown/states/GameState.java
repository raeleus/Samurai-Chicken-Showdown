/*
 * The MIT License
 *
 * Copyright 2017 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ray3k.samuraichickenshowdown.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ray3k.samuraichickenshowdown.Core;
import com.ray3k.samuraichickenshowdown.EntityManager;
import com.ray3k.samuraichickenshowdown.InputManager;
import com.ray3k.samuraichickenshowdown.State;
import com.ray3k.samuraichickenshowdown.entities.ChickenEntity;
import com.ray3k.samuraichickenshowdown.entities.ExclamationEntity;
import com.ray3k.samuraichickenshowdown.entities.GameOverTimerEntity;
import com.ray3k.samuraichickenshowdown.entities.TooEarlyEntity;

public class GameState extends State {
    private static GameState instance;
    private float score;
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private OrthographicCamera uiCamera;
    private Viewport uiViewport;
    private InputManager inputManager;
    private Skin skin;
    private Stage stage;
    private Table table;
    private Label scoreLabel;
    public static EntityManager entityManager;
    public static TextureAtlas spineAtlas;
    private long backSound;
    private float timer;
    public static ChickenEntity playerChicken;
    public static ChickenEntity cpuChicken;
    public static float minCpuSpeed = 0.0f;
    public static float maxCpuSpeed = 1.0f;
    
    public static GameState inst() {
        return instance;
    }
    
    public GameState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        instance = this;
        
        score = 0;
        
        inputManager = new InputManager(); 
        
        uiCamera = new OrthographicCamera();
        uiViewport = new ScreenViewport(uiCamera);
        uiViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiViewport.apply();
        
        uiCamera.position.set(uiCamera.viewportWidth / 2, uiCamera.viewportHeight / 2, 0);
        
        gameCamera = new OrthographicCamera();
        gameViewport = new ScreenViewport(gameCamera);
        gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gameViewport.apply();
        
        gameCamera.position.set(gameCamera.viewportWidth / 2, gameCamera.viewportHeight / 2, 0);
        
        skin = Core.assetManager.get(Core.DATA_PATH + "/ui/Samurai Chicken Showdown.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(inputManager);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        entityManager = new EntityManager();
        
        createStageElements();
        
        spineAtlas = Core.assetManager.get(Core.DATA_PATH + "/spine/Samurai Chicken Showdown.atlas", TextureAtlas.class);
        
        playBackground();
        
        newGame();
    }
    
    public void newGame() {
        entityManager.clear();
        
        playerChicken = new ChickenEntity();
        entityManager.addEntity(playerChicken);
        playerChicken.getAnimationState().setAnimation(0, "peck", false);
        playerChicken.getAnimationState().addAnimation(0, "standing", false, 0.0f);
        playerChicken.getAnimationState().setAnimation(1, "label-p1", false);
        playerChicken.setPosition(150.0f, 100.0f);
        playerChicken.getSkeleton().setFlipX(true);
        
        cpuChicken = new ChickenEntity();
        entityManager.addEntity(cpuChicken);
        cpuChicken.getAnimationState().setAnimation(0, "jump", false);
        cpuChicken.getAnimationState().addAnimation(0, "standing", false, 0.0f);
        cpuChicken.getAnimationState().setAnimation(1, "label-cpu", false);
        cpuChicken.setPosition(Gdx.graphics.getWidth() - 150.0f, 100.0f);
        
        timer = MathUtils.random(6.0f, 12.0f);
    }
    
    private void createStageElements() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        scoreLabel = new Label("", skin);
        root.add(scoreLabel).expandY().padTop(25.0f).top();
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(207.0f / 255.0f, 111.0f / 255.0f, 101.0f / 255.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        
        gameCamera.update();
        spriteBatch.setProjectionMatrix(gameCamera.combined);
        spriteBatch.begin();
        spriteBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        spriteBatch.draw(spineAtlas.findRegion("bg"), 0.0f, 0.0f);
        entityManager.draw(spriteBatch, delta);
        spriteBatch.end();
        
        spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        stage.draw();
    }

    @Override
    public void act(float delta) {
        entityManager.act(delta);
        
        stage.act(delta);
        
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            Core.stateManager.loadState("menu");
        }
        
        if (timer > 0) {
            timer -= delta;
            if (timer < 0) {
                ExclamationEntity exc = new ExclamationEntity();
                exc.setPosition(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f);
                entityManager.addEntity(exc);
                
                stopBackground();
            } else if (Gdx.input.isKeyPressed(Keys.SPACE)) {
                timer = -1;
                
                TooEarlyEntity too = new TooEarlyEntity();
                too.setPosition(playerChicken.getX(), playerChicken.getY() + 75.0f);
                entityManager.addEntity(too);
                
                stopBackground();
                playError();
                
                entityManager.addEntity(new GameOverTimerEntity(2.0f));
            }
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void stop() {
        stage.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
        gameCamera.position.set(width / 2, height / 2.0f, 0.0f);
        
        uiViewport.update(width, height);
        uiCamera.position.set(uiCamera.viewportWidth / 2, uiCamera.viewportHeight / 2, 0);
        stage.getViewport().update(width, height, true);
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
        scoreLabel.setText(String.format("%.3f", score));
    }
    
    public void addScore(float score) {
        this.score += score;
        scoreLabel.setText(Float.toString(this.score));
    }

    public OrthographicCamera getGameCamera() {
        return gameCamera;
    }

    public void setGameCamera(OrthographicCamera gameCamera) {
        this.gameCamera = gameCamera;
    }

    public Skin getSkin() {
        return skin;
    }

    public Stage getStage() {
        return stage;
    }
    
    public void playBackground() {
        backSound = Core.assetManager.get(Core.DATA_PATH + "/sfx/background.wav", Sound.class).play(1.0f);
    }
    
    public void stopBackground() {
        Core.assetManager.get(Core.DATA_PATH + "/sfx/background.wav", Sound.class).stop(backSound);
    }
    
    public void playBlock() {
        Core.assetManager.get(Core.DATA_PATH + "/sfx/block sound.wav", Sound.class).play(.25f);
    }
    
    public void playSlash() {
        Core.assetManager.get(Core.DATA_PATH + "/sfx/slash.wav", Sound.class).play(.25f);
    }
    
    public void playError() {
        Core.assetManager.get(Core.DATA_PATH + "/sfx/error.wav", Sound.class).play(.25f);
    }

    public Label getScoreLabel() {
        return scoreLabel;
    }
}