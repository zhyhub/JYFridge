package smartlink.zhy.jyfridge;

/**
 * 常量池
 */

public class ConstantPool {

    static final byte Zero = 0x00;
    static final byte Default = 10;

    static final byte Data0_beginning_commend = 0x55;
    static final byte Data1_beginning_commend = (byte) 0xAA;

    static final byte Data2_Modify_Temperature = 0x01;
    static final byte Data2_Modify_Mode = 0x02;
    static final byte Data2_Setting_time = 0x03;
    static final byte Data2_Running_State = 0x04;
    static final byte Data2_System_Timing = 0x05;
    static final byte Data2_Remote_Maintenance = 0x06;

    static final byte Intelligent_Model = 0x01;
    static final byte LengCang_Shutdown_Model = 0x02;
    static final byte Holiday_Mode = 0x04;
    static final byte BianWen_Shutdown_Model = 0x08;
    static final byte Quick_Freezing_Mode = 0x10;
    static final byte Quick_Cooling_Mode = 0x20;
    static final byte Child_Lock_Mode = 0x40;
    static final byte LECO_Mode = (byte) 0x80;

}
