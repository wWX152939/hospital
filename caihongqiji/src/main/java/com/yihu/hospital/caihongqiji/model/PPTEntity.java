package com.yihu.hospital.caihongqiji.model;

/**
 * Created by onekey on 2017/4/5.
 */

public class PPTEntity {

    private String name;

    private String uuid;

    private String server_url;

    private String customer_url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getServer_url() {
        return server_url;
    }

    public void setServer_url(String server_url) {
        this.server_url = server_url;
    }

    public String getCustomer_url() {
        return customer_url;
    }

    public void setCustomer_url(String customer_url) {
        this.customer_url = customer_url;
    }

    @Override
    public String toString() {
        return "PPTEntity{" +
                "name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", server_url='" + server_url + '\'' +
                ", customer_url='" + customer_url + '\'' +
                '}';
    }
}
