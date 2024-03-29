package com.minesweeper;

import org.example.minesweeper.TTS;
import org.example.others.Log;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * @author Gloria Pozuelo, Gonzalo Benito and Javier �lvarez
 * This class implements the preferences activity, where you can choose whether disable or enable sounds
 */

public class PrefsActivity extends PreferenceActivity implements OnPreferenceClickListener {
	private static String TAG = "SettingsMenu";
	
	private CheckBoxPreference music, contextCell;
	
	// Option names and default values
	private static final String OPT_MUSIC = "music";
	private static final boolean OPT_MUSIC_DEF = false;
	private static final String OPT_COORDINATES = "context";
	private static final boolean OPT_COORDINATES_DEF = true;

	
	private TTS textToSpeech;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		// Get the checkbox preference
		music = (CheckBoxPreference) findPreference(OPT_MUSIC);
		music.setOnPreferenceClickListener(this);

		contextCell = (CheckBoxPreference) findPreference(OPT_COORDINATES);
		contextCell.setOnPreferenceClickListener(this);
		
		// Initialize TTS engine
		textToSpeech = (TTS) getIntent().getParcelableExtra(MinesweeperActivity.KEY_TTS);
		textToSpeech.setContext(this);
		textToSpeech.setInitialSpeech(getString(R.string.settings_menu_initial_TTStext));
	
		Log.getLog().addEntry(PrefsActivity.TAG,
				configurationToString(this),
				Log.NONE,
				Thread.currentThread().getStackTrace()[2].getMethodName(),
				"Changing actual configuration");
	}

	/**
	 *  Turns off TTS engine
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	    textToSpeech.stop();
	}
	
	/** Get the current value of the music option */
	public static boolean getMusic(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(OPT_MUSIC, OPT_MUSIC_DEF);
	}

	/** Get the current value of the tts option */
	public static boolean getCoordinates(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(OPT_COORDINATES, OPT_COORDINATES_DEF);
	}
	
	public static String configurationToString(Context context){
		return	"Music: " + PrefsActivity.getMusic(context) + "/" +
				" Context Coordinates: "+ PrefsActivity.getCoordinates(context);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (OPT_MUSIC.equals(preference.getKey())) {
			textToSpeech.speak(findPreference(OPT_MUSIC).toString()
					+ music.isChecked());
		} else if (OPT_COORDINATES.equals(preference.getKey())) 
			textToSpeech.speak(findPreference(OPT_COORDINATES).toString()
					+ contextCell.isChecked());
		return true;
	}
}