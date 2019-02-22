package com.ruisasi.weChat.domain;

public class Contact {
    //      Log.i("TAG","username :"+username+" nickname :"+nickname+" type:"+type+" conremark:"+conRemark);
    private String username;
    private String nickname;
    private String type;
    private String alias;
    private String conremark;
    private String chatroomFlag;

    public Contact(String username, String nickname, String type, String alias, String conremark, String chatroomFlag) {
        this.username = username;
        this.nickname = nickname;
        this.type = type;
        this.alias = alias;
        this.conremark = conremark;
        this.chatroomFlag = chatroomFlag;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setConremark(String conremark) {
        this.conremark = conremark;
    }

    public void setChatroomFlag(String chatroomFlag) {
        this.chatroomFlag = chatroomFlag;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getType() {
        return type;
    }

    public String getAlias() {
        return alias;
    }

    public String getConremark() {
        return conremark;
    }

    public String getChatroomFlag() {
        return chatroomFlag;
    }
}
