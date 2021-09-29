package io.github.dzw1113.oom;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:演示元空间的溢出
 * VM args：-XX:MetaspaceSize=16m -XX:MaxMetaspaceSize=16m
 * @author: dzw
 * @date: 2021/09/13 15:21
 **/
public class MeteSpaceOOMTest1 {
    
    public static void main(String[] args) {
        
        List<ClassLoader> classLoaderList = new ArrayList<>();
        while (true) {
            ClassLoader loader = new URLClassLoader(new URL[]{});
            Facade t = (Facade) Proxy.newProxyInstance(loader, new Class<?>[]{Facade.class}, new MetaspaceFacadeInvocationHandler(new FacadeImpl()));
            classLoaderList.add(loader);
        }
    }
    
    public interface Facade {
    }
    
    public static class FacadeImpl implements Facade {
    }
    
    public static class MetaspaceFacadeInvocationHandler implements InvocationHandler {
        private Object impl;
        
        public MetaspaceFacadeInvocationHandler(Object impl) {
            this.impl = impl;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(impl, args);
        }
    }
}
