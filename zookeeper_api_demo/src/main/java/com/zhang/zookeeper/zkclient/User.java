package com.zhang.zookeeper.zkclient;

import java.io.Serializable;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.zkclient.User
 * @Description: 用户类
 * @create 2018/09/26 11:22
 */
public class User implements Serializable {

    private static final long serialVersionUID = 4058590478931819757L;

    private String name;

    private Integer age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
