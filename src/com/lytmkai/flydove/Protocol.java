package com.lytmkai.flydove;

public class Protocol {

	public static String BROADCAST_ADDRESS = "255.255.255.255"; // 发送广播地址

	// 命令字 低8位有效
	public static final int IPMSG_NOOPERATION = 0x00000000; // 不进行任何操作
	public static final int IPMSG_BR_ENTRY = 0x00000001; // 用户上线
	public static final int IPMSG_BR_EXIT = 0x00000002; // 用户下线
	public static final int IPMSG_ANSENTRY = 0x00000003; // 响应在线
	public static final int IPMSG_BR_ABSENCE = 0x00000004; // 更改为离开状态
	public static final int IPMSG_SENDMSG = 0x00000020; // 发送消息
	public static final int IPMSG_RECVMSG = 0x00000021; // 通报收到消息
	public static final int IPMSG_READMSG = 0x00000030; // 消息打开通知
	public static final int IPMSG_DELMSG = 0x00000031; // 消息丢弃通知
	// 选项字 高24位有效
	public static final int IPMSG_FILEATTACHOPT = 0x00200000; // 文件附件选项
	public static final int IPMSG_GETFILEDATA = 0x00000060;	// 文件请求传输
	
	//
	public static final int GET_LOWBYTE = 0x000000FF;
	public static final int GET_HIGHBYTE = 0xFFFFFF00;
	public static String port = "2425"; // 端口
	private String ip = "" ;   // 用户IP  
	private int Ver = 1; // 飞鸽协议 - 版本号
	private String packNo; // 飞鸽协议 - 数据包编号
	private String user = ""; // 飞鸽协议 - 用户名
	private String host = ""; // 飞鸽协议 - 主机
	private int cmd = 0; // 飞鸽协议 - 命令
	private String addition = ""; // 飞鸽协议 - 附加消息

	Protocol() {
		
	}
	public String getIP(){
		return ip;
	}
	public void setIP(String ip){
		this.ip = ip ;
	}
	public String getPackNo() {
		return packNo;
	}

	public void setPackNo(String packNo) {
		this.packNo = packNo;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getCmd() {
		return cmd;
	}

	public void setCmd(int cmd) {
		this.cmd = cmd;
	}

	public String getAddition() {
		return addition;
	}

	public void setAddition(String addition) {
		this.addition = addition;
	}

	@Override
	public String toString() {
		return Ver + ":" + getPacketNo() + ":" + user + ":" + host + ":" + cmd
				+ ":" + addition;
	}

	public String getPacketNo() {
		long ms = System.currentTimeMillis();
		return String.valueOf(ms);
	}
}
