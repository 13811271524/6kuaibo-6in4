package de.blinkt.kuaibo4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.blinkt.kuaibo4.core.ProfileManager;
import de.blinkt.kuaibo4.core.VPNLaunchHelper;
import de.blinkt.kuaibo4.core.VpnStatus;
import de.blinkt.kuaibo4.core.VpnStatus.ConnectionStatus;
import de.blinkt.kuaibo4.R;

public class LaunchVPN extends Activity {

	public static final String EXTRA_KEY = "de.blinkt.openvpn.shortcutProfileUUID";
	public static final String EXTRA_NAME = "de.blinkt.openvpn.shortcutProfileName";
	public static final String EXTRA_HIDELOG =  "de.blinkt.openvpn.showNoLogWindow";
	public static final String EXTRA_AUTH = "auth";

	private static final int START_VPN_PROFILE= 70;
	private static final int LOGIN_VPN_PROFILE= 71;
	public String msg_receive=null;
	public static String port;
	public static String address;


	private ProfileManager mPM;
	private VpnProfile mSelectedProfile;
	private boolean mhideLog=false;
	private boolean mnetstate=false;

	private boolean mCmfixed=false;
	

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.launchwindow);
		
		try{
			DatagramSocket socket = new DatagramSocket(4567);
			InetAddress serverAddress = (InetAddress) InetAddress.getByName("219.234.136.133");
			String str = "hello";
			byte data[] = str.getBytes();
			byte data2[] = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data,data.length,serverAddress,2014);
			DatagramPacket packet2 = new DatagramPacket(data2,data2.length);
			socket.send(packet);
			socket.setSoTimeout(1000);
			while(true){        // Receive data until timeout
	            try {
	            	socket.receive(packet2);
	    			msg_receive = new String(packet2.getData(),packet2.getOffset(),packet2.getLength());
	    			address = msg_receive.substring(0, msg_receive.indexOf(" "));
	    			port = msg_receive.substring(msg_receive.lastIndexOf(" ")+1);
	                System.out.println(packet2);
	                socket.close();
	            }
	            catch (SocketTimeoutException e) {
	                // timeout exception.
	                System.out.println("Timeout reached!!! " + e);
	                socket.close();
	                address = "219.234.136.131";
	                port = "1194";
	            }
	        }	
		}catch(Exception e){
			e.printStackTrace();
		}
		
		mPM =ProfileManager.getInstance(this);
	}	

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.serverConfig){
			new AlertDialog.Builder(this).setTitle("6kuaibo服务器").setMessage("ipv6地址:"+address+"\n\n"+"端口号:"+port).show();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.configmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	protected void onStart() {
		super.onStart();
		
		mnetstate = true;
		
		// Resolve the intent
		final Intent intent = getIntent();
		final String action = intent.getAction();


		if(Intent.ACTION_MAIN.equals(action)) {
			// we got called to be the starting point, most likely a shortcut
			String shortcutUUID = intent.getStringExtra( EXTRA_KEY);
			String shortcutName = intent.getStringExtra( EXTRA_NAME);
			boolean auth = intent.getBooleanExtra(EXTRA_AUTH, true);
			if (!auth)
				new AlertDialog.Builder(this).setTitle("验证错误").setMessage("用户名或密码出错").show();
			mhideLog = intent.getBooleanExtra(EXTRA_HIDELOG, false);

			VpnProfile profileToConnect = ProfileManager.get(this,shortcutUUID);
			if(shortcutName != null && profileToConnect ==null)
				profileToConnect = ProfileManager.getInstance(this).getProfileByName(shortcutName);

			mSelectedProfile = profileToConnect; 
			address = mSelectedProfile.mServerName;
			port = mSelectedProfile.mPort;
			launchVPN();

		}
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		super.onDestroy();
	}

	private void askForPW() {
		

        final Button btn_connect = (Button)findViewById(R.id.ok_connect);
        final Button btn_register = (Button)findViewById(R.id.ok_register);
        btn_register.setOnClickListener(new Button.OnClickListener(){//创建监听    
        	public void onClick(View v) {    
        		Uri uri = Uri.parse("http://www.6kuaibo.com/6kuaibo/register.php");  
        	    startActivity(new Intent(Intent.ACTION_VIEW,uri)); 
        		}    
        	});
        final EditText edit_username = (EditText)findViewById(R.id.username);
        if(mSelectedProfile.mUsername != null)
        	edit_username.setText(mSelectedProfile.mUsername);
        final EditText edit_pw = (EditText)findViewById(R.id.password);
        
        final CheckBox edit_save = (CheckBox)findViewById(R.id.save_password);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String temp_user = prefs.getString("username", "");
        String temp_pw = prefs.getString("password", "");
		boolean checksave = prefs.getBoolean("checksave", false);
		edit_username.setText(temp_user);
		if (checksave){
			edit_save.setChecked(true);
			edit_pw.setText(temp_pw);
		}
        
        btn_connect.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (!mnetstate){
					new AlertDialog.Builder(LaunchVPN.this).setTitle("网络错误").setMessage("网络连接已断开").setPositiveButton("确定", 
							new android.content.DialogInterface.OnClickListener() {
	                    		public void onClick(DialogInterface dialog, int which) {
	                    			dialog.dismiss();
	                    		}
	                		}).show();
				}
				else{
					if (edit_save.isChecked()){
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
						SharedPreferences.Editor editor = prefs.edit();
						editor.putString("username", edit_username.getText().toString());
						editor.putString("password", edit_pw.getText().toString());
						editor.putBoolean("checksave", true);
						editor.commit();
					}
					else{
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
						SharedPreferences.Editor editor = prefs.edit();
						editor.putString("username", edit_username.getText().toString());
						editor.putBoolean("checksave", false);
						editor.commit();
					}
	                	mSelectedProfile.mPort = port;
	                	mSelectedProfile.mServerName = address;
	                    mSelectedProfile.mUsername = edit_username.getText().toString();
	
	                    String pw = edit_pw.getText().toString() + "op";//sunshine
	                    VpnStatus.logInfo("Password:" + pw);
	                    if (edit_save.isChecked()) {
	                         mSelectedProfile.mPassword=pw;
	                    } else {
	                        mSelectedProfile.mTransientPW = pw;
	                    }
	                onActivityResult(LOGIN_VPN_PROFILE, Activity.RESULT_OK, null);
				}
			}
        });

	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode==START_VPN_PROFILE) {
			if(resultCode == Activity.RESULT_OK) {
				//int needpw = mSelectedProfile.needUserPWInput();
				askForPW();
			}
			
			else if (resultCode == Activity.RESULT_CANCELED) {
				// User does not want us to start, so we just vanish
				VpnStatus.updateStateString("USER_VPN_PERMISSION_CANCELLED", "", R.string.state_user_vpn_permission_cancelled,
                        ConnectionStatus.LEVEL_NOTCONNECTED);

				finish();
			}
		}
		else if (requestCode == LOGIN_VPN_PROFILE){//sunshine
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);        
			boolean showlogwindow = prefs.getBoolean("showlogwindow", true);

			if(!mhideLog && showlogwindow)
				showLogWindow();
			new startOpenVpnThread().start();
		} 
	}
	
	ProgressDialog mPdgLaunch;
	void showLogWindow() {
		Intent startLW = new Intent(getBaseContext(),LogWindow.class);
		startLW.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startLW.putExtra("isBack", true);
		startActivity(startLW);
	}

	void showConfigErrorDialog(int vpnok) {
		AlertDialog.Builder d = new AlertDialog.Builder(this);
		d.setTitle(R.string.config_error_found);
		d.setMessage(vpnok);
		d.setPositiveButton(android.R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();

			}
		});
		d.show();
	}

	void launchVPN () {
		int vpnok = mSelectedProfile.checkProfile(this);
		if(vpnok!= R.string.no_error_found) {
			showConfigErrorDialog(vpnok);
			return;
		}

		Intent intent = VpnService.prepare(this);
		// Check if we want to fix /dev/tun
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);        
		boolean usecm9fix = prefs.getBoolean("useCM9Fix", false);
		boolean loadTunModule = prefs.getBoolean("loadTunModule", false);

		if(loadTunModule)
			execeuteSUcmd("insmod /system/lib/modules/tun.ko");

		if(usecm9fix && !mCmfixed ) {
			execeuteSUcmd("chown system /dev/tun");
		}


		if (intent != null) {
			VpnStatus.updateStateString("USER_VPN_PERMISSION", "", R.string.state_user_vpn_password,
                    ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);
			// Start the query
			try {
				startActivityForResult(intent, START_VPN_PROFILE);
			} catch (ActivityNotFoundException ane) {
				// Shame on you Sony! At least one user reported that 
				// an official Sony Xperia Arc S image triggers this exception
				VpnStatus.logError(R.string.no_vpn_support_image);
				Toast.makeText(this, "vpn error: "+ane.getMessage(), Toast.LENGTH_SHORT).show();
			}
		} else {
			onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null);
		}

	}

	private void execeuteSUcmd(String command) {
		ProcessBuilder pb = new ProcessBuilder("su","-c",command);
		try {
			Process p = pb.start();
			int ret = p.waitFor();
			if(ret ==0)
				mCmfixed=true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class startOpenVpnThread extends Thread {
		@Override
		public void run() {
			VPNLaunchHelper.startOpenVpn(mSelectedProfile, getBaseContext());
			finish();
		}
	}


}
