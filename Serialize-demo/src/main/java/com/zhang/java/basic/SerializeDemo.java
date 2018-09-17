package com.zhang.java.basic;

import java.io.*;

/**
 * 序列化Demo
 */
public class SerializeDemo {
    public static void main(String[] args) {
        // 序列化
        serializePerson();

        // 反序列化
        Person person = deSerializePerson();

        System.out.println(person);
    }


    private static void serializePerson(){
        try {
            // 1. 创建对象输出流
            ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(new File("person")));

            // 2. 序列化对象
            Person person = new Person("zhangyuyang",18);
            oo.writeObject(person);
            oo.flush();

            // 3. 序列化结果
            System.out.println("序列化成功: " + new File("person").length());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Person deSerializePerson(){
        try {
            // 1. 构建输入流
            ObjectInputStream ios = new ObjectInputStream(new FileInputStream(new File("person")));
            // 2. 反序列对象
            Person person = (Person) ios.readObject();
            return person;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
