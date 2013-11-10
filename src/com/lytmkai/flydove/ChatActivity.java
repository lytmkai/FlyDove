package com.lytmkai.flydove;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ChatActivity extends Activity {

	private TextView text ;
	private EditText edit ;
	private Button btnSave ;
	private Button btnLoad ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_activity);
		
		text = (TextView) findViewById(R.id.text);
		btnSave = (Button) findViewById(R.id.btnSave);
		btnLoad = (Button) findViewById(R.id.btnLoad);
		
		btnSave.setOnClickListener( new saveListener() );
		btnLoad.setOnClickListener( new loadListener() );
	}

	class saveListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			String sd = Environment.getExternalStorageDirectory().getPath();
			try {
				FileOutputStream fos = new FileOutputStream(sd + UDPRecvThread.save_path + "/1.txt");
				fos.write("Hello world".getBytes("gbk"));
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class loadListener implements OnClickListener{
		@Override
		public void onClick(View v) {

		}
	}
	
	
	
	
	
	
	
	
	
	
}
