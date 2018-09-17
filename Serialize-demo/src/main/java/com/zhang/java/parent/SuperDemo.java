package com.zhang.java.parent;

import java.io.*;

public class SuperDemo {
    public static void main(String[] args) {
        // 序列化
        serializeUser();
        // 反序列化
        User  user = deSerializeUser();
        System.out.println(user);
    }

    private static User deSerializeUser() {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("user")));
            User user = (User) ois.readObject();
            return user;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        return null;
    }

    private static void serializeUser() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("user")));
            User user = new User();
            user.setAge(18);
            user.setName("zhang");
            oos.writeObject(user);
            oos.flush();
            System.out.println("序列化成功");
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
