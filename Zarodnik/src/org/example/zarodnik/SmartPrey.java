package org.example.zarodnik;

import java.util.List;

import org.example.others.RuntimeConfig;
import org.example.tinyEngineClasses.Entity;
import org.example.tinyEngineClasses.GameState;
import org.example.tinyEngineClasses.Mask;
import org.example.tinyEngineClasses.Music;
import org.example.tinyEngineClasses.SpriteMap;

import android.graphics.Bitmap;
import android.graphics.Point;

public class SmartPrey extends Creature {

	private int die_sound;
	
	public SmartPrey(int x, int y, Bitmap img, GameState game, List<Mask> mask, SpriteMap animations, String soundName, Point soundOffset, boolean collidable, int dieSound) {

		super(x, y, img, game, mask, animations, soundName, soundOffset, collidable, 1);
	
		die_sound = dieSound;
	}
	
    @Override
    protected void onUpdate() {
    	super.onUpdate();
    	if(checkAround() && !this.die)
    			avoidPlayer();
    }

	private void avoidPlayer() {
		double dx, dy;

		dx = this.game.getPlayer().getX() - this.x;
		dy = this.game.getPlayer().getY() - this.y;
		if(dx < 0 && dy < 0 && this.x + speed < GameState.SCREEN_WIDTH - getImgWidth() 
							&& this.y + speed < GameState.SCREEN_HEIGHT - getImgHeight()){
			this.x += speed;
			this.y += speed;
		}
		else{
			if(dx < 0 && dy > 0 && this.x + speed < GameState.SCREEN_WIDTH - getImgWidth() 
								&& this.y - speed > getImgHeight()){
				this.x += speed;
				this.y -= speed;
			}
			else{
				if(dx > 0 && dy < 0 && this.x - speed > getImgWidth() 
									&& this.y + speed < GameState.SCREEN_HEIGHT - getImgHeight()){
					this.x -= speed;
					this.y += speed;
				}
				else{
					if(this.x - speed > getImgWidth() && this.y - speed > getImgHeight()){
    					this.x -= speed;
    					this.y -= speed;
					}
				}
			}
		}
	}

	private boolean checkAround() {
		if(this.game != null){
			boolean result = (Math.abs(this.game.getPlayer().getX() - this.x) < 50)
				&& (Math.abs(this.game.getPlayer().getY() - this.y) < 50);
			return result;
		}
		else 
			return false;
	}
	
	@Override
	public void onRemove() {
		super.onRemove();
	}
	
	@Override
	public void onCollision(Entity e) {
		super.onCollision(e);
		if (e instanceof Player){
		}
	}
	
	@Override
	public void onTimer(int timer) {
		if(timer == 0)
			this.remove();
	}

	@Override
	public void onDie() {
		Music.getInstanceMusic().play(this.gameState.getContext(), die_sound, false);
		this.setDie(true);
		this.setTimer(0, RuntimeConfig.FRAMES_PER_STEP*4);
		this.playAnim("die", RuntimeConfig.FRAMES_PER_STEP, false);
		this.setCollidable(false);
	}
}