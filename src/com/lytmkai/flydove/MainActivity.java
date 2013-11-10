package com.lytmkai.flydove;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.lytmkai.flydove.R.id;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener,
		OnItemLongClickListener, OnClickListener {

	private static final String TAG = "MainActivity";
	private TextView title;
	private ListView listView;
	private Button btnRefresh;
	private Button btnSetting;
	private Button btnAbout;
	private Button btnQuit;
	private ArrayList<Map<String, String>> items; // ���ListView����������
	private SimpleAdapter adapter;
	public static MyHandler handler;
	private GestureDetector gesture;
	private PopupWindow menu;
	private WakeLock lock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		// ��ȡActivity�����
		title = (TextView) findViewById(R.id.title);
		listView = (ListView) findViewById(R.id.list);
		// ��ȡϵͳ����
		LoadSetting();
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		lock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
		lock.acquire(3600000);
		
		
		
		
		// �����ж�
		gesture = new GestureDetector(MainActivity.this, new gestureListener());

		// ����ListView
		items = new ArrayList<Map<String, String>>();
		adapter = new SimpleAdapter(this, items, R.layout.item_listview,
				new String[] { "row1", "row2" }, new int[] { R.id.text1,
						R.id.text2 });
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

		new UDPSendThread(MainActivity.this).start();
		handler = new MyHandler();

	}

	// �໬�˵��ĵ���¼�����
	@Override
	public void onClick(View v) {
		// menu.showContent();
		switch (v.getId()) {
		case id.btnRefresh:
			UDPSendThread.type = Protocol.IPMSG_NOOPERATION;
			menu.dismiss();
			break;
		case id.btnSetting:
			startActivity(new Intent(MainActivity.this, SetActivity.class));
			break;
		case id.btnAbout:
			Log.i(TAG, "about clicked...");
			break;
		case id.btnQuit:
			lock.release();
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);
			break;

		default:
			break;
		}

	}

	// ��ȡ��������
	public void LoadSetting() {
		UDPSendThread.local = new Protocol();
		SharedPreferences settings = getSharedPreferences("config",
				Context.MODE_PRIVATE);
		Protocol.port = settings.getString("port", "2425");
		UDPSendThread.local.setUser(settings.getString("user", "Android"));
		UDPRecvThread.isAutoSave = settings.getBoolean("autoSave", true);
		UDPRecvThread.save_path = settings.getString("defPath", "/Download");
		UDPRecvThread.isTone = settings.getBoolean("tone", true);
	}

	// ListView�ĵ������
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this,
				AlertDialog.THEME_TRADITIONAL);
		builder.setTitle("ѡ�����ѡ��");
		builder.setItems(new String[] { "������Ϣ", "�����ļ�" }, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (0 == which) {
					startActivity(new Intent(MainActivity.this, ChatActivity.class));
				}else if (1 == which) {
					
				}
			}
		} );
		builder.show();

	}

	// ListView�ĳ�������
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int pos,
			long id) {
		Log.i(TAG, "long click item");
		return false;
	}

	// �����߳���Ϣ, ������
	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			int msgType = msg.what;
			switch (msgType) {
			case UDPSendThread.WIFI_DISCONNECT:
				NoWIFI_Process();
				break;
			case UDPSendThread.WIFI_CONNECTED:
				Toast.makeText(MainActivity.this, "WIFI�Ѿ�����", Toast.LENGTH_SHORT).show();
				break;
			case UDPRecvThread.NEW_USER:
				UDPSendThread.type = Protocol.IPMSG_ANSENTRY;
			case UDPRecvThread.ANS_ONLINE :
				Protocol user = (Protocol) msg.obj;
				NewUser_Process(user);
				user = null;
				break;
			case UDPRecvThread.RECV_FILE :
				user = (Protocol) msg.obj;
				RecvFile_Process(user);
				break ;
			case TCPTransThread.RECV_SIZE :
				long len = (Long)msg.obj;
				Log.i(TAG, len + "-->");
				break ;
			default:
				break;
			}
		}
	}

	// ��WIFI����ʱ �Ĵ���
	public void NoWIFI_Process() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				MainActivity.this, AlertDialog.THEME_TRADITIONAL); // �����Ի���
		builder.setMessage("WIFIδ����, ����WIFI����ҳ��?");
		builder.setPositiveButton("����", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
			}
		});
		builder.setNegativeButton("�˳�", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				UDPSendThread.loop = false;
				UDPRecvThread.loop = false;
				lock.release();
				System.exit(0);
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	// ���û����� ����Ϣ����
	public void NewUser_Process(Protocol user) {
		String userIP = user.getIP();
		// String userHost = user.getHost();
		String addt = user.getAddition();
		String[] addtArr = addt.split("\0");
		String userName = addtArr[0];
		// String group = addtArr[1];
		// ListView ��һ������
		Map<String, String> item = new HashMap<String, String>();
		item.put("row1", userName);
		item.put("row2", "(" + userIP + ")");
		if (items.contains(item)) {
			return;
		}
		items.add(item);
		adapter.notifyDataSetChanged();
	}

	public void RecvFile_Process(Protocol user){
		if( 0x100 == (user.getCmd() & 0x100)){
			UDPSendThread.local.setCmd(Protocol.IPMSG_RECVMSG);
			UDPSendThread.local.setAddition(user.getPackNo() + "\0" + "Phone\0");
			UDPSendThread.type = Protocol.IPMSG_RECVMSG ;
		}
		
		
		
		Socket socket = new Socket();
		SocketAddress userAddress = new InetSocketAddress(user.getIP(), Integer.parseInt(Protocol.port));
		String addtion = user.getAddition();
		String[] addtArr = addtion.split("\0");
		String[] fileInfo = addtArr[1].split(":");
		String fileNo = fileInfo[0];
		String fileName = fileInfo[1];
		String fileSize = fileInfo[2];
		long filesize = Long.parseLong(fileSize, 16);
		String userPackNo = Integer.toHexString(Integer.parseInt(user.getPackNo()));
		UDPSendThread.local.setCmd(Protocol.IPMSG_GETFILEDATA);
		UDPSendThread.local.setAddition(userPackNo + ":" + fileNo + ":" + "0:");
		new TCPTransThread(socket, userAddress, fileName, filesize).start();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// ���Ƽ����� ʵ��
	class gestureListener extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float x, float y) {
			View view = getLayoutInflater().inflate(R.layout.menu, null);
			menu = new PopupWindow(view, 200, LayoutParams.WRAP_CONTENT, false);
			float distanceX = e1.getX() - e2.getX();
			if (distanceX > 100 && Math.abs(x) > 50) {
				menu.setOutsideTouchable(true);
				menu.showAsDropDown(title, 600, 4);

				btnRefresh = (Button) view.findViewById(R.id.btnRefresh);
				btnRefresh.setOnClickListener(MainActivity.this);

				btnSetting = (Button) view.findViewById(R.id.btnSetting);
				btnSetting.setOnClickListener(MainActivity.this);

				btnAbout = (Button) view.findViewById(R.id.btnAbout);
				btnAbout.setOnClickListener(MainActivity.this);

				btnQuit = (Button) view.findViewById(R.id.btnQuit);
				btnQuit.setOnClickListener(MainActivity.this);

			}
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if (menu != null && menu.isShowing()) {
				menu.dismiss();
			}
			return false;
		}
	}

	// ��Ļ�������
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (menu != null && menu.isShowing()) {
			menu.dismiss();
		}
		return gesture.onTouchEvent(event);
	}

	// ʵ�尴��<back, menu>���¼�����
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (menu != null && menu.isShowing()) {
				menu.dismiss();
			}
		}
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (menu != null && !menu.isShowing()) {
				menu.showAsDropDown(title, 600, 4);
			} else if (menu != null && menu.isShowing()) {
				menu.dismiss();
			}
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void finish() {
		moveTaskToBack(true);
	}

	@Override
	protected void onDestroy() {
		lock.release();
		Log.i(TAG, "MainActivity destroy...");
		super.onDestroy();

	}

}
