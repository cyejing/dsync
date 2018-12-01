package cn.cyejing.dsync.toolkit;

/**
 *
 * @author Born
 */
public class Config {

    /**
     * 服务地址
     */
    private String host = "localhost";
    /**
     * 服务端口
     */
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

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
