package smartlink.zhy.jyfridge;

/**
 * 常量池
 */

public class ConstantPool {

    public static final int FridgeId = 1;
    public static final int UserID = 12345;

    public static final int Camera_0 = 0;
    public static final int Camera_1 = 1;
    public static final int Camera_2 = 2;
    public static final int Camera_3 = 3;

    public static final String BASE_RUYI = "http://api.ruyi.ai/";
//    public static final String BASE_URL = "http://192.168.100.173:8080/imgTest/";
    public static final String BASE_URL = "http://192.168.199.182:8080/imgTest/";
    public static final String APP_KEY = "a0927149-bfd5-40cc-824e-a110b0847c6e";

    public static final String AI = BASE_URL + "in/general!ai";
//    public static final String UpLoadInfo = "http://192.168.100.173:8080/imgTest/in/img!uploadInfo";
    public static final String UpLoadInfo = "http://192.168.199.182:8080/imgTest/in/img!uploadInfo";

    public static final byte Zero = 0x00;
    public static final byte Default = 10;
    public static final byte Data0_beginning_commend = 0x55;
    public static final byte Data1_beginning_commend = (byte) 0xAA;
    public static final byte Data2_Modify_Temperature = 0x01;
    public static final byte Data2_Modify_Mode = 0x02;
    public static final byte Data2_Setting_time = 0x03;
    public static final byte Data2_Running_State = 0x04;
    public static final byte Data2_System_Timing = 0x05;
    public static final byte Data2_Remote_Maintenance = 0x06;
    public static final byte Intelligent_Model = 0x01;
    public static final byte LengCang_Shutdown_Model = 0x02;
    public static final byte Holiday_Mode = 0x04;
    public static final byte BianWen_Shutdown_Model = 0x08;
    public static final byte Quick_Freezing_Mode = 0x10;
    public static final byte Quick_Cooling_Mode = 0x20;
    public static final byte Child_Lock_Mode = 0x40;
    public static final byte LECO_Mode = (byte) 0x80;

}
