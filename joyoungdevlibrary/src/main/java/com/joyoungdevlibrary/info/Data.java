package com.joyoungdevlibrary.info;

/**
 * Created by liuwei on 2017/12/6.
 */

public class Data {

    private String devId;
    private String devTypeId;
    private String data;
    private String func;
    private String seq;
    private String ord;


    public Data(String devId, String devTypeId, String func, String seq) {
        this.devId = devId;
        this.devTypeId = devTypeId;
        this.func = func;
        this.seq = seq;
    }

    public Data(String devId, String devTypeId, String data, String func, String seq, String ord) {
        this.devId = devId;
        this.devTypeId = devTypeId;
        this.data = data;
        this.func = func;
        this.seq = seq;
        this.ord = ord;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getDevTypeId() {
        return devTypeId;
    }

    public void setDevTypeId(String devTypeId) {
        this.devTypeId = devTypeId;
    }

    public String getFunc() {
        return func;
    }

    public void setFunc(String func) {
        this.func = func;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getOrd() {
        return ord;
    }

    public void setOrd(String ord) {
        this.ord = ord;
    }


}
