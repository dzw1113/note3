package io.github.dzw1113.oom;

/**
 * @description:演示栈的溢出
 * VM args：-Xss1m
 * @author: dzw
 * @date: 2021/09/13 15:30
 **/
public class StackSOE {
    
    private static int index = 1;
    
    private static void test() {
        index++;
        test();
    }
    
    public static void main(String[] args) {
        try {
            test();
        }catch (Throwable e){
            System.out.println("Stack deep : "+index);
            e.printStackTrace();
        }
    }
}
