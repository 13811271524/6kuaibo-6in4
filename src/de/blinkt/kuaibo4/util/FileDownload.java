package de.blinkt.kuaibo4.util;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ProgressBar;

import org.apache.http.HttpStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.blinkt.kuaibo4.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class FileDownload {
    private static Context sContext = null;
    private static String filePath;
    private static String currentVerionName;
    public static void checkUpdate(Context c)
    {
        try {
            sContext = c;
            int versionCode = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionCode;
            
            
            try {
	        	final URL  url = new URL("http://update.6kuaibo.com/mobile/update4/vpn.xml");
	        	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        	conn.setConnectTimeout(2000);
	        	conn.setReadTimeout(2000);
				conn.connect();
				InputStream is = conn.getInputStream();
				String currentVersion = getUserByXML(is);
				if(!currentVersion.equals(String.valueOf(versionCode))){
					AlertDialog.Builder adb = new AlertDialog.Builder(sContext);
	                adb.setTitle("Ӧ�ø���").setMessage("���п��õ��°汾�����Ƿ���Ҫ������"+currentVerionName+"?")
	                        .setPositiveButton("ȷ��",new DialogInterface.OnClickListener() {
	                            @Override
	                            public void onClick(DialogInterface dialogInterface, int i) {
	                            	MainActivity.showDownload();
	                            	CheckTask asynTask = new CheckTask(MainActivity.mDownload);
	                            	asynTask.execute(url);
	                            }
	                        })
	                        .setNegativeButton("ȡ��",null)
	                        .show();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public static String getUserByXML(InputStream is) throws Exception{
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbfactory.newDocumentBuilder();
		Document document = db.parse(is);//������ʹ��DOM����XML��׼���������̶�����·

		Element root = document.getDocumentElement();//��ȡ���ĵ�
		// ������ص�xml��login��ǩΪ1�Ļ�����ʼ����User������Userʼ��Ϊnull
		String currentVersion = root.getElementsByTagName("openvpnVersion")
					.item(0).getFirstChild().getNodeValue().toString();
		currentVerionName = root.getElementsByTagName("apk")
				.item(0).getFirstChild().getNodeValue().toString();
		NodeList items = root.getElementsByTagName("openvpn.apk");
		Element apkNode = (Element) items.item(0);
		filePath = apkNode.getAttribute("downloadPath").toString();
		return currentVersion;
	}

    static class CheckTask extends AsyncTask<URL,Integer, Long>
    {
    	private final ProgressDialog bar;  
        private int count = 0;  
      
        public CheckTask(ProgressDialog bar) {  
            super();  
            this.bar = bar;  
        }

        @Override
        protected Long doInBackground(URL... urls) {
        	
        	File NewFile = downLoadFile(filePath);
        	Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(NewFile),
                            "application/vnd.android.package-archive");
            sContext.startActivity(intent);
    		return null;
        	
        }
        
        @Override  
        protected void onProgressUpdate(Integer... values) {  
            count += values[0];  
            bar.setProgress(count);  
            super.onProgressUpdate(values);  
        }
        
        protected File downLoadFile(String httpUrl) {
            // TODO Auto-generated method stub
            final String fileName = "6kuaibo-test.apk";
            File sdcardDir =Environment.getExternalStorageDirectory();
            String path=sdcardDir.getPath()+"/6kuaibo/update4/";
            File path1 = new File(path);
            if (!path1.exists()) {
              path1.mkdir();
            }
            final File file = new File(path + fileName);
            try {
                    URL url = new URL(httpUrl);
                    try {
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            InputStream is = conn.getInputStream();
                            FileOutputStream fos = new FileOutputStream(file);
                            byte[] buf = new byte[256];
                            conn.connect();
                            bar.setMax(conn.getContentLength());
                            double count = 0;
                            if (conn.getResponseCode() >= 400) {
                                    //Toast.makeText(getBaseContext(), "���ӳ�ʱ", Toast.LENGTH_SHORT)
                                                    //.show();
                            } else {
                                    while (count <= 100) {
                                            if (is != null) {
                                                    int numRead = is.read(buf);
                                                    publishProgress(numRead);
                                                    if (numRead <= 0) {
                                                            break;
                                                    } else {
                                                            fos.write(buf, 0, numRead);
                                                    }
                                            } else {
                                                    break;
                                            }
                                    }
                            }
                            conn.disconnect();
                            fos.close();
                            is.close();
                    } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                    }
            } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
            return file;
        }        
    }
}
