package io.github.dzw1113.jvmclass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * @description: 运行时修改字节
 * https://www.cnblogs.com/xiaofuge/p/12868783.html
 * @author: dzw
 * @date: 2021/09/13 16:55
 **/
public class ByteBuddyTest {
    
    public static void main(String[] args) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, IOException {
        System.out.println(Object.class.toString());
        Class<?> dynamicType = new ByteBuddy()
                .subclass(Object.class)
                .method(ElementMatchers.named("toString"))
                .intercept(FixedValue.value("Hello World!"))
                .make()
                .load(ByteBuddyTest.class.getClassLoader())
                .getLoaded();
        System.out.println(dynamicType.newInstance().toString());
        
        DynamicType.Unloaded<?> dynamicType1 = new ByteBuddy()
                .subclass(Object.class)
                .name("org.itstack.demo.bytebuddy.HelloWorld")
                .make();
        outputClazz(dynamicType1.getBytes());
        
        DynamicType.Unloaded<?> dynamicType2 = new ByteBuddy()
                .subclass(Object.class)
                .name("org.itstack.demo.bytebuddy.HelloWorld")
                .defineMethod("main", void.class, Modifier.PUBLIC + Modifier.STATIC)
                .withParameter(String[].class, "args")
                .intercept(FixedValue.value("Hello World!"))
                .make();
        String sourcePath = outputClazz(dynamicType2.getBytes());
        
        System.out.println("=============================");
        DynamicType.Unloaded<?> dynamicType3 = new ByteBuddy()
                .subclass(Object.class)
                .name("org.itstack.demo.bytebuddy.HelloWorld")
                .defineMethod("main", void.class, Modifier.PUBLIC + Modifier.STATIC)
                .withParameter(String[].class, "args")
                .intercept(MethodDelegation.to(Hi.class))
                .make();
        outputClazz(dynamicType3.getBytes());
        
        Class<?> clazz = dynamicType3.load(ByteBuddyTest.class.getClassLoader())
                .getLoaded();
//        dynamicType3.saveIn(new File("e:\\test\\"));
        // 反射调用
        clazz.getMethod("main", String[].class).invoke(clazz.newInstance(), (Object) new String[1]);
    
        System.out.println("=============================");
    }
    
    private static String outputClazz(byte[] bytes) {
        FileOutputStream out = null;
        String pathName = null;
        try {
            pathName = ByteBuddyTest.class.getResource("/").getPath() + "ByteBuddyHelloWorld.class";
            out = new FileOutputStream(new File(pathName));
            System.out.println("类输出路径：" + pathName);
            out.write(bytes);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != out) try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return pathName;
    }
    
    @Override
    public String toString() {
        return "hahahaha";
    }
    
}
