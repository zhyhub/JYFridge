package com.joyoungdevlibrary.config;


/**
 * @author Administrator 几个定死命令的byte
 */
public class CommandConst {

	public static String left0(String str)
	{
		int j = 4 - str.length();
		String tmp = "";
		for (int i = 0; i < j; i++)
		{
			tmp = tmp + "0";
		}

		return tmp + str;
	}

	public static byte[] hexIntU32(int i)
	{
		String b = Integer.toHexString(i);

		while (b.length() < 8)
		{
			b = "0" + b;
		}

		byte[] tb = new byte[4];

		int i1 = Integer.valueOf(b.substring(0, 2), 16);
		tb[0] = (byte)i1;
		int i2 = Integer.valueOf(b.substring(2, 4), 16);
		tb[1] = (byte)i2;
		int i3 = Integer.valueOf(b.substring(4, 6), 16);
		tb[2] = (byte)i3;
		int i4 = Integer.valueOf(b.substring(6, 8), 16);
		tb[3] = (byte)i4;
		return tb;

	}

	public static byte[] hexIntU16(int i) {
		String b = Integer.toHexString(i);

		while (b.length() < 4) {
			b = "0" + b;
		}

		byte[] tb = new byte[2];

		int i1 = Integer.valueOf(b.substring(0, 2), 16);
		tb[0] = (byte) i1;
		int i2 = Integer.valueOf(b.substring(2, 4), 16);
		tb[1] = (byte) i2;
		return tb;

	}





	public static byte[] APP_CMD_HEADER = new byte[] { (byte) 0xcc, (byte) 0x00, (byte) 0x00, (byte) 0x01,
			(byte) 0x00 };

	// UDP设备发现
	public static byte[] UDP_FIND_MOUDLE = new byte[] { (byte) 0xcc, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00,
			(byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xdd, (byte) 0x01, (byte) 0x00,
			(byte) 0x00, (byte) 0x00 };

	// 状态查询
	public static byte[] TCP_STATUS_QUERY = new byte[] {

			(byte) 0xcc, (byte) 0x00, //
			(byte) 0x00, (byte) 0x01, // VERSION
			(byte) 0x00, // RAS
			(byte) 0x00, (byte) 0x07, // LENGTH
			(byte) 0x00, // CRU
			(byte) 0x00, (byte) 0x01, // type
			(byte) 0x00, (byte) 0xb2, // cmd type
			(byte) 0x00, (byte) 0x00 // cmd length
	};

	// 状态查询
	public static byte[] TCP_STATUS_QUERY_ff07 = new byte[] {

			(byte) 0xcc, (byte) 0x00, //
			(byte) 0x00, (byte) 0x01, // VERSION
			(byte) 0x00, // RAS
			(byte) 0x00, (byte) 0x07, // LENGTH
			(byte) 0x00, // CRU
			(byte) 0x00, (byte) 0x01, // type
			(byte) 0xFF, (byte) 0x07, // cmd type
			(byte) 0x00, (byte) 0x00 // cmd length
	};

	// 网络菜谱查询[cc 00 00 01 00 00 09 00 00 01 cc d3 00 02 00 00]
	public static byte[] TCP_NETMENU_QUERY = new byte[] { (byte) 0xcc, (byte) 0x00, //
			(byte) 0x00, (byte) 0x01, // VERSION
			(byte) 0x00, // RAS
			(byte) 0x00, (byte) 0x07, // LENGTH
			(byte) 0x00, // CRU
			(byte) 0x00, (byte) 0x01, // type
			(byte) 0xcc, (byte) 0xd3, // cmd type
			(byte) 0x00, (byte) 0x00 // cmd length
	};
}
