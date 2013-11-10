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
	private ArrayList<Map<String, String>> items; // 存放ListView的所有数据
	private SimpleAdapter adapter;
	public static MyHandler handler;
	private GestureDetector gesture;
	private PopupWindow menu;
	private WakeLock lock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		// 获取Activity的组件
		title = (TextView) findViewById(R.id.title);
		listView = (ListView) findViewById(R.id.list);
		// 读取系统设置
		LoadSetting();
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		lock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
		lock.acquire(3600000);
		
		
		
		
		// 手势判断
		gesture = new GestureDetector(MainActivity.this, new gestureListener());

		// 创建ListView
		items = new ArrayList<Map<String, String>>();
		adapter = new SimpleAdapter(this, items, R.layout.item_listview,
				new String[] { "row1", "row2" }, new int[] { R.id.text1,
						R.id.text2 });
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

		new UDPSendThread(MainActivity.this).start();
		handler = new MyHandler();

	}

	// 侧滑菜单的点击事件处理
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

	// 读取程序设置
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

	// ListView的点击操作
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this,
				AlertDialog.THEME_TRADITIONAL);
		builder.setTitle("选择操作选项");
		builder.setItems(new String[] { "发送消息", "发送文件" }, new DialogInterface.OnClickListener() {
			
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

	// ListView的长按操作
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int pos,
			long id) {
		Log.i(TAG, "long click item");
		return false;
	}

	// 接收线程信息, 并处理
	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			int msgType = msg.what;
			switch (msgType) {
			case UDPSendThread.WIFI_DISCONNECT:
				NoWIFI_Process();
				break;
			case UDPSendThread.WIFI_CONNECTED:
				Toast.makeText(MainActivity.this, "WIFI已经连接", Toast.LENGTH_SHORT).show();
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

	// 无WIFI连接时 的处理
	public void NoWIFI_Process() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				MainActivity.this, AlertDialog.THEME_TRADITIONAL); // 创建对话框
		builder.setMessage("WIFI未连接, 进入WIFI设置页面?");
		builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
			}
		});
		builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
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

	// 新用户上线 的消息处理
	public void NewUser_Process(Protocol user) {
		String userIP = user.getIP();
		// String userHost = user.getHost();
		String addt = user.getAddition();
		String[] addtArr = addt.split("\0");
		String userName = addtArr[0];
		// String group = addtArr[1];
		// ListView 的一条数据
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// 手势监听器 实现
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

	// 屏幕点击处理
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (menu != null && menu.isShowing()) {
			menu.dismiss();
		}
		return gesture.onTouchEvent(event);
	}

	// 实体按键<back, menu>的事件处理
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
