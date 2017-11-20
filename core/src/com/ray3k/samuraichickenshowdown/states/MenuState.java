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
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.samuraichickenshowdown.Core;
import com.ray3k.samuraichickenshowdown.State;

public class MenuState extends State {
    private Stage stage;
    private Skin skin;
    private Table root;

    public MenuState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        skin = Core.assetManager.get(Core.DATA_PATH + "/ui/Samurai Chicken Showdown.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        Gdx.input.setInputProcessor(stage);
        
        createMenu();
    }
    
    private void createMenu() {
        Table back = new Table();
        back.setFillParent(true);
        stage.addActor(back);
        
        Table fence = new Table(skin);
        fence.setBackground(skin.getTiledDrawable("fence"));
        back.add(fence).growX().height(192.0f).expandY().top().padTop(15.0f);
        
        root = new Table(skin);
        root.setFillParent(true);
        stage.addActor(root);
        
        Image titleImage = new Image(skin, "title");
        titleImage.setScaling(Scaling.none);
        root.add(titleImage).colspan(2);
        
        root.defaults().space(30.0f);
        root.row();
        TextButton textButtton = new TextButton("Play", skin);
        root.add(textButtton);
        
        textButtton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Core.assetManager.get(Core.DATA_PATH + "/sfx/block sound.wav", Sound.class).play(.25f);
                showSpeedDialog();
            }
        });
        
        textButtton = new TextButton("Quit", skin);
        root.add(textButtton);
        
        textButtton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Core.assetManager.get(Core.DATA_PATH + "/sfx/block sound.wav", Sound.class).play(.25f);
                Gdx.app.exit();
            }
        });
    }
    
    private void showSpeedDialog() {
        Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                Core.stateManager.loadState("game");
            }
        };
        
        Label label = new Label("CPU Reaction Speed", skin, "black");
        dialog.getContentTable().add(label).expandY().top();
        
        dialog.getContentTable().row();
        final Label minLabel = new Label("Minimum Speed: " + String.format("%.2f", GameState.minCpuSpeed) + " sec.", skin, "black");
        dialog.getContentTable().add(minLabel).left().expandX();
        
        dialog.getContentTable().row();
        final Slider minSlider = new Slider(0, GameState.maxCpuSpeed, .01f, false, skin);
        minSlider.setValue(GameState.minCpuSpeed);
        dialog.getContentTable().add(minSlider).growX();
        
        dialog.getContentTable().row();
        final Label maxLabel = new Label("Maximum Speed: " + String.format("%.2f", GameState.maxCpuSpeed) + " sec.", skin, "black");
        dialog.getContentTable().add(maxLabel).left().expandX();
        
        dialog.getContentTable().row();
        final Slider maxSlider = new Slider(GameState.minCpuSpeed, 1.0f, .01f, false, skin);
        System.out.println(GameState.maxCpuSpeed);
        maxSlider.setValue(GameState.maxCpuSpeed);
        dialog.getContentTable().add(maxSlider).growX().expandY().top();
        
        dialog.button("OK").key(Keys.ENTER, null).key(Keys.ESCAPE, null);
        
        dialog.show(stage);
        dialog.setWidth(700.0f);
        dialog.setHeight(500.0f);
        dialog.setPosition(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f, Align.center);
        
        minSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                GameState.minCpuSpeed = minSlider.getValue();
                minLabel.setText("Minimum Speed: " + String.format("%.2f", minSlider.getValue()) + " sec.");
                
                maxSlider.setRange(minSlider.getValue(), 1.0f);
            }
        });
        
        maxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                GameState.maxCpuSpeed = maxSlider.getValue();
                maxLabel.setText("Maximum Speed: " + String.format("%.2f", maxSlider.getValue()) + " sec.");
                
                minSlider.setRange(0.0f, maxSlider.getValue());
            }
        });
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(149 / 255.0f, 178 / 255.0f, 57 / 255.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    @Override
    public void act(float delta) {
        stage.act(delta);
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
        stage.getViewport().update(width, height, true);
    }
}