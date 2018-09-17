package com.zhang.java.clone;

import java.io.Serializable;

public class Teacher implements Serializable {
    private static final long serialVersionUID = -1;

    private String name;

    @Override
    public String toString() {
        return "Teacher{" +
                "name='" + name + '\'' +
                '}';
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
