package com.zhang.java.parent;


/**
 * 如果父类没有实现序列化，而子类实现列序列化。那么父类中的成员没办法做序列化操作
 */
public class SuperUser{

    private int age;

    @Override
    public String toString() {
        return "SuperUser{" +
                "age=" + age +
                '}';
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
