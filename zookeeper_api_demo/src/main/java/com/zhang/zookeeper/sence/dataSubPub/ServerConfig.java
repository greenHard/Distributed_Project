package com.zhang.zookeeper.sence.dataSubPub;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.sence.dataSubPub.ServerConfig
 * @Description: 配置信息
 * @create 2018/09/26 15:16
 */
public class ServerConfig {

    private String dbUrl;

    private String dbPwd;

    private String dbUser;

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbPwd() {
        return dbPwd;
    }

    public void setDbPwd(String dbPwd) {
        this.dbPwd = dbPwd;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    @Override
    public String toString() {
        return "ServerConfig{" +
                "dbUrl='" + dbUrl + '\'' +
                ", dbPwd='" + dbPwd + '\'' +
                ", dbUser='" + dbUser + '\'' +
                '}';
    }
}
