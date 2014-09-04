package de.blinkt.kuaibo4;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.*;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import de.blinkt.kuaibo4.core.OpenVpnService;
import de.blinkt.kuaibo4.core.ProfileManager;
import de.blinkt.kuaibo4.core.VpnStatus;
import de.blinkt.kuaibo4.core.OpenVpnService.LocalBinder;
import de.blinkt.kuaibo4.core.VpnStatus.ConnectionStatus;
import de.blinkt.kuaibo4.core.VpnStatus.LogItem;
import de.blinkt.kuaibo4.core.VpnStatus.LogListener;
import de.blinkt.kuaibo4.core.VpnStatus.StateListener;
import de.blinkt.kuaibo4.R;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

public class LogWindow extends Activity implements StateListener  {
	private static final String LOGTIMEFORMAT = "logtimeformat";
	private static final int START_VPN_CONFIG = 0;
	private WebView  webView;
	protected OpenVpnService mService;
	private static Context mContext = null;
	private ServiceConnection mConnection = new ServiceConnection() {



		@Override
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mService =null;
		}

	};



	class LogWindowListAdapter implements ListAdapter, LogListener, Callback {

		private static final int MESSAGE_NEWLOG = 0;

		private static final int MESSAGE_CLEARLOG = 1;
		
		private static final int MESSAGE_NEWTS = 2;

		private Vector<LogItem> myEntries=new Vector<LogItem>();

		private Handler mHandler;

		private Vector<DataSetObserver> observers=new Vector<DataSetObserver>();

		private int mTimeFormat=0;


		public LogWindowListAdapter() {
			initLogBuffer();

			if (mHandler == null) {
				mHandler = new Handler(this);
			}

			VpnStatus.addLogListener(this);
		}



		private void initLogBuffer() {
			myEntries.clear();
            Collections.addAll(myEntries, VpnStatus.getlogbuffer());
		}

		String getLogStr() {
			String str = "";
			for(LogItem entry:myEntries) {
				str+=entry.getString(LogWindow.this) + '\n';
			}
			return str;
		}

		@Override
		public void registerDataSetObserver(DataSetObserver observer) {
			observers.add(observer);

		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
			observers.remove(observer);
		}

		@Override
		public int getCount() {
			return myEntries.size();
		}

		@Override
		public Object getItem(int position) {
			return myEntries.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView v;
			if(convertView==null)
				v = new TextView(getBaseContext());
			else
				v = (TextView) convertView;
			
			LogItem le = myEntries.get(position);
			String msg = le.getString(LogWindow.this);
			if (mTimeFormat != 0) {
				Date d = new Date(le.getLogtime());
				java.text.DateFormat timeformat;
				if (mTimeFormat==2) 
					timeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
				else
					timeformat = DateFormat.getTimeFormat(LogWindow.this);
				String time = timeformat.format(d);
				msg =  time + " " + msg;
			}
			v.setText(msg);
			return v;
		}

		@Override
		public int getItemViewType(int position) {
			return 0;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public boolean isEmpty() {
			return myEntries.isEmpty();

		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public boolean isEnabled(int position) {
			return true;
		}

		@Override
		public void newLog(LogItem logmessage) {
			Message msg = Message.obtain();
            assert (msg!=null);
			msg.what=MESSAGE_NEWLOG;
			Bundle mbundle=new Bundle();
			mbundle.putParcelable("logmessage", logmessage);
			msg.setData(mbundle);
			mHandler.sendMessage(msg);
		}

		@Override
		public boolean handleMessage(Message msg) {
			// We have been called
			if(msg.what==MESSAGE_NEWLOG) {

				LogItem logmessage = msg.getData().getParcelable("logmessage");
				myEntries.add(logmessage);

				for (DataSetObserver observer : observers) {
					observer.onChanged();
				}
			} else if (msg.what == MESSAGE_CLEARLOG) {
				initLogBuffer();
				for (DataSetObserver observer : observers) {
					observer.onInvalidated();
				}
			}  else if (msg.what == MESSAGE_NEWTS) {
				for (DataSetObserver observer : observers) {
					observer.onInvalidated();
				}
			}

			return true;
		}

		public void nextTimeFormat() {
			mTimeFormat= (mTimeFormat+ 1) % 3;
			mHandler.sendEmptyMessage(MESSAGE_NEWTS);
		}
		
	}



	private LogWindowListAdapter ladapter;

    private void showDisconnectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_cancel);
        builder.setMessage(R.string.cancel_connection_query);
        builder.setNegativeButton(android.R.string.no, null);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ProfileManager.setConntectedVpnProfileDisconnected(LogWindow.this);
                if (mService != null && mService.getManagement() != null)
                    mService.getManagement().stopVPN();
                finish();
            }
        });

        builder.show();
    }

    public static void NoNetwork(){
    	AlertDialog.Builder builder = new Builder(mContext);
    	builder.setTitle("网络已断开");
    	builder.setPositiveButton("断开连接",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });
        builder.setNegativeButton("等待网络恢复",
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.cancel){
            showDisconnectDialog();
            return true;
        }else if(item.getItemId()==R.id.home){
        	webView.loadUrl("http://www.6kuaibo.com/logMobile.php");
            return true;
        }
		return super.onOptionsItemSelected(item);

	}


    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.logmenu, menu);
		return true;
	}


	@Override
	protected void onResume() {
		super.onResume();
		VpnStatus.addStateListener(this);
        Intent intent = new Intent(this, OpenVpnService.class);
        intent.setAction(OpenVpnService.START_SERVICE);

        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


        if (getIntent() !=null && OpenVpnService.DISCONNECT_VPN.equals(getIntent().getAction()))
            showDisconnectDialog();

        setIntent(null);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == START_VPN_CONFIG && resultCode==RESULT_OK) {
			String configuredVPN = data.getStringExtra(VpnProfile.EXTRA_PROFILEUUID);

			final VpnProfile profile = ProfileManager.get(this,configuredVPN);
			ProfileManager.getInstance(this).saveProfile(this, profile);
			// Name could be modified, reset List adapter

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(R.string.configuration_changed);
			dialog.setMessage(R.string.restart_vpn_after_change);


			dialog.setPositiveButton(R.string.restart,
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(getBaseContext(), LaunchVPN.class);
					intent.putExtra(LaunchVPN.EXTRA_KEY, profile.getUUIDString());
					intent.setAction(Intent.ACTION_MAIN);
					startActivity(intent);
				}


			});
			dialog.setNegativeButton(R.string.ignore, null);
			dialog.create().show();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onStop() {
		super.onStop();
		VpnStatus.removeStateListener(this);
        unbindService(mConnection);
        getPreferences(0).edit().putInt(LOGTIMEFORMAT, ladapter.mTimeFormat).apply();

    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.logwindow);

		Intent intent=getIntent();
		boolean isBack = intent.getBooleanExtra("isBack", true);
		
		webView  = (WebView)  findViewById(R.id.webView);
		ladapter = new LogWindowListAdapter();
		ladapter.mTimeFormat = getPreferences(0).getInt(LOGTIMEFORMAT, 0);
		//lv.setAdapter(ladapter);

        mPdg = new ProgressDialog(this);
        mPdg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mPdg.setCancelable(false);
        mPdg.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //finish();
            }
        });
        mPdg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) mPdg.dismiss();
                return false;
            }
        });
        mPdg.show();
		//getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    ProgressDialog mPdg;

    @Override
	public void updateState(final String status,final String logmessage, final int resid, final ConnectionStatus level) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Log.i("Test_Auth", logmessage);
				String prefix=getString(resid) + " ";
				if (status.equals("BYTECOUNT") || status.equals("NOPROCESS") )
                {
					prefix="";
                    //mPdg.dismiss();
                }
				if (resid==R.string.unknown_state)
					prefix+=status;
                mPdg.setMessage(prefix + logmessage);
                if (logmessage.startsWith("SUCCESS") && mPdg.isShowing()){
					mPdg.dismiss();
					Success();
				}
                if(logmessage.startsWith("Auth")){	
					Intent intent = new Intent(getBaseContext(),LaunchVPN.class);
					intent.putExtra(LaunchVPN.EXTRA_NAME, "6kuaibo");
					intent.putExtra(LaunchVPN.EXTRA_AUTH, false);
					intent.setAction(Intent.ACTION_MAIN);
					startActivity(intent);
				}
                if(MainActivity.closeLog){
                	MainActivity.closeLog = false;
                	finish();
				}
			}
		});

	}
    
    @SuppressLint("SetJavaScriptEnabled")
	public void Success(){
        webView  = (WebView)  findViewById(R.id.webView);
        webView.loadUrl("http://www.6kuaibo.com/logMobile.php");
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);  
        webView.getSettings().setBuiltInZoomControls(true);
        
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
             view.loadUrl(url);   //在当前的webview中跳转到新的url
             return true;
            }
           });
        webView.setWebChromeClient(new WebChromeClient() {  
            public void onProgressChanged(WebView view, int progress) {  
                // Activity和Webview根据加载程度决定进度条的进度大小  
                // 当加载到100%的时候 进度条自动消失  
            	LogWindow.this.setProgress(progress * 100);  
            }  
        });
    }
    
    @Override 
    //设置回退  
    //覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法  
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
         if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {  
             webView.goBack(); //goBack()表示返回WebView的上一页面  
             return true;  
         }  
         return true;  
    } 

	@Override
	protected void onDestroy() {
		VpnStatus.removeLogListener(ladapter);
		super.onDestroy();
	}

}
