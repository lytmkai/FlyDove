package com.lytmkai.flydove;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TCPTransThread extends Thread {
	private static final String TAG = "TCPSendThread";
	public static final int RECV_SIZE = 21;
	
	private Socket socket;
	private SocketAddress address;
	private String fileName;
	private long fileSize;
	TCPTransThread(Socket socket, SocketAddress address, String filename, long filesize){
		this.socket = socket;
		this.address = address;
		this.fileName = filename;
		this.fileSize = filesize;
	}
	
	
	@Override
	public void run() {
		try {
			socket.connect(address);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			byte[] buffer = UDPSendThread.local.toString().getBytes("gbk");
			OutputStream os = socket.getOutputStream();
			os.write(buffer, 0, buffer.length);
			os.flush();
			File SDCARD_File = Environment.getExternalStorageDirectory();
			if ( Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				File file = new File(SDCARD_File, UDPRecvThread.save_path + File.separator + fileName) ;
				if ( file.exists() ) {
					file.delete() ;
				}
				File parent = file.getParentFile();
				parent.mkdirs();
				try {
					Log.i(TAG, fileSize+"");
					buffer = new byte[1024];
					FileOutputStream bos = new FileOutputStream(file);
					InputStream is = socket.getInputStream();
					int len = 0;
					long progress = 0;
					while ( (len = is.read(buffer)) != -1 ) {
						bos.write(buffer, 0, len);
						bos.flush();
						progress += len;
						Log.i(TAG, len + "--");
						
						send2Activity(RECV_SIZE, progress * 100 / fileSize );
						if ( progress == fileSize) {
							break;
						}
					}
					Log.i(TAG, "File received over..");
					bos.close();
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				
				
				
				
				
				
				
			} else {
				Log.i(TAG, "SDcard不存在或者不能进行读写操作") ;
				return ;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
		
		
		
		
		
	}

	
	
	
	public void send2Activity(int msgType, Object addt){
		Handler handler = MainActivity.handler;
		Message msg = handler.obtainMessage(msgType);
		msg.obj = addt;
		handler.sendMessage(msg);
	}
	
	
	
	
	
	
	
	
	
}
