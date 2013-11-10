package com.lytmkai.flydove;

public class Protocol {

	public static String BROADCAST_ADDRESS = "255.255.255.255"; // ���͹㲥��ַ

	// ������ ��8λ��Ч
	public static final int IPMSG_NOOPERATION = 0x00000000; // �������κβ���
	public static final int IPMSG_BR_ENTRY = 0x00000001; // �û�����
	public static final int IPMSG_BR_EXIT = 0x00000002; // �û�����
	public static final int IPMSG_ANSENTRY = 0x00000003; // ��Ӧ����
	public static final int IPMSG_BR_ABSENCE = 0x00000004; // ����Ϊ�뿪״̬
	public static final int IPMSG_SENDMSG = 0x00000020; // ������Ϣ
	public static final int IPMSG_RECVMSG = 0x00000021; // ͨ���յ���Ϣ
	public static final int IPMSG_READMSG = 0x00000030; // ��Ϣ��֪ͨ
	public static final int IPMSG_DELMSG = 0x00000031; // ��Ϣ����֪ͨ
	// ѡ���� ��24λ��Ч
	public static final int IPMSG_FILEATTACHOPT = 0x00200000; // �ļ�����ѡ��
	public static final int IPMSG_GETFILEDATA = 0x00000060;	// �ļ�������
	
	//
	public static final int GET_LOWBYTE = 0x000000FF;
	public static final int GET_HIGHBYTE = 0xFFFFFF00;
	public static String port = "2425"; // �˿�
	private String ip = "" ;   // �û�IP  
	private int Ver = 1; // �ɸ�Э�� - �汾��
	private String packNo; // �ɸ�Э�� - ���ݰ����
	private String user = ""; // �ɸ�Э�� - �û���
	private String host = ""; // �ɸ�Э�� - ����
	private int cmd = 0; // �ɸ�Э�� - ����
	private String addition = ""; // �ɸ�Э�� - ������Ϣ

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
