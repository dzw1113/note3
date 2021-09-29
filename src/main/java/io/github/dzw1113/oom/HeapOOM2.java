package io.github.dzw1113.oom;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @description: oom测试
 * VM args: VM args: -Xms10m -Xmx10m -XX:PrintGCDetails
 * @author: dzw
 * @date: 2021/09/13 15:17
 **/
public class HeapOOM2 {
    
    public static void main(String[] args) throws Exception {
        List<Object> list = new LinkedList<>();
        int i = 0;
        while (true) {
            i++;
            if (0 == i % 1000) {
                TimeUnit.MILLISECONDS.sleep(10);
            }
            list.add(new Object());
        }
    }
    
}
