/*
 * NerdShooter is a pseudo library project for future Xemplar 2D Side Scroller Games.
 * Copyright (C) 2016  Rohan Loomis
 *
 * This file is part of NerdShooter
 *
 * NerdShooter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License, or
 * any later version.
 *
 * NerdShooter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.xemplar.games.android.nerdshooter.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.xemplar.games.android.nerdshooter.NerdShooter;
import com.xemplar.games.android.nerdshooter.blocks.ExitBlock;
import com.xemplar.games.android.nerdshooter.controller.JaxonController;
import com.xemplar.games.android.nerdshooter.entities.Entity;
import com.xemplar.games.android.nerdshooter.entities.Jaxon;
import com.xemplar.games.android.nerdshooter.entities.mobs.EnemyMob;
import com.xemplar.games.android.nerdshooter.model.World;
import com.xemplar.games.android.nerdshooter.utils.InterScreenData;
import com.xemplar.games.android.nerdshooter.utils.XPMLItem;
import com.xemplar.games.android.nerdshooter.view.WorldRenderer;

import static com.badlogic.gdx.graphics.GL20.*;
import static com.xemplar.games.android.nerdshooter.NerdShooter.GUI_SCALE;

public class GameScreen implements Screen, InputProcessor {
    public static boolean useGameDebugRenderer = false;
    public static GameScreen instance;
    public static long gameTicks = 0L;
    private Rectangle left, right, jump, fire, sanic;
    public World world;
    private Jaxon jaxon;
    public float buttonSize = 0F;

    private static int levelNum;
    private static String packName;
    public static Texture tex;
    public static int numPressed = 0;
    
    private WorldRenderer renderer;
    private JaxonController controller;
    private ShapeRenderer button;
    private SpriteBatch batch;
    private BitmapFont font;
    public int width, height;
    
    private TextureRegion controlLeft;
    private TextureRegion controlRight;
    private TextureRegion controlUp;
    
    public GameScreen(String pack, int level){
        gameTicks = 0L;
        instance = this;
        
        levelNum = level;
        packName = pack;
        
        tex = new Texture(Gdx.files.internal("scatt.png"));
        
        font = NerdShooter.text;
        font.setColor(1, 1, 1, 1);
        
        if(level == -1){
            GameScreen.useGameDebugRenderer = true;
        } else {
            GameScreen.useGameDebugRenderer = false;
        }
        
        controlLeft = NerdShooter.atlas.findRegion("HUDLeft");
        controlRight = NerdShooter.atlas.findRegion("HUDRight");
        controlUp = NerdShooter.atlas.findRegion("HUDJump");
        
        world = new World(pack, level);
        jaxon = world.getJaxon();
        controller = jaxon.getController();

        batch = new SpriteBatch();
        button = new ShapeRenderer();
        
        left = new Rectangle();
        right = new Rectangle();
        
        jump = new Rectangle();
        fire = new Rectangle();
        sanic = new Rectangle();
    }
    
    public void show() {
        gameTicks = 0L;
        
        renderer = new WorldRenderer(world, useGameDebugRenderer);
        controller.reset();
        Gdx.input.setInputProcessor(this);
        
        NerdShooter.shooter.setCurrentScreen(NerdShooter.GAME_SCREEN);
    }
    
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);

        updateEntities(delta);
        updateTextures((double)gameTicks);
        renderer.render(delta);

        button.begin(ShapeRenderer.ShapeType.Filled);{
            if (Gdx.app.getType().equals(Application.ApplicationType.Android)){
                Gdx.gl.glEnable(GL_BLEND);
                Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                button.setColor(0.0F, 0.5F, 0.5F, 0.5F);
                button.rect(left.x, left.y, left.width, left.height);
                button.rect(right.x, right.y, right.width, right.height);

                button.setColor(1.0F, 1.0F, 1.0F, 0.5F);
                button.rect(jump.x, jump.y, jump.width, jump.height);
                button.rect(fire.x, fire.y, fire.width, fire.height);
            }
            
            jaxon.inventory.renderGUI(button, width, height, buttonSize * GUI_SCALE);
        } button.end();

        batch.begin(); {
            long seconds = (long) ((gameTicks / 60D) * 10L);

            if (Gdx.app.getType().equals(Application.ApplicationType.Android)) {
                batch.draw(controlLeft, left.x, left.y, left.width, left.height);
                batch.draw(controlRight, right.x, right.y, right.width, right.height);
                batch.draw(controlUp, jump.x, jump.y, jump.width, jump.height);
            }

            jaxon.inventory.renderItems(batch, width, height, buttonSize * GUI_SCALE);
            jaxon.renderHealth(batch, width, height, buttonSize * GUI_SCALE);
            if (NerdShooter.PREF_DEBUG) {
                font.draw(batch, "Time: " + (seconds / 10D) + " seconds, FPS: " + Gdx.graphics.getFramesPerSecond() + " delta: " + Gdx.graphics.getDeltaTime(), 0, height - 10);
            }
        } batch.end();
        
        gameTicks++;
    }

    public static void finishLevel(int code){
        XPMLItem item = new XPMLItem("data");
        item.addElement(CompletedLevel.KEY_COMPLETED_TIME, (long)((gameTicks / 60D) * 10L) + "");
        item.addElement(CompletedLevel.KEY_FINISH_TYPE, code + "");
        item.addElement(CompletedLevel.KEY_LEVEL_NUM, levelNum + "");
        item.addElement(CompletedLevel.KEY_LEVEL_PACK, packName);
        
        reset_textures();
        
        InterScreenData.getInstance(NerdShooter.COMP_DATA).setData(item);
        NerdShooter.shooter.setScreen(CompletedLevel.instance);
    }
    
    public void resize(int width, int height) {
        renderer.setSize(width, height);
        this.width = width;
        this.height = height;
        buttonSize = height / NerdShooter.BUTTON_HEIGHT;
        
        left.set(buttonSize / 2F, buttonSize / 2F, buttonSize, buttonSize);
        right.set(buttonSize * 2F, buttonSize / 2F, buttonSize, buttonSize);
        
        jump.set(width - (buttonSize * 3F/2F), buttonSize / 2F, buttonSize, buttonSize);
        fire.set(width - (buttonSize * 3F/2F), buttonSize * 2F, buttonSize, buttonSize);
        
        sanic.set(0, height - buttonSize, buttonSize, buttonSize);
        
        if(!NerdShooter.PREF_LEFTY){
            left.set(buttonSize / 2F, buttonSize / 2F, buttonSize, buttonSize);
            right.set(buttonSize * 2F, buttonSize / 2F, buttonSize, buttonSize);
            
            jump.set(width - (buttonSize * 3F/2F), buttonSize / 2F, buttonSize, buttonSize);
            fire.set(width - (buttonSize * 3F/2F), buttonSize * 2F, buttonSize, buttonSize);
        } else {
            left.set(width - (buttonSize * 3F), buttonSize / 2F, buttonSize, buttonSize);
            right.set(width - (buttonSize * 3F/2F), buttonSize / 2F, buttonSize, buttonSize);
            
            jump.set(buttonSize / 2F, buttonSize / 2F, buttonSize, buttonSize);
            fire.set(buttonSize / 2F, buttonSize * 2F, buttonSize, buttonSize);
        }
    }
    
    public void hide() {
        Gdx.input.setInputProcessor(null);
        reset_textures();
    }
    
    public void pause() {
        reset_textures();
    }
    
    public void resume() {
        NerdShooter.shooter.setScreen(StartScreen.instance);
    }
    
    public void dispose() {
        Gdx.input.setInputProcessor(null);

        reset_textures();
    }
    
    public boolean keyDown(int keycode) {
        if(NerdShooter.shooter.useKeys){
            if (keycode == NerdShooter.shooter.keys[0]){
                controller.leftPressed(-1);
            }
            if (keycode == NerdShooter.shooter.keys[1]){
                controller.rightPressed(-1);
            }
            if (keycode == NerdShooter.shooter.keys[2]){
                controller.jumpPressed(-1);
            }
            if (keycode == NerdShooter.shooter.keys[3]){
                controller.firePressed(-1);
            }
        } else {
            if (keycode == Keys.LEFT){
                controller.leftPressed(-1);
            }
            if (keycode == Keys.RIGHT){
                controller.rightPressed(-1);
            }
            if (keycode == Keys.SPACE){
                controller.jumpPressed(-1);
            }
            if (keycode == Keys.X){
                controller.firePressed(-1);
            }
        }
        
        if ((keycode == Keys.BACK) || (keycode == Keys.ESCAPE)){
            finishLevel(ExitBlock.EXIT_NOCLEAR);
        }
        
        if (keycode == Keys.S && NerdShooter.sanic){
            NerdShooter.sanic = false;
            jaxon.loadTextures();
            StartScreen.reloadMusic();
        } else if (keycode == Keys.S && !NerdShooter.sanic){
            NerdShooter.sanic = true;
            jaxon.loadTextures();
            StartScreen.reloadMusic();
        }
        
        if (keycode == Keys.NUM_1){
            jaxon.inventory.setSelctedItem(3);
        }
        if (keycode == Keys.NUM_2){
            jaxon.inventory.setSelctedItem(2);
        }
        if (keycode == Keys.NUM_3){
            jaxon.inventory.setSelctedItem(1);
        }
        if (keycode == Keys.NUM_4){
            jaxon.inventory.setSelctedItem(0);
        }
        
        return true;
    }
    
    public boolean keyUp(int keycode) {
        if(NerdShooter.shooter.useKeys){
            if (keycode == NerdShooter.shooter.keys[0]){
                controller.leftReleased();
            }
            if (keycode == NerdShooter.shooter.keys[1]){
                controller.rightReleased();
            }
            if (keycode == NerdShooter.shooter.keys[2]){
                controller.jumpReleased();
            }
            if (keycode == NerdShooter.shooter.keys[3]){
                controller.fireReleased();
            }
        } else {
            if (keycode == Keys.LEFT){
                controller.leftReleased();
            }
            if (keycode == Keys.RIGHT){
                controller.rightReleased();
            }
            if (keycode == Keys.SPACE){
                controller.jumpReleased();
            }
            if (keycode == Keys.X){
                controller.fireReleased();
            }
        }
        return true;
    }
    
    public boolean keyTyped(char character) {
        return false;
    }
    
    
    public boolean touchDown(int x, int y, int pointer, int button) {
        if(Gdx.app.getType().equals(Application.ApplicationType.Android)){
            if(left.contains(x, (y - height) * -1)){
                controller.leftPressed(pointer);
            }
            
            if(right.contains(x, (y - height) * -1)){
                controller.rightPressed(pointer);
            }
        
            if(jump.contains(x, (y - height) * -1)){
                controller.jumpPressed(pointer);
            }
            
            if(fire.contains(x, (y - height) * -1)){
                controller.firePressed(pointer);
            }
            
            if(sanic.contains(x, (y - height) * -1)){
                if (NerdShooter.sanic){
                    NerdShooter.sanic = false;
                    jaxon.loadTextures();
                    StartScreen.reloadMusic();

                    numPressed = 0;
                } else if (!NerdShooter.sanic){
                    System.out.println(numPressed);
                    if(numPressed == 2){
                        NerdShooter.sanic = true;
                        jaxon.loadTextures();
                        StartScreen.reloadMusic();
                    }

                    numPressed++;
                }
                return true;
            }
        }
        
        return jaxon.inventory.pressed(x, (y - height) * -1);
    }
    
    public boolean touchUp(int x, int y, int pointer, int button) {
        if(Gdx.app.getType().equals(Application.ApplicationType.Android)){
            if(left.contains(x, (y - height) * -1)) {
                controller.leftReleased();
            }
        
            if(right.contains(x, (y - height) * -1)) {
                controller.rightReleased();
            }
        
            if(jump.contains(x, (y - height) * -1)){
                controller.jumpReleased();
            }
            
            if(fire.contains(x, (y - height) * -1)){
                controller.fireReleased();
            }
            return true;
        }
        return false;
    }
    
    public boolean touchDragged(int x, int y, int pointer) {
        if(Gdx.app.getType().equals(Application.ApplicationType.Android)){
            if(controller.isLeftDown() && !left.contains(x, (y - height) * -1) && controller.leftPointer == pointer) {
                controller.leftReleased();
            }
            if(controller.isRightDown() && !right.contains(x, (y - height) * -1) && controller.rightPointer == pointer) {
                controller.rightReleased();
            }
            if(controller.isJumpDown() && !jump.contains(x, (y - height) * -1) && controller.jumpPointer == pointer) {
                controller.jumpReleased();
            }
            
            if(!controller.isLeftDown() && left.contains(x, (y -height) * -1) && controller.leftPointer == -1){
                controller.leftPressed(pointer);
            }
            
            if(!controller.isRightDown() && right.contains(x, (y -height) * -1) && controller.rightPointer == -1){
                controller.rightPressed(pointer);
            }
            
            if(!controller.isJumpDown() && jump.contains(x, (y -height) * -1) && controller.jumpPointer == -1){
                controller.jumpPressed(pointer);
            }
            return true;
        }
        return false;
    }
    
    public boolean mouseMoved(int x, int y) {
        return false;
    }
    
    public boolean scrolled(int amount) {
        return false;
    }


    private static Array<UpdateTex> updates = new Array<UpdateTex>();
    public static void updateTextures(double ticks){
        for(UpdateTex up : updates){
            up.update(ticks);
        }
    }
    public static final void reset_textures(){
        for(UpdateTex up : updates){
            up.reset();
        }
    }
    public static final void setup_textures(TextureAtlas atlas){
        updates.add(new UpdateTex(atlas.findRegion("lava")));
        updates.add(new UpdateTex(atlas.findRegion("lavaTop_mid")));
    }
    private static final class UpdateTex{
        private boolean reset = true;
        private float startU, widthU;
        private TextureRegion region;
        
        private UpdateTex(TextureRegion region){
            this.region = region;
            startU = region.getU();
            widthU = (region.getU2() - startU) / 3F;
            startU += widthU;
        }

        private void update(double ticks){
            if(reset){
                startU = region.getU();
                widthU = (region.getU2() - startU) / 3F;
                startU += widthU;
            }
            reset = false;
            float new_x = (float)(((ticks % 100D) / 100D) * widthU);

            region.setU(startU + (widthU - new_x));
            region.setU2(startU + (widthU - new_x) + widthU);
        }
        
        private void reset(){
            if(reset) return;
            
            reset = true;
            
            startU -= widthU;
            region.setU(startU);
            region.setU2(startU + (widthU * 3F));
        }
    }

    public void updateEntities(float delta){
        for(Entity e : world.getEntities()){
            int w = world.getLevel().getWidth();
            int h = world.getLevel().getHeight();
            
            if(e.getPosition().x + 1 < 0){
                e.kill();
                continue;
            }
            
            if(e.getPosition().x > w){
                e.kill();
                continue;
            }
            
            if(e.getPosition().y + 1 < 0){
                e.kill();
                continue;
            }
            
            if(e.getPosition().y > h){
                e.kill();
                continue;
            }
            
            e.update(delta);
        }
    }
    
    public static TextureAtlas getTextureAltlas(){
        return NerdShooter.atlas;
    }
}
