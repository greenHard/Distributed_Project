package com.zhang.java.parent;


import java.io.Serializable;

public class User extends SuperUser implements Serializable {

    private static final long serialVersionUID = 6244837929799767391L;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                '}';
    }
}
