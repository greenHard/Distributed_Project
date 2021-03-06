package com.zhang.java.basic;

import com.baidu.bjf.remoting.protobuf.FieldType;
import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

import java.io.Serializable;

/**
 * 人物
 */
public class Person implements Serializable {
    //4625141069485141003
    private static final long serialVersionUID = -2572627257192440740L;

    private static int height = 2;

    @Protobuf(fieldType = FieldType.STRING,order = 1)
    private String name;

    @Protobuf(fieldType = FieldType.INT32,order = 2)
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Person() {
    }
}
