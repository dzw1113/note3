package io.github.dzw1113.oom;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: oom测试
 * VM args: -Xms20m -Xmx20m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=c:\dump\heap.hprof -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
 * @author: dzw
 * @date: 2021/09/13 15:17
 **/
public class HeapOOM1 {
    public static void main(String[] args) throws Exception {
        List<byte[]> list = new ArrayList<>();
        while (true) {
            list.add(new byte[1024 * 1024]); // 每次增加一个1M大小的数组对象
        }
        
    }
    
}
