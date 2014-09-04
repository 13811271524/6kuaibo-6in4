package de.blinkt.kuaibo4;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.StrictMode;
import de.blinkt.kuaibo4.fragments.AboutFragment;
import de.blinkt.kuaibo4.fragments.VPNProfileList;
import de.blinkt.kuaibo4.util.FileDownload;
import de.blinkt.kuaibo4.util.UpdateAgent;
import de.blinkt.kuaibo4.R;


public class MainActivity extends Activity {

	public static boolean closeLog = false;
	private static Context mContext = null;
	public static ProgressDialog mDownload;

	protected void onCreate(android.os.Bundle savedInstanceState) {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath().build());

		super.onCreate(savedInstanceState);
		
		mContext = this;
		
		ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		//bar.hide();

		Tab vpnListTab = bar.newTab().setText("��ʼ6kuaibo");
		Tab abouttab = bar.newTab().setText(R.string.about);

		vpnListTab.setTabListener(new TabListener<VPNProfileList>("profiles", VPNProfileList.class));
		abouttab.setTabListener(new TabListener<AboutFragment>("about", AboutFragment.class));
		
		bar.addTab(vpnListTab);
		bar.addTab(abouttab);
		
		
		createSDCardDir();
		File sdcardDir =Environment.getExternalStorageDirectory();
        //�õ�һ��·����������sdcard���ļ���·��������
          String path_o=sdcardDir.getPath()+"/6kuaibo/6kuaibo-6in4.ovpn";
          File path1 = new File(path_o);
        try {
			InputStream ovpn = getAssets().open("6kuaibo-6in4.ovpn");
			copyFile(ovpn,path1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        FileDownload.checkUpdate(this);
	}
	
	public static void showDownload(){
		
		mDownload = new ProgressDialog(mContext);
		mDownload.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mDownload.setIndeterminate(false);
		mDownload.setTitle("������");
		mDownload.show();
	}
	
	public static void copyFile(InputStream in, File target) {
		  // �������������
		  OutputStream out = null;
		  // ���������ֽ���
		  BufferedInputStream bin = null;
		  BufferedOutputStream bout = null;
		  try {
			  // ����ʵ��
			  out = new FileOutputStream(target);
			  bin = new BufferedInputStream(in);
			  bout = new BufferedOutputStream(out);

			  byte[] b = new byte[8192];// ���ڻ�����ֽ�����
			  int len = bin.read(b);// ��ȡ��ȡ���ĳ���
			  while (len != -1)// �ж��Ƿ��ȡ��β��
			  {
				  bout.write(b, 0, len);
				  len = bin.read(b);
			  }
		  } catch (FileNotFoundException e) {
			  e.printStackTrace();
		  } catch (IOException e) {
			  e.printStackTrace();
		  } finally {
			  try {
				  if (bin != null) {
					  bin.close();
				  }
				  if (bout != null) {
					  bout.close();
				  }
			  } catch (IOException e) {
				  e.printStackTrace();
			  }
		  }

	  }
	
	public void createSDCardDir(){
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
               // ����һ���ļ��ж��󣬸�ֵΪ�ⲿ�洢����Ŀ¼
                File sdcardDir =Environment.getExternalStorageDirectory();
              //�õ�һ��·����������sdcard���ļ���·��������
                String path=sdcardDir.getPath()+"/6kuaibo";
                File path1 = new File(path);
               if (!path1.exists()) {
                //�������ڣ�����Ŀ¼��������Ӧ��������ʱ�򴴽�
                path1.mkdir();
              }
               }
        else{
         setTitle("false");
         return;
       }
       }


	protected class TabListener<T extends Fragment> implements ActionBar.TabListener
	{
		private Fragment mFragment;
		private String mTag;
		private Class<T> mClass;


        public TabListener(String tag, Class<T> clz) {
            mTag = tag;
            mClass = clz;

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = getFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
        }
      
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(MainActivity.this, mClass.getName());
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            }
        }


		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		System.out.println(data);


	}


}