package com.mica;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Environment;

public class PersPosUtils {
	
	public static String LOGFILENAME = "";
	
	public static void generate_file_name() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
		String formattedDate = df.format(c.getTime());
		LOGFILENAME = "MICA_route0" + "_" +  formattedDate + ".xml";	
	}

	public static void clearLog() {
		if (!PersPos.started) return;
		String content = PersPos.app.StatusTxt.getText().toString();
		PersPos.app.StatusTxt.setText(content.substring(3*content.length()/5));
	}

	public static void showLog(String msg) {
		//if (!PersPos.started) return;
		String content = PersPos.app.StatusTxt.getText().toString();
		if (content.split(System.getProperty("line.separator")).length > 10) clearLog();
		PersPos.app.StatusTxt.append(getTime() + ": "+ msg +"\n");
	}
	
	public static void showWarning(String msg) {
		//		Toast.makeText(PersPos.app.getApplicationContext(), msg, Toast.LENGTH_SHORT).show(c);
		showLog(msg);
	}

	public static boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager)PersPos.app.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivityManager.getActiveNetworkInfo() != null;
	}


	public static boolean checkExternalStorage() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		} else {
			PersPosUtils.showLog("Cannot open External Storage Data");
			return false;
		}
	}

	public static void writeOfflineLog(String paramValue) 
	{
		//if (!checkExternalStorage()) return;

		try {
			String root = Environment.getExternalStorageDirectory().toString();

			File path = new File(root + "/Android/data/com.mica.perspos");
			//path.mkdir();

			File file = new File(path, LOGFILENAME);    
			if (!file.exists()) file.createNewFile();

			FileOutputStream os = new FileOutputStream(file, true);
			os.write((paramValue + "\r\n").getBytes());
			os.close();
		} catch (IOException e) {
			showLog("IO external storage error: " + e);
		}
	}

	public static void clearOfflineLog() 
	{
		if (!checkExternalStorage()) return;
	}

	// lay tu ben setting ve
	public static String getURL(String fname) {
		if (Settings.serverAddress.length() == 0)
			return null;

		String serverAddress = Settings.serverAddress;
		StringBuilder sb = new StringBuilder();
		sb.append("http://");
		sb.append(serverAddress);
		if (serverAddress.charAt(serverAddress.length() - 1) != '/')
			sb.append('/');
		sb.append(fname);
		return sb.toString();
	}

	public static void sendScanInfo(String paramValue) {
		PersPosUtils.showWarning("Server not set");
		return;

	}

	public static String getTime() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
		String formattedDate = df.format(c.getTime());
		return formattedDate;
	}

	//
	// Sending time stamp to server
	//
	public static void sendTimeInfo(String wifiAddress, String btAddress) {
		PersPosUtils.showWarning("Server not set");
	}

	//
	// Send the log data file to the server
	// 
	public static int uploadFile() {
		PersPosUtils.showLog("Upload File to server...");
		return 0;
	}


	public static String sendFileToServer(String targetUrl) {
	    String response = "error";
	    //Log.e("Image filename", filename);
	    //Log.e("url", targetUrl);
	    HttpURLConnection connection = null;
	    DataOutputStream outputStream = null;
	    // DataInputStream inputStream = null;

		String root = Environment.getExternalStorageDirectory().toString();
		String filename = root + "/Android/data/com.mica.perspos/" + LOGFILENAME;	    
		String pathToOurFile = filename;
	    String urlServer = targetUrl;
	    String lineEnd = "\r\n";
	    String twoHyphens = "--";
	    String boundary = "*****";
	    DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss", Locale.US);

	    int bytesRead, bytesAvailable, bufferSize;
	    byte[] buffer;
	    int maxBufferSize = 1 * 1024;
	    try {
	        FileInputStream fileInputStream = new FileInputStream(new File(
	                pathToOurFile));

	        URL url = new URL(urlServer);
	        connection = (HttpURLConnection) url.openConnection();

	        // Allow Inputs & Outputs
	        connection.setDoInput(true);
	        connection.setDoOutput(true);
	        connection.setUseCaches(false);
	        connection.setChunkedStreamingMode(1024);
	        // Enable POST method
	        connection.setRequestMethod("POST");

	        connection.setRequestProperty("Connection", "Keep-Alive");
	        connection.setRequestProperty("Content-Type",
	                "multipart/form-data;boundary=" + boundary);

	        outputStream = new DataOutputStream(connection.getOutputStream());
	        outputStream.writeBytes(twoHyphens + boundary + lineEnd);

	        String connstr = null;
	        connstr = "Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
	                + pathToOurFile + "\"" + lineEnd;
	        //Log.i("Connstr", connstr);

	        outputStream.writeBytes(connstr);
	        outputStream.writeBytes(lineEnd);

	        bytesAvailable = fileInputStream.available();
	        bufferSize = Math.min(bytesAvailable, maxBufferSize);
	        buffer = new byte[bufferSize];

	        // Read file
	        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
	        //Log.e("Image length", bytesAvailable + "");
	        try {
	            while (bytesRead > 0) {
	                try {
	                    outputStream.write(buffer, 0, bufferSize);
	                } catch (OutOfMemoryError e) {
	                    e.printStackTrace();
	                    response = "outofmemoryerror";
	                    fileInputStream.close();
	                    return response;
	                }
	                bytesAvailable = fileInputStream.available();
	                bufferSize = Math.min(bytesAvailable, maxBufferSize);
	                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            response = "error";
                fileInputStream.close();
	            return response;
	        }
	        outputStream.writeBytes(lineEnd);
	        outputStream.writeBytes(twoHyphens + boundary + twoHyphens
	                + lineEnd);

	        // Responses from the server (code and message)
	        int serverResponseCode = connection.getResponseCode();
	        String serverResponseMessage = connection.getResponseMessage();
	        //Log.i("Server Response Code ", "" + serverResponseCode);
	        //Log.i("Server Response Message", serverResponseMessage);

	        if (serverResponseCode == 200) {
	            response = "true";
	            //showLog("200");
	        }


	        String CDate = null;
	        Date serverTime = new Date(connection.getDate());
	        try {
	            CDate = df.format(serverTime);
	        } catch (Exception e) {
	            e.printStackTrace();
	            //Log.e("Date Exception", e.getMessage() + " Parse Exception");
	        }
	        //Log.i("Server Response Time", CDate + "");

	        filename = CDate
	                + filename.substring(filename.lastIndexOf("."),
	                        filename.length());
	        //Log.i("File Name in Server : ", filename);
	        
			if(serverResponseCode == 200){
				///PersPosUtils.showLog("Upload Successful!");
				//PersPosUtils.showLog(serverResponseMessage);
				InputStream input = connection.getInputStream();
				Scanner sc = new Scanner(input);
				String response_text;
				while (sc.hasNext()) {
					response_text = sc.next();
					//showLog(response_text);
				}

				sc.close();
				//showLog("\n\n\n\n\n");
			}    

	        
	        fileInputStream.close();
	        outputStream.flush();
	        outputStream.close();
	        outputStream = null;
	    } catch (Exception ex) {
	        // Exception handling
	        response = "error";
	        //Log.e("Send file Exception", ex.getMessage() + "");
	        ex.printStackTrace();
	        showLog(ex.toString());
	    }
	    return response;
	}
	
	public static void printStartTime() {
		showLog("Start Recording");
	}
	
	public static void updateTime(long time) {
		long tmp = time/100;
		String content = tmp/10 + "." + tmp%10 + " seconds";
		
		PersPos.app.elapsedTxt.setText(content);		
	}
	
	public static void updateWifitxt(int cnt) {
		PersPos.app.countWifiTxt.setText(cnt + " packages");
	}
	
	public static void updateGPStxt(int cnt) {
		PersPos.app.countGPSTxt.setText(cnt + " packages");
	}
}
