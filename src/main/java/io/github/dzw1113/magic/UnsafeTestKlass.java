package io.github.dzw1113.magic;

import lombok.Data;

/**
 * @description:
 * @author: dzw
 * @date: 2021/09/08 16:06
 **/
@Data
public class UnsafeTestKlass {
    private int a;
    
    private long b;
    
    private Person person;
    
    private String text = "说明";
    
    private static String staticName = "静态名称";
    
    private String[] arr;
    
    public UnsafeTestKlass() {
        this(199,299,new Person("张三",30));
    }
    
    public UnsafeTestKlass(int a,long b,Person person) {
        this.a = a;
        this.b = b;
        this.person = person;
    }
    
    
    public long a() {
        return a;
    }
    
}

@Data
class Person{
    String name;
    
    Integer age;
    
    public Person(){
    
    }
    
    public Person(String name,Integer age){
        this.name = name;
        this.age = age;
    }
    
}
