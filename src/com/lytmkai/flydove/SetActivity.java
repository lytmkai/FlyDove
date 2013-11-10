package com.lytmkai.flydove;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class SetActivity extends Activity {

	private ImageView btnSetBack ;				// ���� ��ť
	private Button btnSetPort ;			// �˿� ���ð�ť, ��ť�ı���ʾ���õĶ˿�
	private Button btnSetUser ;			// �û��� ���ð�ť, ��ť�ı���ʾ���õ��û���
	private CheckedTextView chkAutoSave ;	// �Զ����� ����
	private Button btnSetPath ;			// Ĭ��·�� ��ť, ��ť�ı���ʾ���õ�·��
	private CheckedTextView chkTone ;		// ��ʾ�� ����
	private SharedPreferences settings ;   // ��ȡ������Ϣ
	private SharedPreferences.Editor editor ;	// �޸�������Ϣ
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		btnSetBack = (ImageView)findViewById(R.id.btnSetBack);
		btnSetPort = (Button)findViewById(R.id.btnSetPort);
		btnSetUser = (Button)findViewById(R.id.btnSetUser);
		chkAutoSave = (CheckedTextView)findViewById(R.id.btnSetSave);
		btnSetPath = (Button)findViewById(R.id.btnSetPath);
		chkTone = (CheckedTextView)findViewById(R.id.btnSetTone);
		
		setListener listener = new setListener();
		btnSetBack.setOnClickListener(listener);
		btnSetPort.setOnClickListener(listener);
		btnSetUser.setOnClickListener(listener);
		chkAutoSave.setOnClickListener(listener);
		btnSetPath.setOnClickListener(listener);
		chkTone.setOnClickListener(listener);
		
		settings = getSharedPreferences("config", Context.MODE_PRIVATE);
		editor = settings.edit();
	}

	class setListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			switch ( v.getId() ) {
				case R.id.btnSetBack :
					onBackPressed();
					break;
				case R.id.btnSetPort :
					portShowDialog();
					break;
				case R.id.btnSetUser :
					userShowDialog() ;
					break;
				case R.id.btnSetSave :
					switchAutoSave();
					break;
				case R.id.btnSetPath :
					selectPath();
					break;
				case R.id.btnSetTone :
					switchTone();
					break;
				default:		
					break;
			}
		}
	}
	// ���� �˿� �ĶԻ���
	public void portShowDialog(){
		AlertDialog.Builder builder =  new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
		
		final EditText editPort = new EditText(this);
		editPort.setTextColor(Color.BLACK);
		editPort.setTextSize(30f);
		editPort.setText(btnSetPort.getText());
		editPort.setHint("������˿ں�...");
		editPort.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
		editPort.setInputType(InputType.TYPE_CLASS_NUMBER);
		editPort.setGravity(Gravity.CENTER);
		builder.setView(editPort);
		
		builder.setMessage(null);
		builder.setNegativeButton("ȡ��", null);
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String tmp = editPort.getText().toString();
				if (tmp =="") {
					return;
				}
				btnSetPort.setText( tmp );
				Protocol.port = tmp ;
				editor.putString("port", tmp);
				editor.commit();
			}
		});
		builder.show();
	}
	// ���� �û��� �ĶԻ���
	public void userShowDialog(){
		AlertDialog.Builder builder =  new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
		
		final EditText editUser = new EditText(this);
		editUser.setTextColor(Color.BLACK);
		editUser.setTextSize(30f);
		editUser.setText(btnSetUser.getText());
		editUser.setHint("�������û���...");
		editUser.setGravity(Gravity.CENTER);
		builder.setView(editUser);
		
		builder.setMessage(null);
		builder.setNegativeButton("ȡ��", null);
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String tmp = editUser.getText().toString();
				if (tmp =="") {
					return;
				}
				btnSetUser.setText( tmp );
				editor.putString("user", tmp);
			}
		});
		builder.show();
	}
	// ���� �Զ������·�� �ĶԻ���
	public void selectPath(){
		final String SDCARD_Path =  Environment.getExternalStorageDirectory().getPath();
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File sd = new File(SDCARD_Path);
			// �г�SDCARD�µ������ļ���
			final String[] files = sd.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					File current = new File(dir, filename);
					if (filename.startsWith(".") || current.isFile()) {
						return false;
					} else {
						return true;
					}
				}
			});
			// ���������ļ���
			Arrays.sort(files);
			// �����ļ�ѡ��Ի���
			AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
			builder.setTitle(sd.getPath() + File.separator);
			builder.setSingleChoiceItems(files, -1, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					btnSetPath.setText(SDCARD_Path + File.separator + files[which]);
					editor.putString("defPath", File.separator + files[which]);
					dialog.cancel();
					
				}
			});
			Dialog dialog = builder.show();
			dialog.closeOptionsMenu();
		} else {
			Toast.makeText(SetActivity.this, "SD�� �����ڻ��߲��ܽ��ж�д����", Toast.LENGTH_SHORT).show(); 
		}
	}
	// ���� �Զ����� ���
	public void switchAutoSave(){
		boolean check = chkAutoSave.isChecked();
		if (check) {
			chkAutoSave.setChecked(false);
//			chkAutoSave.setCheckMarkDrawable(R.drawable.uncheck);
		} else {
			chkAutoSave.setChecked(true);
//			chkAutoSave.setCheckMarkDrawable(R.drawable.checked);
		}
		editor.putBoolean("autoSave", !check);
	}
	// ���� ��ʾ�� ���
	public void switchTone(){
		boolean flag = chkTone.isChecked();
		if (flag) {
			chkTone.setChecked(false);
			//chkTone.setCheckMarkDrawable(R.drawable.uncheck);
		} else {
			chkTone.setChecked(true);
			//chkTone.setCheckMarkDrawable(R.drawable.checked);
		}
		editor.putBoolean("tone", !flag);
	}
	
	
	
	
	
	
	@Override
	protected void onStart() {
		btnSetPort.setText( settings.getString("port", "2425") ); 
		btnSetUser.setText( settings.getString("user", "Android"));
		chkAutoSave.setChecked( settings.getBoolean("autoSave", true) );
		btnSetPath.setText( "/mnt/SDCARD" + settings.getString("defPath", "/Download"));
		chkTone.setChecked(settings.getBoolean("tone", true));
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		editor.commit();
		super.onStop();
	}
	
	
	
	
	
	
	
	

}
