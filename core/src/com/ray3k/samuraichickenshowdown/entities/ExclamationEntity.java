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

package com.ray3k.samuraichickenshowdown.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.TimeUtils;
import com.ray3k.samuraichickenshowdown.Core;
import com.ray3k.samuraichickenshowdown.Entity;
import com.ray3k.samuraichickenshowdown.SpineEntity;
import com.ray3k.samuraichickenshowdown.states.GameState;

public class ExclamationEntity extends SpineEntity {
    private float cpuReactionSpeed;
    private float timer;
    private long timeStamp;
    private boolean pressed;
    private InputListener inputListener;

    @Override
    public void actSub(float delta) {
        if (timer >= 0) {
            System.out.println(timer);
            timer -= delta;

            if (timer <= 0.0f) {
                dispose();
                GameState.entityManager.addEntity(new FlashEntity());

                switch (MathUtils.random(2)) {
                    case 0:
                        GameState.cpuChicken.getAnimationState().setAnimation(0, "attack-paint", false);
                        GameState.playerChicken.getAnimationState().setAnimation(0, "hurt-brush", false);
                        GameState.cpuChicken.getSkeleton().findSlot("accessory-above").getColor().set(Color.RED);
                        GameState.playerChicken.getSkeleton().findSlot("stain").getColor().set(Color.RED);
                        break;
                    case 1:
                        GameState.cpuChicken.getAnimationState().setAnimation(0, "attack-dye", false);
                        GameState.playerChicken.getAnimationState().setAnimation(0, "hurt-dye", true);
                        GameState.cpuChicken.getSkeleton().findSlot("accessory-below").getColor().set(Color.RED);
                        GameState.playerChicken.getSkeleton().setColor(Color.RED);
                        break;
                    case 2:
                        GameState.cpuChicken.getAnimationState().setAnimation(0, "attack-pie", false);
                        GameState.playerChicken.getAnimationState().setAnimation(0, "hurt-pie", false);
                        GameState.playerChicken.getSkeleton().findSlot("accessory-above").getColor().set(Color.RED);
                        GameState.playerChicken.getSkeleton().findSlot("stain").getColor().set(Color.RED);
                        break;
                }

                GameState.playerChicken.setX(500.0f);
                GameState.cpuChicken.setX(150.0f);
                
                GameState.entityManager.addEntity(new GameOverTimerEntity(7.0f));
                GameState.entityManager.addEntity(new WinnerEntity(WinnerEntity.Type.CPU));
                
                GameState.inst().getScoreLabel().setText(String.format("%.3f", cpuReactionSpeed));
            } else if (pressed) {
                timer = -1;
                dispose();
                GameState.entityManager.addEntity(new FlashEntity());

                switch (MathUtils.random(2)) {
                    case 0:
                        GameState.playerChicken.getAnimationState().setAnimation(0, "attack-paint", false);
                        GameState.cpuChicken.getAnimationState().setAnimation(0, "hurt-brush", false);
                        GameState.playerChicken.getSkeleton().findSlot("accessory-above").getColor().set(Color.BLUE);
                        GameState.cpuChicken.getSkeleton().findSlot("stain").getColor().set(Color.BLUE);
                        break;
                    case 1:
                        GameState.playerChicken.getAnimationState().setAnimation(0, "attack-dye", false);
                        GameState.cpuChicken.getAnimationState().setAnimation(0, "hurt-dye", true);
                        GameState.playerChicken.getSkeleton().findSlot("accessory-below").getColor().set(Color.BLUE);
                        GameState.cpuChicken.getSkeleton().setColor(Color.BLUE);
                        break;
                    case 2:
                        GameState.playerChicken.getAnimationState().setAnimation(0, "attack-pie", false);
                        GameState.cpuChicken.getAnimationState().setAnimation(0, "hurt-pie", false);
                        GameState.cpuChicken.getSkeleton().findSlot("accessory-above").getColor().set(Color.BLUE);
                        GameState.cpuChicken.getSkeleton().findSlot("stain").getColor().set(Color.BLUE);
                        break;
                }

                GameState.playerChicken.setX(650.0f);
                GameState.cpuChicken.setX(300.0f);
                
                GameState.entityManager.addEntity(new GameOverTimerEntity(7.0f));
                GameState.entityManager.addEntity(new WinnerEntity(WinnerEntity.Type.PLAYER));
            }
        }
    }

    @Override
    public void drawSub(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void create() {
        setSkeletonData(Core.DATA_PATH + "/spine/exclamation.json", "animation");
        getAnimationState().setAnimation(0, "animation", false);
        GameState.inst().playBlock();
        
        timer = MathUtils.random(GameState.minCpuSpeed, GameState.maxCpuSpeed);
        cpuReactionSpeed = timer;
        timeStamp = TimeUtils.nanoTime();
        pressed = false;
        
        inputListener = new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (!pressed) {
                    GameState.inst().setScore(TimeUtils.timeSinceNanos(timeStamp) / 1000000000.0f);
                    pressed = true;
                }
                return false;
            }
        };
        
        GameState.inst().getStage().addListener(inputListener);
    }

    @Override
    public void actEnd(float delta) {
    }

    @Override
    public void destroy() {
        GameState.inst().getStage().removeListener(inputListener);
    }

    @Override
    public void collision(Entity other) {
    }
    
}
