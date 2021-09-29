package io.github.dzw1113.vm;

import java.io.IOException;

import com.sun.tools.attach.AttachNotSupportedException;

import net.bytebuddy.agent.VirtualMachine;

/**
 * @description:
 * @author: dzw
 * @date: 2021/09/14 13:07
 **/
public class ByteBuddyVirtualMachineTest {
    
    public static void main(String[] args) throws IOException, AttachNotSupportedException {
        VirtualMachine virtualMachine = VirtualMachine.ForHotSpot.attach("24452");
        System.out.println(virtualMachine.getSystemProperties());
        System.out.println(virtualMachine.getAgentProperties());
        virtualMachine.detach();
    }
    
}
