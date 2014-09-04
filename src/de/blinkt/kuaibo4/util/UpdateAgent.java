package de.blinkt.kuaibo4.util;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.blinkt.kuaibo4.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class UpdateAgent {
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
	                adb.setTitle("应用更新").setMessage("已有可用的新版本，您是否需要升级到"+currentVerionName+"?")
	                        .setPositiveButton("确定",new DialogInterface.OnClickListener() {
	                            @Override
	                            public void onClick(DialogInterface dialogInterface, int i) {
	                            	MainActivity.showDownload();
	                            	new CheckTask().execute(url);
	                            }
	                        })
	                        .setNegativeButton("取消",null)
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
		Document document = db.parse(is);//上面是使用DOM解析XML的准备工作，固定的套路

		Element root = document.getDocumentElement();//获取根文档
		// 如果返回的xml中login标签为1的话，则开始构造User，否则User始终为null
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

        @Override
        protected Long doInBackground(URL... urls) {
        	
        	File NewFile = downLoadFile("http://update.6kuaibo.com/mobile/update4/apk/test-download.apk");
        	Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(NewFile),
                            "application/vnd.android.package-archive");
            sContext.startActivity(intent);
          
            return null;
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
                            double count = 0;
                            if (conn.getResponseCode() >= 400) {
                                    //Toast.makeText(getBaseContext(), "连接超时", Toast.LENGTH_SHORT)
                                                    //.show();
                            } else {
                                    while (count <= 100) {
                                            if (is != null) {
                                                    int numRead = is.read(buf);
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
