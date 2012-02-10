package org.example.zarodnik;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.example.R;
import org.example.activities.MainActivity;
import org.example.activities.ZarodnikGameActivity;
import org.example.others.RuntimeConfig;
import org.example.tinyEngineClasses.BitmapScaler;
import org.example.tinyEngineClasses.CustomBitmap;
import org.example.tinyEngineClasses.Entity;
import org.example.tinyEngineClasses.Game;
import org.example.tinyEngineClasses.GameState;
import org.example.tinyEngineClasses.Input;
import org.example.tinyEngineClasses.Input.EventType;
import org.example.tinyEngineClasses.Mask;
import org.example.tinyEngineClasses.MaskCircle;
import org.example.tinyEngineClasses.Music;
import org.example.tinyEngineClasses.NumberGenerator;
import org.example.tinyEngineClasses.Sound2D;
import org.example.tinyEngineClasses.SpriteMap;
import org.example.tinyEngineClasses.TTS;
import org.example.tinyEngineClasses.VolumeManager;
import org.pielot.openal.Source;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.view.View;

public class ZarodnikGameplay extends GameState {
	
	public enum Sense { UP, DOWN, LEFT, RIGHT };
	
	private static final int intro_sound = R.raw.start;
	
	private static final int maxPredatorNumber = 3;
	private static final int prey_sound_die = R.raw.prey_dead;

	private static String prey_sound = "prey";
	private static String predator_sound = "predator";
	private static String radio_sound = "radio";
	private static String seaweed_sound = "radio"; // TODO: find ocean sound
	private static String capsule_sound = "radio"; // TODO: find ? sound
 	
	// Sheets dimensions
	private static int playerRow = 8;
	private static int playerCol = 3;
	private static int preyRow = 1;
	private static int preyCol = 9;
	private static int predatorRow = 1;
	private static int predatorCol = 8;
	
	private float fontSize;
	private Typeface font;
	private Paint brush;
	
	private Player player;
	private Entity prey;
	
	private boolean flag = false;
	
	private int level;

	private int preyN;
	
	private boolean transition;
	private int dx;
	private int dy;
	private int incX;
	private int incY;
	private List<Entity> tempEntities; 
	
	private Bitmap backgroundImage;
	
	private int record;

	private boolean tutorial;

	public ZarodnikGameplay(View v, TTS textToSpeech, Context c, Game game) {
		super(v,c,textToSpeech, game);
		
		int sheetSize;
		
		textToSpeech.setQueueMode(TTS.QUEUE_ADD);
		textToSpeech.setInitialSpeech("");

		record = loadRecord();
		
		sheetSize = 400;
		tempEntities = new ArrayList<Entity>();
		createEntities(record,sheetSize);
		
		// Set background image
		backgroundImage = BitmapFactory.decodeResource(v.getResources(), R.drawable.background);
		backgroundImage = CustomBitmap.getResizedBitmap(backgroundImage, SCREEN_HEIGHT, SCREEN_WIDTH);
		//setBackground(field);
		
		fontSize = (this.getContext().getResources().getDimensionPixelSize(R.dimen.font_size_gameplay)/GameState.scale);
		
		font = Typeface.createFromAsset(this.getContext().getAssets(),RuntimeConfig.FONT_PATH);
		
		brush = new Paint();
		brush.setTextSize(fontSize);
		brush.setARGB(255, 51, 51, 51);
		if(font != null)
			brush.setTypeface(font);
		
		flag = true;
		dx = 0; 
		dy = 0;
		tutorial = false;
	}
	
	public Player getPlayer(){
		return player;
	}
	
	/**
	 * Loads from internal file system the previous record in Free Mode.
	 * 
	 * */
	private int loadRecord() {
		FileInputStream fis;
		int record = -1;
		try { 
			fis = this.getContext().openFileInput(MainActivity.FILENAMEFREEMODE);
			ObjectInputStream ois = new ObjectInputStream(fis);
			Object f = ois.readObject();
			record = (Integer) f;
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (record == -1)
			return 0;
		else
			return record;
		
	}
	
	/**
	 * Instantiates the entities in the game.
	 * @param sheetSize 
	 * 
	 * */
	private void createEntities(int record, int sheetSize) {
		// Game entities: predators, preys and player
		
		// Player
		createPlayer(record, sheetSize);
		// Predators
		createPredator(sheetSize);
		// Prey
		createPrey(sheetSize);
		
		Iterator<Entity> it = tempEntities.iterator(); 
		Entity e;
		while(it.hasNext()){
			e = it.next();
			this.addEntity(e);
		}
		tempEntities.clear();
	}
	
	
	private void createPlayer(int record, int sheetSize) {
		int  playerX, playerY;
		int frameW, frameH;
		Bitmap playerBitmap = null;
		ArrayList<Integer> aux;
		ArrayList<Mask> playerMasks;
		
		/*BitmapScaler scaler;
		
		try {
			scaler = new BitmapScaler(this.getContext().getResources(), R.drawable.playersheetx, sheetSize);
			playerBitmap = scaler.getScaled();
		} catch (IOException ex) {
			ex.printStackTrace();
		}*/
		
		playerBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.playersheetm);
		
		/*-------- Animations --------------------------------------*/
		SpriteMap animations = new SpriteMap(playerRow, playerCol, playerBitmap, 0);
		aux = new ArrayList<Integer>();
		aux.add(0);
		aux.add(1);
		aux.add(2);
		aux.add(1);
		aux.add(0);
		animations.addAnim("left", aux, RuntimeConfig.FRAMES_PER_STEP, true);
		
		aux = new ArrayList<Integer>();
		aux.add(0);
		aux.add(3);
		aux.add(4);
		aux.add(5);
		aux.add(4);
		aux.add(3);
		aux.add(0);
		animations.addAnim("eatL", aux, RuntimeConfig.FRAMES_PER_STEP, false);

		aux = new ArrayList<Integer>();
		aux.add(18);
		aux.add(19);
		aux.add(20);
		aux.add(19);
		aux.add(18);
		animations.addAnim("right", aux, RuntimeConfig.FRAMES_PER_STEP, true);
		
		aux = new ArrayList<Integer>();
		aux.add(18);
		aux.add(21);
		aux.add(22);
		aux.add(23);
		aux.add(22);
		aux.add(21);
		aux.add(18);
		animations.addAnim("eatR", aux, RuntimeConfig.FRAMES_PER_STEP, false);
		
		aux = new ArrayList<Integer>();
		aux.add(6);
		aux.add(7);
		animations.addAnim("up", aux, RuntimeConfig.FRAMES_PER_STEP, true);
		
		aux = new ArrayList<Integer>();
		aux.add(6);
		aux.add(8);
		aux.add(7);
		aux.add(6);
		animations.addAnim("eatU", aux, RuntimeConfig.FRAMES_PER_STEP, false);
		
		aux = new ArrayList<Integer>();
		aux.add(9);
		aux.add(10);
		aux.add(11);
		aux.add(10);
		aux.add(9);
		animations.addAnim("down", aux, RuntimeConfig.FRAMES_PER_STEP, false);
		
		aux = new ArrayList<Integer>();
		aux.add(9);
		aux.add(12);
		aux.add(13);
		aux.add(14);
		aux.add(13);
		aux.add(12);
		aux.add(9);
		animations.addAnim("eatD", aux, RuntimeConfig.FRAMES_PER_STEP, false);
		
		aux = new ArrayList<Integer>();
		aux.add(15);
		aux.add(16);
		aux.add(17);
		animations.addAnim("die", aux, RuntimeConfig.FRAMES_PER_STEP, false);
		
		/*--------------------------------------------------*/
		
		frameW = playerBitmap.getWidth() / playerCol;
		frameH = playerBitmap.getHeight() / playerRow;
		
		playerMasks = new ArrayList<Mask>();
		playerMasks.add(new MaskCircle(frameW/2,frameH/2,frameW/3));

		playerX = SCREEN_WIDTH / 2;
		playerY = SCREEN_HEIGHT - SCREEN_HEIGHT / 3;
		
		player = new Player(playerX, playerY, record, playerBitmap, this, playerMasks, animations, null, null);
		
		tempEntities.add(player);
	}
	
	private void createPredator(int sheetSize) {
		Entity e; 
		List<Sound2D> sources; 
		Source s;
		int frameW, frameH;
		int predatorN, predatorX, predatorY;
		int width, height;
		Random numberGenerator;
		Bitmap predatorBitmap = null;
		ArrayList<Integer> aux;
		ArrayList<Mask> predatorMasks;
		
		numberGenerator = new Random();
		predatorN = numberGenerator.nextInt(maxPredatorNumber) + 1;
		
		BitmapScaler scaler;
		try {
			scaler = new BitmapScaler(this.getContext().getResources(), R.drawable.predatorsheetx, sheetSize);
			predatorBitmap = scaler.getScaled();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		
		frameW = predatorBitmap.getWidth() / predatorCol;
		frameH = predatorBitmap.getHeight() / predatorRow;
		width = SCREEN_WIDTH - frameW*2;
		height = SCREEN_HEIGHT - frameH*2;
		while (predatorN != 0){
			predatorX = numberGenerator.nextInt(width);
			predatorY = numberGenerator.nextInt(height);
		
			predatorMasks = new ArrayList<Mask>();
			predatorMasks.add(new MaskCircle(frameW/2,frameH/2,frameW/3));	
			
			SpriteMap animations = new SpriteMap(predatorRow, predatorCol, predatorBitmap, 0);
			aux = new ArrayList<Integer>();
			aux.add(6);
			animations.addAnim("up", aux, RuntimeConfig.FRAMES_PER_STEP, false);
			aux = new ArrayList<Integer>();
			aux.add(4);
			aux.add(5);
			animations.addAnim("down", aux, RuntimeConfig.FRAMES_PER_STEP, false);
			aux = new ArrayList<Integer>();
			aux.add(2);
			aux.add(3);
			animations.addAnim("left", aux, RuntimeConfig.FRAMES_PER_STEP, false);
			aux = new ArrayList<Integer>();
			aux.add(0);
			aux.add(1);
			animations.addAnim("right", aux, RuntimeConfig.FRAMES_PER_STEP, false);
			aux = new ArrayList<Integer>();
			aux.add(7);
			animations.addAnim("die", aux, RuntimeConfig.FRAMES_PER_STEP, false);
			
			e = new Predator(predatorX, predatorY, null, this, predatorMasks, animations, 
					predator_sound, new Point(frameW/2,frameW/2), true);
			
			
			while (!this.positionFreeEntities(e)){
				predatorX = numberGenerator.nextInt(width);
				predatorY = numberGenerator.nextInt(height);
				e.setX(predatorX); e.setY(predatorY);
			}
			
			tempEntities.add(e);
			
			sources = e.getSources();
			if(!sources.isEmpty()){
				s = sources.get(0).getS();
				s.setGain(5);
				s.setPitch(0.5f);
			}
			predatorN--;
		}
	}


	private void createPrey(int sheetSize) {
		int  preyX, preyY;
		int frameW, frameH;
		int width, height;
		Random numberGenerator;
		Bitmap preyBitmap = null;
		ArrayList<Integer> aux;
		ArrayList<Mask> preyMasks;
		Entity e;
		
		BitmapScaler scaler;
		try {
			scaler = new BitmapScaler(this.getContext().getResources(), R.drawable.preysheetx, sheetSize);
			preyBitmap = scaler.getScaled();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		/*-------- Animations --------------------------------------*/
		SpriteMap animations = new SpriteMap(preyRow, preyCol, preyBitmap, 0);
		aux = new ArrayList<Integer>();
		aux.add(6);
		aux.add(7);
		animations.addAnim("up", aux, RuntimeConfig.FRAMES_PER_STEP, false);
		aux = new ArrayList<Integer>();
		aux.add(4);
		aux.add(5);
		animations.addAnim("down", aux, RuntimeConfig.FRAMES_PER_STEP, false);
		aux = new ArrayList<Integer>();
		aux.add(2);
		aux.add(3);
		animations.addAnim("left", aux, RuntimeConfig.FRAMES_PER_STEP, false);
		aux = new ArrayList<Integer>();
		aux.add(0);
		aux.add(1);
		animations.addAnim("right", aux, RuntimeConfig.FRAMES_PER_STEP, false);
		aux = new ArrayList<Integer>();
		aux.add(8);
		animations.addAnim("die", aux, RuntimeConfig.FRAMES_PER_STEP, false);
		/*----------------------------------------------*/
		
		frameW = preyBitmap.getWidth() / preyCol;
		frameH = preyBitmap.getHeight() / preyRow;
		
		preyMasks = new ArrayList<Mask>();
		preyMasks.add(new MaskCircle(frameW/2,frameH/2,frameW/3));	
		
		numberGenerator = new Random();
		width = SCREEN_WIDTH - frameW*2;
		height = SCREEN_HEIGHT - frameH*2;
		preyX = numberGenerator.nextInt(width);
		preyY = numberGenerator.nextInt(height);
		
		e = new SmartPrey(preyX, preyY, null, this, preyMasks, animations,  
				prey_sound, new Point(frameW/2,frameW/2),true, prey_sound_die);
		
		while (!this.positionFreeEntities(e)){
			preyX = numberGenerator.nextInt(width);
			preyY = numberGenerator.nextInt(height);
			e.setX(preyX); e.setY(preyY);
		}
		
		tempEntities.add(e);
		
		prey = e;
		
		preyN = 1;
	}
	
	@Override
	public void onInit() {
		super.onInit();
		this.getTextToSpeech().setQueueMode(TextToSpeech.QUEUE_FLUSH);
		this.getTextToSpeech().speak(" ");
		Music.getInstanceMusic().playWithBlock(this.getContext(), intro_sound, false);
		this.getTextToSpeech().setQueueMode(TextToSpeech.QUEUE_FLUSH);
		this.getTextToSpeech().speak(this.getContext().getString(R.string.game_play_initial_TTStext));
		Input.getInput().clean();
	}

	@Override
	public void onDraw(Canvas canvas) {
        if(transition){
        	transitionEffectBackground(canvas);
        }
        else{
    		canvas.drawBitmap(backgroundImage, null, new Rect(0, 0, GameState.SCREEN_WIDTH, GameState.SCREEN_HEIGHT), null);
    		
            if(flag){
        		brush.setARGB(255, 0, 0, 51);
        	    canvas.drawText(this.getContext().getString(R.string.initial_message), GameState.SCREEN_WIDTH/3, GameState.SCREEN_HEIGHT/2, brush);
            	flag = false;
            }
            
            if(player.isRemovable()){
            	brush.setARGB(255, 0, 0, 51);
            	canvas.drawText(this.getContext().getString(R.string.ending_lose_message), GameState.SCREEN_WIDTH/3, GameState.SCREEN_HEIGHT/2, brush);
            }
        }
        super.onDraw(canvas);
	}
	
	@Override
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
		
		isChangeScreen();

	}

	private void isChangeScreen() {
		if(preyN == 0 && !transition){
				if(player.getX() < 30){
					incX = 32;
					incY = 0;
					transition = true;
					createEntitiesWithoutPlayer(400);
					Music.getInstanceMusic().play(this.getContext(), R.raw.bip, true);
				}else{
					if(player.getX() + (player.getImgWidth()) > GameState.SCREEN_WIDTH){
						incX = -32;
						incY = 0;
						transition = true;
						createEntitiesWithoutPlayer(400);
						Music.getInstanceMusic().play(this.getContext(), R.raw.bip, true);
					}else{
						if(player.getY() < 30){
							incX = 0;
							incY = 32;
							transition = true;
							createEntitiesWithoutPlayer(400);
							Music.getInstanceMusic().play(this.getContext(), R.raw.bip, true);
						}else{
							if(player.getY() + (player.getImgHeight()) > GameState.SCREEN_HEIGHT){
								incX = 0;
								incY = -32;
								transition = true;
								createEntitiesWithoutPlayer(400);
								Music.getInstanceMusic().play(this.getContext(), R.raw.bip, true);
							}
						}
					}
				}

		}
        if(transition){
        	transitionEffectLogic();
        }
	}
	

	private void transitionEffectBackground(Canvas canvas) {
		int width, widthPix; 
		int height, heightPix;
		int offSetX, offSetY;
		int x, y;
		Iterator<Entity> it;
		Entity e;
		DisplayMetrics dm = new DisplayMetrics();
		this.getContext().getWindowManager().getDefaultDisplay().getMetrics(dm); 
		width = dm.widthPixels; 
		height = dm.heightPixels;
		widthPix = (int) Math.ceil(dm.widthPixels * (dm.densityDpi / 160.0));
		heightPix = (int) Math.ceil(dm.heightPixels * (dm.densityDpi / 160.0));
		
		canvas.drawBitmap(backgroundImage, null, new Rect(dx,dy,dx + backgroundImage.getWidth(),dy + backgroundImage.getHeight()),null);
		
		offSetX = (int) (dx + Math.signum(-incX) * backgroundImage.getWidth());
		offSetY = (int) (dy + Math.signum(-incY) * backgroundImage.getHeight());
		canvas.drawBitmap(backgroundImage, null, new Rect(offSetX, offSetY,
					offSetX +  backgroundImage.getWidth(),offSetY + backgroundImage.getHeight()),null);
		
		it = this.getEntities().iterator(); 
		while(it.hasNext()){
			e = it.next();
			if(!(e instanceof ScoreBoard)){
				e.setFrozen(true);
				e.setX((int) (e.getX() + incX)); 
				e.setY((int) (e.getY() + incY));
			}
			if(e instanceof Radio || e instanceof Seaweed || e instanceof Capsule){
				e.remove();
			}
			e.onDraw(canvas);
		}

		it = tempEntities.iterator(); 
		while(it.hasNext()){
			e = it.next();
			e.setFrozen(true);
			x = (int) (e.getX() + offSetX); 
			y = (int) (e.getY() + offSetY);
			e.onDraw(x, y, canvas);
		}

		if(dy  == height || dx == width || dy  == -height || dx ==  -width){
			transition = false;
			it = this.getEntities().iterator(); 
			while(it.hasNext()){
				e = it.next();
				if(e instanceof Player){
					e.setFrozen(false);
					((Player)e).setInMovement(false);
				}else{
					if(!(e instanceof ScoreBoard))
						e.remove();
				}
			}
			it = tempEntities.iterator(); 
			while(it.hasNext()){
				e = it.next();
				e.setFrozen(false);
				this.addEntity(e);
			}
			tempEntities.clear();
			getRenderables().clear();
			dx = 0;
			dy = 0;
			this.getTextToSpeech().setQueueMode(TTS.QUEUE_FLUSH);
			this.getTextToSpeech().speak(this.getContext().getString(R.string.screen_change));
			Music.getInstanceMusic().stop(this.getContext(), R.raw.bip);
			if(tutorial)
				this.stop();
		}
	}
	
	private void transitionEffectLogic() {
		dy += incY;
		dx += incX;
	}

	public void decrementPrey() {
		preyN --;
	}
	
	private void createEntitiesWithoutPlayer(int sheetSize){
		// Predators
		createPredator(sheetSize);
		// Prey
		createPrey(sheetSize);
		// Capsule, radio, seaweed, whatever
		createItems();
	}

	private void createItems() {
		Item i = null;
		int n = NumberGenerator.nextInt(Item.ITEM_NUMBER);
		
		if(n >= 0 && n < Item.ITEM_NUMBER)
			i = createItem(n);
		
		// Only if it's the first time that we instantiate the Item
		if(i != null){
			
			tempEntities.add(i);
			
			if(i.isFirstInstance()){
				List<Integer> order = this.game.getOrder();
				switch(n){
					case(Item.RADIO):
						order.add(this.game.getNext() + 1, ZarodnikGameActivity.TUTORIAL1_ID);
						break;
					case(Item.SEAWEED):
						order.add(this.game.getNext() + 1, ZarodnikGameActivity.TUTORIAL2_ID);
						break;
					case(Item.CAPSULE):
						order.add(this.game.getNext() + 1, ZarodnikGameActivity.TUTORIAL3_ID);
						break;
				}
				order.add(this.game.getNext() + 2, ZarodnikGameActivity.GAMEPLAY_ID);
				tutorial = true;
			}
			else
				tutorial = false;
		}
	}

	private Item createItem(int type) {
		Item i = null; 
		int  x, y;
		Source s;
		int frameW, frameH;
		int width, height;
		Bitmap itemBitmap = null;
		ArrayList<Mask> itemMasks;

		switch(type){
			case(Item.RADIO):
				itemBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.radio);
				break;
			case(Item.SEAWEED):
				itemBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.seaweed);
				break;
			case(Item.CAPSULE):
				itemBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.capsule);
				break;
			default:
				break;
		}
		
		frameW = itemBitmap.getWidth();
		frameH = itemBitmap.getHeight();
		
		itemMasks = new ArrayList<Mask>();
		itemMasks.add(new MaskCircle(frameW/2,frameH/2,frameW/3));	
		
		width = SCREEN_WIDTH - frameW*2;
		height = SCREEN_HEIGHT - frameH*2;
		x = NumberGenerator.nextInt(width);
		y = NumberGenerator.nextInt(height);
		
		switch(type){
		case(Item.RADIO):
			i = new Radio(x, y, itemBitmap, this, itemMasks, null, radio_sound, new Point(frameW/2,frameW/2), true, prey);
			s = i.getSources().get(0).getS();
			s.setGain(2);
			break;
		case(Item.SEAWEED):
			i = new Seaweed(x, y, itemBitmap, this, itemMasks, null, seaweed_sound, new Point(frameW/2,frameW/2), true);
			s = i.getSources().get(0).getS();
			s.setGain(2);
			break;
		case(Item.CAPSULE):
			i = new Capsule(x, y, itemBitmap, this, itemMasks, null, capsule_sound, new Point(frameW/2,frameW/2), true);
			s = i.getSources().get(0).getS();
			s.setGain(2);
			break;
		default:
			break;
		}
		
		while (!this.positionFreeEntities(i)){
			x = NumberGenerator.nextInt(width);
			y = NumberGenerator.nextInt(height);
			i.setX(x); i.setY(y);
		}

		return i;
	}

}