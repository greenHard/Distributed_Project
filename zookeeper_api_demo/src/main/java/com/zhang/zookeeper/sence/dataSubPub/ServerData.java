package com.zhang.zookeeper.sence.dataSubPub;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.sence.dataSubPub.ServerData
 * @Description: 服务器基本信息
 * @create 2018/09/26 15:20
 */
public class ServerData {
    private String address;

    private String name;

    private Integer id;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ServerData{" +
                "address='" + address + '\'' +
                ", name='" + name + '\'' +
                ", id=" + id +
                '}';
    }
}
