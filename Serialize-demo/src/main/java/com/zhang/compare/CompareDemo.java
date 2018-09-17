package com.zhang.compare;

import com.alibaba.fastjson.JSON;
import com.baidu.bjf.remoting.protobuf.Codec;
import com.baidu.bjf.remoting.protobuf.ProtobufProxy;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.zhang.java.basic.Person;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 各类序列化方式的比较
 */
public class CompareDemo {


    //初始化
    private static Person init() {
        Person person = new Person();
        person.setName("zhang");
        person.setAge(18);
        return person;
    }

    public static void main(String[] args) throws IOException {
        Person person = init();
        // 使用jackson序列化 139ms,大小 25byte
        executeWithJack(person);
        // 使用fastJson 199ms,大小 25byte
        executeWithFastJson(person);
        // 使用protoBuf 速度 25ms,大小 8byte
        executeWithProtoBuf(person);
        // 使用hessian  速度 8ms,大小 58byte
        executeWithHession(person);
    }

    public static void executeWithJack(Person person) {
        ObjectMapper mapper = new ObjectMapper();
        byte[] writeBytes = null;
        try {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++) {
                writeBytes = mapper.writeValueAsBytes(person);
            }
            System.out.println("Jackson序列化: " + (System.currentTimeMillis() - start) + "ms : 总大小 ->" + writeBytes.length);

            Person person1 = mapper.readValue(writeBytes, Person.class);
            System.out.println(person1);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void executeWithFastJson(Person person) {
        String text = null;
        Long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            text = JSON.toJSONString(person);
        }
        System.out.println("fastjson序列化：" + (System.currentTimeMillis() - start) + "ms : " +
                "总大小->" + text.getBytes().length);
    }

    public static void executeWithProtoBuf(Person person) throws IOException {
        // 1. 创建protoBuf代理
        Codec<Person> personCodec = ProtobufProxy.create(Person.class, false);
        Long start = System.currentTimeMillis();
        byte[] bytes = null;
        for (int i = 0; i < 10000; i++) {
            // 序列化
            bytes = personCodec.encode(person);
        }
        System.out.println("protobuf序列化：" + (System.currentTimeMillis() - start) + "ms : " +
                "总大小->" + bytes.length);
        // 反序列化
        Person person1 = personCodec.decode(bytes);
        System.out.println(person1);
    }


    public static void executeWithHession(Person person) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HessianOutput ho = new HessianOutput(baos);
        Long start=System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            ho.writeObject(person);
            if(i==0){
                System.out.println(baos.toByteArray().length);
            }
        }
        System.out.println("Hessian序列化："+(System.currentTimeMillis()-start)+"ms : " +
                "总大小->"+baos.toByteArray().length);

        // 反序列化
        HessianInput hi = new HessianInput(new ByteArrayInputStream(baos.toByteArray()));
        Person person1 = (Person) hi.readObject();
        System.out.println(person1);

    }
}
