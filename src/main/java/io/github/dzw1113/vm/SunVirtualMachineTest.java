package io.github.dzw1113.vm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import sun.tools.attach.HotSpotVirtualMachine;

/**
 * @description:https://github.com/arodchen/MaxSim
 * @author: dzw
 * @date: 2021/09/14 13:07
 **/
public class SunVirtualMachineTest {
    
    private static final String TARGET_CLASS = "com.xtt.base.datasource.DruidConfiguration";
    static String pid = "24452";
    
    private static HotSpotVirtualMachine vm = null;
    
    static {
        try {
            vm = (HotSpotVirtualMachine) VirtualMachine.attach(pid);
        } catch (AttachNotSupportedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws Exception {
//        runTest();
        //jinfo -flag OldSize 24452
//        testGetFlag("OldSize");
//        getInstanceCountFromHeapHisto();
//        testSetFlag("OldSize", "179306498");
        testDumheap();
        vm.detach();
    }
    
    /**
     * 导出Full thread dump 指令：jstack
     *
     * @return
     * @throws Exception
     */
    public static boolean runTest() throws Exception {
        BufferedReader remoteDataReader = new BufferedReader(new InputStreamReader(vm.remoteDataDump()));
        String line = null;
        while ((line = remoteDataReader.readLine()) != null) {
            System.out.println(line);
        }
        return true;
    }
    
    /**
     * 获取flag
     *
     * @param flagName
     * @throws Exception
     */
    public static void testGetFlag(String flagName) throws Exception {
        BufferedReader remoteDataReader = new BufferedReader(new InputStreamReader(
                vm.printFlag(flagName)));
        String line = null;
        while ((line = remoteDataReader.readLine()) != null) {
            System.out.println("printFlag: " + line);
        }
    }
    
    /**
     * 设置flag
     *
     * @param flagName
     * @param flagValue
     * @throws Exception
     */
    public static void testSetFlag(String flagName, String flagValue) throws Exception {
        BufferedReader remoteDataReader = new BufferedReader(new InputStreamReader(
                vm.setFlag(flagName, flagValue)));
        
        String line;
        while ((line = remoteDataReader.readLine()) != null) {
            System.out.println("setFlag: " + line);
        }
        remoteDataReader.close();
        
        remoteDataReader = new BufferedReader(new InputStreamReader(vm.printFlag(flagName)));
        
        line = null;
        while ((line = remoteDataReader.readLine()) != null) {
            System.out.println("getFlag: " + line);
        }
    }
    
    
    /**
     * 打印每个class的实例数目,内存占用,类全名信息,用于堆的dump，jmap会用到
     *
     * @return
     * @throws Exception
     */
    private static int getInstanceCountFromHeapHisto() throws Exception {
        int instanceCount = 0;
        try (InputStream heapHistoStream = vm.heapHisto("-live");
             BufferedReader in = new BufferedReader(new InputStreamReader(heapHistoStream))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
                if (inputLine.contains(TARGET_CLASS)) {
                    instanceCount = Integer.parseInt(inputLine
                            .split("[ ]+")[2]);
                    System.out.println("instance count: " + instanceCount);
                    break;
                }
            }
        }
        
        return instanceCount;
    }
    
    public static void testDumheap() throws Exception {
        int instanceCount = 0;
        try (InputStream heapHistoStream = vm.dumpHeap();
             BufferedReader in = new BufferedReader(new InputStreamReader(heapHistoStream))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
            }
        }
    }
    
}
