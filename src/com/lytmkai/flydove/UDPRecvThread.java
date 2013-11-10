package com.lytmkai.flydove;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UDPRecvThread implements Runnable {

	private static final String TAG = "UDPRecvThread";
	public static final int NEW_USER = 11;
	public static final int ANS_ONLINE = 12;
	public static final int USER_EXIT = 13;
	public static final int RECV_MSG = 14;
	public static final int RECV_FILE = 15;
	
	
	
	public static boolean loop = true;	// 线程持久运行标志
	public int reSentTime;		// 超时重发计数
	public static boolean isAutoSave ;	// 是否自动接收文件
	public static boolean isTone ;		// 是否播放提示音
	public static String save_path ;		// 保存文件路径
	private DatagramPacket packet ;	// 接收到的数据包
	private byte[] buffer;
	@Override
	public void run() {
		Log.i(TAG, "接收线程启动...");
		android.os.Process.setThreadPriority(-5);
		buffer = new byte[256];
		packet = new DatagramPacket(buffer, buffer.length);
		while ( loop ) {
			try {
				UDPSendThread.socket.receive(packet);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
			
			try {
				String msg = new String(buffer, 0, packet.getLength(), "GBK");
				Log.i(TAG, msg);
				msgProcess(msg);
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		
		
		Log.i(TAG, "接收线程结束");
	}
	// 消息帧 处理
	public void msgProcess(String msg){
		String[] msgArr = msg.split(":");
		if (msgArr[3].equals(UDPSendThread.local.getHost())) {
			return;
		}
		Protocol user = new Protocol();
		user.setIP( packet.getAddress().getHostAddress());
		user.setPackNo(msgArr[1]);
		user.setUser(msgArr[2]);
		user.setHost(msgArr[3]);
		StringBuilder addition = new StringBuilder();
		for (int i = 5; i < msgArr.length-1; i++) {
			addition.append(msgArr[i] + ":");
		}
		addition.append(msgArr[msgArr.length-1]);
		user.setAddition(addition.toString());
		int intCmd = Integer.parseInt(msgArr[4]);
		user.setCmd(intCmd);
		int cmdLow  = intCmd & Protocol.GET_LOWBYTE ;
		int cmdHigh = intCmd & Protocol.GET_HIGHBYTE ;
		switch (cmdLow ) {
		case  Protocol.IPMSG_BR_ENTRY :	
			send2Activity(NEW_USER, user);
			break ;
		case Protocol.IPMSG_ANSENTRY : 
			send2Activity(ANS_ONLINE, user);
			break ;
		case Protocol.IPMSG_SENDMSG :
			if ((cmdHigh & Protocol.IPMSG_FILEATTACHOPT) == Protocol.IPMSG_FILEATTACHOPT ) {
				send2Activity(RECV_FILE, user);
			}
			
		default:		
			break;
		}
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// 向主线程发送消息
	public void send2Activity(int msgType, Object addt){
		Handler handler = MainActivity.handler;
		Message msg = handler.obtainMessage(msgType);
		msg.obj = addt;
		handler.sendMessage(msg);
	}
	
}
