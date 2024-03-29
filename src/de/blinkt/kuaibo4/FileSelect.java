package de.blinkt.kuaibo4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class FileSelect extends Activity {
	public static final String RESULT_DATA = "RESULT_PATH";
	public static final String START_DATA = "START_DATA";
	public static final String WINDOW_TITLE = "WINDOW_TILE";
	public static final String NO_INLINE_SELECTION = "de.blinkt.openvpn.NO_INLINE_SELECTION";
	public static final String SHOW_CLEAR_BUTTON = "de.blinkt.openvpn.SHOW_CLEAR_BUTTON";
	public static final String DO_BASE64_ENCODE = "de.blinkt.openvpn.BASE64ENCODE";
	
		
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState); 
		
		Intent intent = new Intent();
		
		intent.putExtra(RESULT_DATA,"/mnt/sdcard/6kuaibo/6kuaibo-6in4.ovpn");
		setResult(Activity.RESULT_OK,intent);
		finish();
	}
}
