package de.blinkt.kuaibo4;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ExecCommandWithHandler {
	private Handler handler;
	private String cmd;
	private String result;
	private Process proc;
	private int notifyNum;
	private boolean notifyEachChar;
	private int exitValue;
	private volatile boolean stop;
	
	/**
	 * Execute a Linux command with root permission, if handler is provided, 
	 * notify handler after execution or after each character based on the 
	 * boolean provided.
	 * @param command The command to be executed.
	 * @param notifyHandler The handler to be notified, or null.
	 * @param num The message number to be passed to the handler.
	 * @param notifyEachCharacter If true, notify handler after each character when handler is not null.
	 */
	public ExecCommandWithHandler(String command, Handler notifyHandler, int num, boolean notifyEachCharacter) {
		cmd = command;
		handler = notifyHandler;
		notifyNum = num;
		notifyEachChar = notifyEachCharacter;
		stop = false;
	}
	
	public String getResult() {
		if (handler == null) {
        	try {
        		Log.d("getResult: ExecCommandWithHandler", "No handler: wait for execution terminate.");
        		exitValue = proc.waitFor();  //Cause the calling thread to wait for the native thread "proc" to finish execution.
	        } catch (InterruptedException e) {
	        	Log.e("getResult: ExecCommandWithHandler", "InterruptedException: proc has not terminated.");
	        	exitValue = -1;
	        }
        }
		Log.d("getResult: ExecCommandWithHandler","-------"+cmd+" = \n"+result);
		return result;
	}
	
	public void stopExecution() {
		stop = true;
	}
	
	public int getExitValue() {
		if (handler == null) {
        	try {
        		Log.e("getExitValue: ExecCommandWithHandler", "No handler: wait for execution terminate.");
        		exitValue = proc.waitFor();  //Cause the calling thread to wait for the native thread "proc" to finish execution.
	        } catch (InterruptedException e) {
	        	Log.e("getExitValue: ExecCommandWithHandler", "InterruptedException: proc has not terminated.");
	        	exitValue = -1;
	        }
        }
		Log.e("getExitValue: ExecCommandWithHandler","-------"+cmd+" = "+exitValue);
		return exitValue;
	}
	
	public void execute() {
		Runtime runtime = Runtime.getRuntime();
		
		try {
			proc = runtime.exec("su");  //Process proc has got root privilege
			DataOutputStream os = new DataOutputStream(proc.getOutputStream());
			os.writeBytes(cmd + "\n");  //Use "\n" to flag the end of command
			os.writeBytes("exit\n");
			os.flush();

	        //Use InputStreamReader to obtain console output
	        InputStream is = proc.getInputStream();
	        InputStreamReader inputStreamReader = new InputStreamReader(is);
	        BufferedReader inputReader = new BufferedReader(inputStreamReader);
	        
	        //Use InputStreamReader to obtain console error message
	        InputStream es = proc.getErrorStream();
	        InputStreamReader errorStreamReader = new InputStreamReader(es);
	        BufferedReader errorReader = new BufferedReader(errorStreamReader);
	        
	        // read the output
	        char c;
	        int i;
	        StringBuilder sb = new StringBuilder("");
	        while (!stop && ((i = inputReader.read()) != -1)) {
	        	c = (char)i;
	            sb.append(c);
	            result = sb.toString(); 
	            if (notifyEachChar && (handler != null)) {
		            Message msg = new Message();
			        msg.what = notifyNum;
		            handler.sendMessage(msg);
	            }
	        }
	        while (!stop && ((i = errorReader.read()) != -1)) {
	        	c = (char)i;
	            sb.append(c);
	            result = sb.toString();	
	            if (notifyEachChar && (handler != null)) {
		            Message msg = new Message();
			        msg.what = notifyNum;
		            handler.sendMessage(msg);
	            }
	        }
	        
	        if (!notifyEachChar && (handler != null)) {
	            Message msg = new Message();
		        msg.what = notifyNum;
	            handler.sendMessage(msg);
	        }
	        
	        is.close();
	        es.close();
	        os.close();
		} catch (IOException e1) {
			Log.e("ExecCommandWithHandler: IOException", e1.toString());
		}
	}
}
