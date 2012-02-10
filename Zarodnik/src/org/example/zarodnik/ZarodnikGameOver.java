package org.example.zarodnik;

import java.io.IOException;

import org.example.R;
import org.example.others.RuntimeConfig;
import org.example.tinyEngineClasses.BitmapScaler;
import org.example.tinyEngineClasses.Game;
import org.example.tinyEngineClasses.GameState;
import org.example.tinyEngineClasses.Input;
import org.example.tinyEngineClasses.Input.EventType;
import org.example.tinyEngineClasses.TTS;
import org.example.tinyEngineClasses.VolumeManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.view.View;

public class ZarodnikGameOver extends GameState {
	
	private Text text;
	
	private Bitmap arrow;
	
	private int stepsPerWord = RuntimeConfig.TEXT_SPEED;

	
	public ZarodnikGameOver(View v, TTS textToSpeech, Context c, Game game) {
		super(v,c,textToSpeech, game);
		
		int textoffSetX;
		int textoffSetY;
		float fontSize;
		Typeface font;
		Paint brush;
		
		fontSize =  (this.getContext().getResources().getDimension(R.dimen.font_size_intro)/GameState.scale);
		font = Typeface.createFromAsset(this.getContext().getAssets(),RuntimeConfig.FONT_PATH);
		brush = new Paint();
		brush.setTextSize(fontSize);
		brush.setARGB(255, 255, 255, 204);
		if(font != null)
			brush.setTypeface(font);
	
		textoffSetX = SCREEN_WIDTH  / 4;
		textoffSetY = SCREEN_HEIGHT / 2;
		text = new Text(textoffSetX, textoffSetY, null, this, null, null, 
				null, null, false, brush, stepsPerWord, this.getContext().getString(R.string.game_over_text));
		this.addEntity(text);
		
		BitmapScaler scaler;
		try {
			scaler = new BitmapScaler(this.getContext().getResources(),R.drawable.arrow, 30);
			arrow = scaler.getScaled();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void onInit() {
		super.onInit();
		getTextToSpeech().speak(this.context.getString(R.string.game_over_tts));
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(text.isFinished()){
			canvas.drawBitmap(arrow, SCREEN_WIDTH/2 - arrow.getWidth(), (SCREEN_HEIGHT - SCREEN_HEIGHT/5)  - arrow.getHeight(), null);
		}
	}
	
	public void onUpdate() {
		super.onUpdate();
		EventType e = Input.getInput().removeEvent("onVolUp");
		if (e != null){
			VolumeManager.adjustStreamVolume(this.context, AudioManager.ADJUST_RAISE);
		}else{
			e = Input.getInput().removeEvent("onVolDown");
			if (e != null)
				VolumeManager.adjustStreamVolume(this.context, AudioManager.ADJUST_LOWER);
		}
		
		e = Input.getInput().removeEvent("onLongPress");
		if(e != null && text.isFinished()){
			this.stop();
		}
		else{
			if(e != null){
				text.setStepsPerWord(1);
			}
		}
	}
}