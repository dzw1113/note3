package io.github.dzw1113.oom;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * @description:演示本地内存的溢出
 * VM args： -Xmx20M -XX:MaxDirectMemorySize=10M
 * @author: dzw
 * @date: 2021/09/13 15:24
 **/
public class LocalMemoryOOM {
    
    public static void main(String[] args) throws IllegalAccessException {
        Field field = Unsafe.class.getDeclaredFields()[0];
        field.setAccessible(true);
        
        Unsafe unsafe = (Unsafe) field.get(null);
        
        while (true) {
            unsafe.allocateMemory(1024 * 1024);
        }
    }
}
