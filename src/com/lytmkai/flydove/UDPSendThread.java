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
	
	public static boolean loop = true;		//�߳�ѭ�����б�־
	public static boolean idle	;
	private MainActivity activity;
	protected static DatagramSocket socket;		// UDP����socket
	private DatagramPacket packet;	// Ҫ���͵����ݰ�
	public static int type;		// �̲߳�������
	public static Protocol local;		// ����Э��֡����
	private int port;					// UDP�㲥�˿ڵ� int��ʽ
	
	UDPSendThread(MainActivity activity){
		this.activity = activity;
		port = Integer.parseInt(Protocol.port);
	}
	@Override
	public void run() {
		checkWIFI();
		if (initConn()) {   // ���������߳�
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
				// �����޲��� ���ݰ�
				idle = false;
				NoOperPacket();
			case Protocol.IPMSG_BR_ENTRY :
				// ����֪ͨ ���ݰ�
				idle = false;
				// �������ݰ� ��������
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
				// ��Ӧ���� ���ݰ�
				idle = false;
				AnsOnlinePacket() ;
				type = NOSEND ;
				break;
			case Protocol.IPMSG_SENDMSG :
				// ������Ϣ ���ݰ�
				idle = false;
				type = NOSEND ;
				break;
			case Protocol.IPMSG_FILEATTACHOPT: 
				// �����ļ� ���ݰ�
				idle = false;
				type = NOSEND ;
				break;
			case Protocol.IPMSG_BR_EXIT :
				// �������� ���ݰ�
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
		Log.i(TAG, "�����߳̽���...");
	}
	// �����������
	public void checkWIFI(){
		boolean first = true;
		boolean isConn;
		do {
			ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			isConn = info.isConnected();
			// ��һ�μ�� ��Activity����WIFIΪ������Ϣ
			if (first && isConn==false) {
				send2Activity(WIFI_DISCONNECT, "WIFIδ����..");
				first= false;
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Log.i(TAG, "WIFI���Ӽ��ing...");
		} while (!isConn);
		send2Activity(WIFI_CONNECTED, "WIFI�Ѿ�����");
	}
	// ��ʼ��UDP��������
	private boolean initConn(){
		boolean flag;
		WifiManager wifiMgr = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo info =  wifiMgr.getDhcpInfo();
		int ip = info.ipAddress;		// ��ȡIP��ַ, ���Ϊ����
		String ipStr = ( ip & 0xFF)+ "." + ((ip >> 8 ) & 0xFF) + "." + ((ip >> 16 ) & 0xFF) +"."+ ((ip >> 24 ) & 0xFF); // ��������IP�õ��ַ�����ʽ��IP��ַ
		local.setHost("Android");	// ���ñ���Protocol��IP��ַ
		int netmask = info.netmask;		// ��ȡ��������
		int brInt = ip | (~netmask);	// ��������ȡ������IP��������, �õ��㲥��ַ��������ʽ
		String brStr = ( brInt & 0xFF)+ "." + ((brInt >> 8 ) & 0xFF) + "." + ((brInt >> 16 ) & 0xFF) +"."+ ((brInt >> 24 ) & 0xFF);
		Protocol.BROADCAST_ADDRESS = brStr;
		try {
			socket = new DatagramSocket(port);
			flag = true;
		} catch (SocketException e) {
			Log.i(TAG, "�˿ڴ򿪴���...");
			flag = false;
		}
		return flag;
		
	}
	// ���� �޲��� ���ݰ�
	public void NoOperPacket(){
		local.setCmd(Protocol.IPMSG_NOOPERATION);
		local.setAddition("\0");
		sendPacket(local, Protocol.BROADCAST_ADDRESS, port);
	}
	// ���� ���� ���ݰ�
	public void OnlinePacket(){
		local.setCmd(Protocol.IPMSG_BR_ENTRY);
		local.setAddition(local.getUser() + "\0Phone\0");
		sendPacket(local, Protocol.BROADCAST_ADDRESS, port);
	}
	// ���� ��Ӧ���� ���ݰ�
	public void AnsOnlinePacket(){
		local.setCmd(Protocol.IPMSG_ANSENTRY);
		local.setAddition(local.getUser() + "\0Phone\0");
		sendPacket(local, Protocol.BROADCAST_ADDRESS, port);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	// ����֡��Ϣ��UDP������
	public void sendPacket(Protocol protocol, String host, int port){
		String str = protocol.toString();
		byte[] buffer = str.getBytes();
		try {
			packet = new DatagramPacket(buffer, buffer.length, new InetSocketAddress(host, port));
			socket.send(packet);
		} catch (Exception e) {
			Log.i(TAG, "�˿����Ӵ���");
		} 
	}
	
	
	
	// ��MainActivity������Ϣ
	public void send2Activity(int msgType, String addt){
		Handler handler = MainActivity.handler;
		Message msg = handler.obtainMessage(msgType);
		msg.obj = addt;
		handler.sendMessage(msg);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


	
	
	

	
	
	
	
	
	
	
	
}
