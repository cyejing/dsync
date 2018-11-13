package cn.cyejing.dsync.toolkit;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-11-12 17:18
 **/
public class Config {

    private String host = "localhost";
    private int port = 4843;

    public static Config config() {
        return new Config();
    }

    public Config host(String host) {
        this.host = host;
        return this;
    }

    public Config port(int port) {
        this.port = port;
        return this;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
