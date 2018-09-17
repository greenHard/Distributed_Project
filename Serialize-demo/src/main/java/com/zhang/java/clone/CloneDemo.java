package com.zhang.java.clone;

import java.io.IOException;

public class CloneDemo {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Teacher teacher = new Teacher();
        teacher.setName("mic");

        Student student = new Student();
        student.setAge(18);
        student.setName("zhang");
        student.setTeacher(teacher);

        Student student1 = (Student) student.deepClone();
        System.out.println(student);

        student1.getTeacher().setName("tom");
        System.out.println(student1);
        System.out.println(student);

    }
}
