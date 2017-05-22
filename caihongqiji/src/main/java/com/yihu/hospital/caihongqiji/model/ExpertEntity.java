package com.yihu.hospital.caihongqiji.model;

/**
 * Created by onekey on 2017/4/5.
 */

public class ExpertEntity {

    private String id;

    private int role; // 主播1 成员0 上麦成员2

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "ExpertEntity{" +
                "id='" + id + '\'' +
                ", role=" + role +
                '}';
    }
}
