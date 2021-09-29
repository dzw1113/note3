package io.github.dzw1113.oom;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * @description:演示直接内存的溢出
 * VM args:-Xmx20M -XX:MaxDirectMemorySize=10M
 * @author: dzw
 * @date: 2021/09/13 15:24
 **/
public class DirectMemoryOOM {
    public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
        List<ByteBuffer> list = new LinkedList<>();
        while (true) {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024 * 1024);
            list.add(byteBuffer);
        }
        
    }
}
