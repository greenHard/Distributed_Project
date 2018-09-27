package com.zhang.zookeeper.sence.leadMaster;

import java.io.Serializable;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.sence.leadMaster.RunningData
 * @Description:工作服务器信息
 * @create 2018/09/26 14:21
 */
public class RunningData implements Serializable {

    private static final long serialVersionUID = 4035472536095703L;

    private Long cid;

    private String name;

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "RunningData{" +
                "cid=" + cid +
                ", name='" + name + '\'' +
                '}';
    }
}
