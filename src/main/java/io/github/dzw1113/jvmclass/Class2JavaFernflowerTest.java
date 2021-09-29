package io.github.dzw1113.jvmclass;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import fudge.forgedflower.main.Fernflower;
import fudge.forgedflower.main.decompiler.PrintStreamLogger;

/**
 * @description: class转换成java
 * @author: dzw
 * @date: 2021/09/17 14:07
 **/
public class Class2JavaFernflowerTest {
    
    public static void main(String[] args) {
        PrintStreamLogger logger = new PrintStreamLogger(System.out);
    
        Map<String, Object> mapOptions = new HashMap<>();
        mapOptions.put("dgs","true");
    
        File destination = new File("E:\\test\\source");
        
        ConsoleDecompiler consoleDecompiler = new ConsoleDecompiler(destination,mapOptions,logger);
        Fernflower fernflower = new Fernflower(consoleDecompiler, consoleDecompiler, mapOptions, logger);
        try {
            fernflower.getStructContext().addSpace(new File("E:\\test"), true);
            fernflower.decompileContext();
        } finally {
            fernflower.clearContext();
        }
    }
}
