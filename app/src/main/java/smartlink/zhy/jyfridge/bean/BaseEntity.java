package smartlink.zhy.jyfridge.bean;


/**
 * Created by Administrator on 2017/11/30 0030.
 */

public class BaseEntity {

    private String msg;
    private int code;
    private byte[] data;
    private boolean suceess;
    private String text;
    private int type;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isSuceess() {
        return suceess;
    }

    public void setSuceess(boolean suceess) {
        this.suceess = suceess;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
