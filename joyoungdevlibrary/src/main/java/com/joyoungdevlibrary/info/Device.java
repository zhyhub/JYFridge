package com.joyoungdevlibrary.info;

/**
 * Created by liuwei on 2016/6/30.
 */
public class Device {

    private String mdcode;
    private String sn;
    private String host;
    private String port;
    private String userId;
    private String keepalive;

    private String addr;
    private String user;
    private String pass;


    public String getMdcode() {
        return mdcode;
    }

    public void setMdcode(String mdcode) {
        this.mdcode = mdcode;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getKeepalive() {
        return keepalive;
    }

    public void setKeepalive(String keepalive) {
        this.keepalive = keepalive;
    }

    @Override
    public String toString() {
        return "Device = " + "[" + "mdcode:" + mdcode + "]";
    }
}
