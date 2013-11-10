package com.lytmkai.flydove;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UDPSendThread extends Thread {
	public static final int WIFI_DISCONNECT = 1;
	public static final int WIFI_CONNECTED = 2;
	public static final int NOSEND = 63;
	
	private static final String TAG = "UDPSendThread";
	
	public static boolean loop = true;		//线程循环运行标志
	public static boolean idle	;
	private MainActivity activity;
	protected static DatagramSocket socket;		// UDP传输socket
	private DatagramPacket packet;	// 要发送的数据包
	public static int type;		// 线程操作类型
	public static Protocol local;		// 本机协议帧对象
	private int port;					// UDP广播端口的 int形式
	
	UDPSendThread(MainActivity activity){
		this.activity = activity;
		port = Integer.parseInt(Protocol.port);
	}
	@Override
	public void run() {
		checkWIFI();
		if (initConn()) {   // 启动接收线程
			new Thread(new UDPRecvThread()).start();	
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int time = 0;
		type = Protocol.IPMSG_NOOPERATION;
		while ( loop ) {
			switch ( type ) {
			case Protocol.IPMSG_NOOPERATION :
				// 发送无操作 数据包
				idle = false;
				NoOperPacket();
			case Protocol.IPMSG_BR_ENTRY :
				// 上线通知 数据包
				idle = false;
				// 上线数据包 发送三次
				while ( time < 3 ) {
					OnlinePacket();
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					time++;
				} 
				time = 0;
				type = NOSEND ;
				break;
			case Protocol.IPMSG_ANSENTRY :
				// 响应在线 数据包
				idle = false;
				AnsOnlinePacket() ;
				type = NOSEND ;
				break;
			case Protocol.IPMSG_SENDMSG :
				// 发送信息 数据包
				idle = false;
				type = NOSEND ;
				break;
			case Protocol.IPMSG_FILEATTACHOPT: 
				// 发送文件 数据包
				idle = false;
				type = NOSEND ;
				break;
			case Protocol.IPMSG_BR_EXIT :
				// 本机下线 数据包
				idle = false;
				type = NOSEND ;
				break ;
			case Protocol.IPMSG_RECVMSG :
				sendPacket(local, Protocol.BROADCAST_ADDRESS, port);
				type = NOSEND;
				break;
			default:	
				idle = true;
				break;
			}
		}
		Log.i(TAG, "发送线程结束...");
	}
	// 检查网络连接
	public void checkWIFI(){
		boolean first = true;
		boolean isConn;
		do {
			ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			isConn = info.isConnected();
			// 第一次检测 向Activity发送WIFI为连接消息
			if (first && isConn==false) {
				send2Activity(WIFI_DISCONNECT, "WIFI未连接..");
				first= false;
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Log.i(TAG, "WIFI连接检测ing...");
		} while (!isConn);
		send2Activity(WIFI_CONNECTED, "WIFI已经连接");
	}
	// 初始化UDP网络连接
	private boolean initConn(){
		boolean flag;
		WifiManager wifiMgr = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo info =  wifiMgr.getDhcpInfo();
		int ip = info.ipAddress;		// 获取IP地址, 结果为整形
		String ipStr = ( ip & 0xFF)+ "." + ((ip >> 8 ) & 0xFF) + "." + ((ip >> 16 ) & 0xFF) +"."+ ((ip >> 24 ) & 0xFF); // 根据整形IP得到字符串形式的IP地址
		local.setHost("Android");	// 设置本地Protocol的IP地址
		int netmask = info.netmask;		// 获取子网掩码
		int brInt = ip | (~netmask);	// 子网掩码取反再与IP作或运算, 得到广播地址的整形形式
		String brStr = ( brInt & 0xFF)+ "." + ((brInt >> 8 ) & 0xFF) + "." + ((brInt >> 16 ) & 0xFF) +"."+ ((brInt >> 24 ) & 0xFF);
		Protocol.BROADCAST_ADDRESS = brStr;
		try {
			socket = new DatagramSocket(port);
			flag = true;
		} catch (SocketException e) {
			Log.i(TAG, "端口打开错误...");
			flag = false;
		}
		return flag;
		
	}
	// 创建 无操作 数据包
	public void NoOperPacket(){
		local.setCmd(Protocol.IPMSG_NOOPERATION);
		local.setAddition("\0");
		sendPacket(local, Protocol.BROADCAST_ADDRESS, port);
	}
	// 创建 上线 数据包
	public void OnlinePacket(){
		local.setCmd(Protocol.IPMSG_BR_ENTRY);
		local.setAddition(local.getUser() + "\0Phone\0");
		sendPacket(local, Protocol.BROADCAST_ADDRESS, port);
	}
	// 创建 响应在线 数据包
	public void AnsOnlinePacket(){
		local.setCmd(Protocol.IPMSG_ANSENTRY);
		local.setAddition(local.getUser() + "\0Phone\0");
		sendPacket(local, Protocol.BROADCAST_ADDRESS, port);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	// 发送帧信息到UDP网络中
	public void sendPacket(Protocol protocol, String host, int port){
		String str = protocol.toString();
		byte[] buffer = str.getBytes();
		try {
			packet = new DatagramPacket(buffer, buffer.length, new InetSocketAddress(host, port));
			socket.send(packet);
		} catch (Exception e) {
			Log.i(TAG, "端口连接错误");
		} 
	}
	
	
	
	// 向MainActivity发送消息
	public void send2Activity(int msgType, String addt){
		Handler handler = MainActivity.handler;
		Message msg = handler.obtainMessage(msgType);
		msg.obj = addt;
		handler.sendMessage(msg);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


	
	
	

	
	
	
	
	
	
	
	
}
