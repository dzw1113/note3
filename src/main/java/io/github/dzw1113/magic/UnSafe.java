package io.github.dzw1113.magic;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sun.misc.Unsafe;

/**
 * @description:基础类
 * @author: dzw
 * @date: 2021/09/07 17:34
 **/
public class UnSafe {
    
    public static void main(String[] args) throws Throwable {
        Unsafe unsafe = getUnsafe();
        /*
        调整新本机内存块的大小，以字节为单位。新块的内容超过旧块的大小是未初始化的;它们通常都是垃圾。当且仅当请求
        的大小为零时，生成的本机指针将为零。生成的本机指针将对所有值类型进行对齐。通过调用freeMemory(long)来释
        放这个内存，或者使用reallocatemmemory (long, long)来调整它的大小。传递给该方法的地址可能为空，在这
        种情况下将执行分配。
        */
        /*分配一个新的本机内存块，其大小以字节为单位。内存的内容未初始化;它们通常都是垃圾。生成的本机指针永远不会为
        零，并且会对所有值类型进行对齐。通过调用freeMemory(long)来释放这个内存，或者使用reallocateMemory
        (long, long)来调整它的大小。*/
//        offset();
        List<byte[]> list = new ArrayList<>();
        while (true) {
            list.add(new byte[1024 * 1024]); // 每次增加一个1M大小的数组对象
        }
    
    }
    
    private static Unsafe getUnsafe() throws Throwable {
        Class<?> unsafeClass = Unsafe.class;
        for (Field f : unsafeClass.getDeclaredFields()) {
            if ("theUnsafe".equals(f.getName())) {
                f.setAccessible(true);
                return (Unsafe) f.get(null);
            }
        }
        throw new IllegalAccessException("no declared field: theUnsafe");
    }
    
    private static void offset() throws Throwable {
        Unsafe unsafe = getUnsafe();
        UnsafeTestKlass unsafeTestKlass = new UnsafeTestKlass();
        UnsafeTestKlass unsafeTestKlass1 = new UnsafeTestKlass();
        System.out.println(unsafeTestKlass.hashCode());
        System.out.println(unsafeTestKlass1.hashCode());
        System.out.println("==============offset==================");
        //objectFieldOffset方法用于获取非静态属性Field在对象实例中的偏移量，读写对象的非静态属性时会用到这个偏移量
        long aOffset = unsafe.objectFieldOffset(UnsafeTestKlass.class.getDeclaredField("a"));
        long bOffset = unsafe.objectFieldOffset(UnsafeTestKlass.class.getDeclaredField("b"));
        long personOffset = unsafe.objectFieldOffset(UnsafeTestKlass.class.getDeclaredField("person"));
        long arrOffset = unsafe.objectFieldOffset(UnsafeTestKlass.class.getDeclaredField("arr"));
        System.out.println("==============static offset==================");
        //staticFieldOffset方法用于获取静态属性Field在对象中的偏移量，读写静态属性时必须获取其偏移量
        //staticFieldBase方法用于返回Field所在的对象
        Field field = UnsafeTestKlass.class.getDeclaredField("staticName");
        long staticNameOffset = unsafe.staticFieldOffset(UnsafeTestKlass.class.getDeclaredField("staticName"));
        Object base = unsafe.staticFieldBase(field);
        System.out.println(unsafe.getObject(base, staticNameOffset));
        unsafe.putObject(base, staticNameOffset, "修改后静态名称");
        // 修改后
        System.out.println(unsafe.getObject(base, staticNameOffset));
        System.out.println("============find value by offset====================");
        long textOffset = unsafe.objectFieldOffset(UnsafeTestKlass.class.getDeclaredField("text"));
        System.out.println(unsafe.objectFieldOffset(UnsafeTestKlass.class.getDeclaredField("text")));
        System.out.println("a在内存地址的偏移量:" + aOffset);
        System.out.println("b在内存地址的偏移量:" + bOffset);
        System.out.println("person在内存地址的偏移量:" + personOffset);
        System.out.println("text在内存地址的偏移量:" + textOffset);
        System.out.println("arr在内存地址的偏移量:" + arrOffset);
        System.out.println("a在内存地址的偏移量值:" + unsafe.getInt(unsafeTestKlass, aOffset));//a
        System.out.println("b在内存地址的偏移量值:" + unsafe.getInt(unsafeTestKlass, bOffset));//b
        System.out.println("person在内存地址的偏移量值:" + unsafe.getInt(unsafeTestKlass, personOffset));//person
        System.out.println("================================");
        Person person = new Person();
        System.out.println("person在内存地址的偏移量值:" + unsafe.getAndSetObject(unsafeTestKlass, personOffset, person));
        String text = null;
        System.out.println("text在内存地址的偏移量值:" + unsafe.getAndSetObject(unsafeTestKlass, textOffset, text));//text
        unsafeTestKlass.setA(1999);
        System.out.println("a改变后在内存地址的偏移量:" + aOffset);
        System.out.println("a改变后在内存地址的偏移量值:" + unsafe.getInt(unsafeTestKlass, aOffset));//a
        System.out.println("=============array===================");
        //arrayBaseOffset方法用于返回数组中第一个元素实际地址相对整个数组对象的地址的偏移量。arrayIndexScale方法用于计算数组中第一个元素所占用的内存空间。
        Person[] persons = new Person[5];
        long personsOffset = unsafe.arrayBaseOffset(persons.getClass());
        System.out.println("persons的长度偏移量：" + personsOffset);
        // 每个对象所占的长度
        long scale = unsafe.arrayIndexScale(persons.getClass());
        System.out.println("persons的长度：" + scale);
        unsafe.putObject(persons, personsOffset, new UnsafeTestKlass(399, 399, new Person("张三3", 33)));
        unsafe.putObject(persons, personsOffset + 2 * scale, new UnsafeTestKlass(499, 499, new Person("张三4", 34)));
        System.out.println(Arrays.toString(persons));
    }
    
}

