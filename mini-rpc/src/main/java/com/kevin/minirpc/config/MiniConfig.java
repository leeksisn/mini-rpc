package com.kevin.minirpc.config;

import java.util.ResourceBundle;

/**
 * @author kevin.lee
 * @date 2021/1/6 0006
 */
public class MiniConfig {

    public static String host;
    public static Integer port;

    static {
        ResourceBundle bundle = ResourceBundle.getBundle("miniRPC");
        host = bundle.getString("miniRPC.host");
        port = Integer.parseInt(bundle.getString("miniRPC.port"));
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
