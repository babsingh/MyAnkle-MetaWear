package com.ece1778.project.myAnkle.Helpers;

import com.ece1778.project.myAnkleUser.BuildConfig;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class PrefUtils {
	public static final String ANKLE_SIDE_KEY = "ankleSide";
	public static final String EYE_STATE_KEY = "eyeState";
	public static final String LOCAL_USER_ID = "local_user_id";
	public static final String SERVER_ID = "server_id";
	public static final String OPT_OUT_KEY = "opt_out";
	public static final String FIRST_LAUNCH_KEY = "first_launch";
	
	public static final String DEBUG_RESEARCHER_NAME = "researcher_name";
	public static final String DEBUG_BODY_PART_TESTED = "body_part";
	
	
	public static void setStringPreference(Context c, String key, String value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);        
	    SharedPreferences.Editor editor = prefs.edit();	    
	    editor.putString(key, value);
	    editor.commit(); 
	}
	
	public static String getStringPreference(Context c, String key) {
		String value = "";
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
	    value = prefs.getString(key, null);	    
	    return value;
	}
	
	public static void setIntPreference(Context c, String key, int value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);        
	    SharedPreferences.Editor editor = prefs.edit();	    
	    editor.putInt(key, value);
	    editor.commit();
	}
	
	public static int getIntPreference(Context c, String key) {
		int value = -1;
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
	    value = prefs.getInt(key, -1);	    
	    return value;
	}
	
	public static void setDefaultPreferences(Context c) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putInt(LOCAL_USER_ID, -1);
		editor.putInt(SERVER_ID, -1);
		editor.putString(ANKLE_SIDE_KEY, null);
		editor.putString(EYE_STATE_KEY, null);
		
		editor.commit();
	}
	
	// these preferences were set automatically from a preference.xml file
	public static int getCountdown(Context c) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		if(BuildConfig.DEBUG) Log.e("PrefUtils getCountdown", String.valueOf(prefs.getString("prefCountdownDuration", "failed")));
		return Integer.parseInt(prefs.getString("prefCountdownDuration", "15"));
	}

	public static int getDuration(Context c) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		return Integer.parseInt(prefs.getString("prefExerciseDuration", "30"));
	}
	
	public static boolean getPingCheckBox(Context c) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		return prefs.getBoolean("prefPing", true);
	}
	
	public static boolean getVibrateCheckBox(Context c) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		
		return prefs.getBoolean("prefVibrate", true);
	}
}