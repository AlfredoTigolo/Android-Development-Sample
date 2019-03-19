package com.tigolocrwriter.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.*;

import com.atigolocrwriter.data.Equipment;
import com.atigolocrwriter.util.ParseJSON;
import com.southbayaacrwriter.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView; 


public class LoginInActivity extends Activity {
	private Thread parseLoginThread;
	private static ImageView imageView;
	private static TextView tv =null;
	private static ProgressDialog dialog = null;
	private static String data;
	private static boolean taskComplete = false; 
	private static Handler handler;
	private static Equipment eq = null;
	private static boolean Authenticate = false;
	private static EditText ETUserName = null;
	private static EditText ETPassword = null;
	private static String UserName = null;
	private static String Password = null;
	private static boolean threadCompleted = false;
	private static boolean authenticated = false;
	private static EditText PostBack=null;
	private static String URL = "http://10.0.0.27/CRWebServices/ConditionReportService.svc/CredentialsValid?";
	 
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		PostBack = (EditText) findViewById(R.id.txtUserName); 
		handler = new Handler();
		
		if (parseLoginThread != null && parseLoginThread.isAlive()) {
			dialog = ProgressDialog.show(this, "Logging In", "Please wait...");
		}else
		{
			//showDialog(R.string.about_menu, "Main Thread:" + String.valueOf(authenticated), this);
		}
		
		 
		
	} 
	
	public void parseLoginData(View view) {
		Context c = this;
		
		dialog = ProgressDialog.show(this, "Logging In", "Pease wait...");
		
		parseLoginThread = new MyThread(c);

		parseLoginThread.start();
		//dialog.dismiss();
		
		
	}
	
	static public class MyThread extends Thread {
		private Context ctx;
	
		public MyThread(Context c) {
			// TODO Auto-generated constructor stub
			this.ctx = c;
			//tv.setText("bit me!");
			//showDialog(R.string.about_menu, UserName + ";" + Password, ctx);
			
		}

		@Override
		public void run() {
			String readTwitterFeed = readLoginInfo(ctx);
			
			try {
				JSONObject jsonObj = new JSONObject(readTwitterFeed);
				int VehicleId = -1;
				authenticated = jsonObj.getBoolean("d");
				//authenticated =Boolean.parseBoolean(jsonObj.getJSONObject("d").toString());
				taskComplete = true;
				Message msg = handler.obtainMessage(); 
				msg.obj =  jsonObj;
				handler.sendMessage(msg); 

 
				//handler.sendEmptyMessage(R.string.Data_Sent_Succeed); 
			
				handler.post(new MyRunnable());
				handler.notify();
			} catch (Exception e) {
				//tv.setText("error:"+e.toString()); 
				e.printStackTrace(); 
			} 
			
			
			//Toast.makeText(ctx, data, 1).show(); 
		}
		 
		 
		}
	@Override
	public Object onRetainNonConfigurationInstance() {
		return parseLoginThread;
	}
	static public class MyRunnable implements Runnable {
		synchronized void proceed() { notify(); }   
		synchronized void pause()   { try {
			wait(50000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   } 
		public void run() {
			//tv.setText(data);
			//authenticated = true;
			
			//showDialog(R.string.about_menu, String.valueOf(authenticated));
			
			taskComplete = true;
			//PostBack.setText(String.valueOf(taskComplete));
			dialog.dismiss();
		}

	
	}
	private void syncronized(MyRunnable myRunnable) {
			// TODO Auto-generated method stub
			
		}
	public static String readLoginInfo(Context c) {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		String  FullURL = URL + "UserName="+UserName +"&Password="+Password; 
		
		HttpGet httpGet = new HttpGet(FullURL);
		
		//showDialog2(R.string.validation_Error, "Running Thread...with this-->"+FullURL );
		
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				//String data = EntityUtils.toString(entity);
				//JSONArray timeline = new JSONArray(data);
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				
				String line;
				Context ctx = null;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
					
					//dialog = ProgressDialog.show(ctx, "Download", "downloading");
					//dialog.dismiss();
				}
			} else { 
				Log.e(LoginInActivity.class.toString(), "Failed to authenticate user");
				//showDialog(R.string.about_menu, "Failed to  authenticate user!", c);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}
	public void onLoginButtonClick(View view) {
		
		
		ETUserName = (EditText)findViewById(R.id.txtUserName); 
		ETPassword = (EditText)findViewById(R.id.txtPassword); 
		
		if(ETUserName.getText().toString().equals("") || ETPassword.getText().toString().equals("")){
			showDialog(R.string.validation_Error, "Please enter UserName/Password!", this);
		}
		else
		{
			
			UserName = ETUserName.getText().toString();
			Password = ETPassword.getText().toString();
			//showDialog(R.string.about_menu, UserName + ";" + Password, this);
			
			parseLoginData(imageView);
			
		}
		
		parseLoginThread = (Thread) getLastNonConfigurationInstance(); 
		try {
			parseLoginThread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//sleep(50000);
		if (parseLoginThread != null && parseLoginThread.isAlive()) {
			dialog = ProgressDialog.show(this, "Logging in", "Please wait...");
		}else
		{
			if(taskComplete){
				if(authenticated){
					Intent i = new Intent(this, VehicleSearch.class);
					i.putExtra("UserName",ETUserName.getText().toString());
					startActivity(i);
				}else
				{
					showDialog(R.string.about_menu, "Invaid UserName/Password!", this);
				}
			}
		}
		
		//sleep(100000);
		//dialog.dismiss();
		
	}
	private static void showDialog(int title, CharSequence message, Context ctx) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton("OK", null);
		builder.show();
	}
	
	private void showDialog2(int title, CharSequence message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton("OK", null);
		builder.show();
	}
	private void sleep(int i) {
	// TODO Auto-generated method stub
	
	}
}

