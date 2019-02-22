package com.ruisasi.weChat.domain;

import java.io.Serializable;
import java.util.ArrayList;

public class GroupMessage implements Serializable {

    private int type;
    private String content;
    private String[] talkers;
    public GroupMessage(String[] talkers, int type, String content) {
        this.talkers = talkers;
        this.type = type;
        this.content = content;
    }

    public String[] getTalkers() {
        return talkers;
    }

    public void setTalkers(String[] talkers) {
        this.talkers = talkers;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
