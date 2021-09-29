package io.github.dzw1113.vm;

/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import com.sun.jna.Platform;

import sun.management.VMManagement;


/**
 * @description:https://github.com/arodchen/MaxSim
 * @author: dzw
 * @date: 2021/09/14 16:05
 **/
public final class ProcessTools {
    
    private ProcessTools() {
    }
    
    /**
     * Pumps stdout and stderr from running the process into a String.
     *
     * @param processHandler ProcessHandler to run.
     * @return Output from process.
     * @throws IOException If an I/O error occurs.
     */
    public static OutputBuffer getOutput(ProcessBuilder processBuilder) throws IOException {
        return getOutput(processBuilder.start());
    }
    
    /**
     * Pumps stdout and stderr the running process into a String.
     *
     * @param process Process to pump.
     * @return Output from process.
     * @throws IOException If an I/O error occurs.
     */
    public static OutputBuffer getOutput(Process process) throws IOException {
        ByteArrayOutputStream stderrBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream stdoutBuffer = new ByteArrayOutputStream();
        StreamPumper outPumper = new StreamPumper(process.getInputStream(), stdoutBuffer);
        StreamPumper errPumper = new StreamPumper(process.getErrorStream(), stderrBuffer);
        Thread outPumperThread = new Thread(outPumper);
        Thread errPumperThread = new Thread(errPumper);
        
        outPumperThread.setDaemon(true);
        errPumperThread.setDaemon(true);
        
        outPumperThread.start();
        errPumperThread.start();
        
        try {
            process.waitFor();
            outPumperThread.join();
            errPumperThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        
        return new OutputBuffer(stdoutBuffer.toString(), stderrBuffer.toString());
    }
    
    /**
     * Get the process id of the current running Java process
     *
     * @return Process id
     */
    public static int getProcessId() throws Exception {
        
        // Get the current process id using a reflection hack
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        Field jvm = runtime.getClass().getDeclaredField("jvm");
        
        jvm.setAccessible(true);
        VMManagement mgmt = (sun.management.VMManagement) jvm.get(runtime);
        
        Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
        
        pid_method.setAccessible(true);
        
        int pid = (Integer) pid_method.invoke(mgmt);
        
        return pid;
    }
    
    /**
     * Get platform specific VM arguments (e.g. -d64 on 64bit Solaris)
     *
     * @return String[] with platform specific arguments, empty if there are none
     */
    public static String[] getPlatformSpecificVMArgs() {
        
        if (Platform.is64Bit() && Platform.isSolaris()) {
            return new String[] { "-d64" };
        }
        
        return new String[] {};
    }
    
    /**
     * Create ProcessBuilder using the java launcher from the jdk to be tested and
     * with any platform specific arguments prepended
     */
    public static ProcessBuilder createJavaProcessBuilder(String... command) throws Exception {
        String javapath = getJDKTool("java");
        
        ArrayList<String> args = new ArrayList<>();
        args.add(javapath);
        Collections.addAll(args, getPlatformSpecificVMArgs());
        Collections.addAll(args, command);
        
        return new ProcessBuilder(args.toArray(new String[args.size()]));
        
    }
    
    public static String getJDKTool(String tool) {
        String binPath = System.getProperty("test.jdk");
        if (binPath == null) {
            throw new RuntimeException("System property 'test.jdk' not set. This property is normally set by jtreg. "
                    + "When running test separately, set this property using '-Dtest.jdk=/path/to/jdk'.");
        }
        
        binPath += File.separatorChar + "bin" + File.separatorChar + tool;
        
        return binPath;
    }
    
}

class OutputBuffer {
    private final String stdout;
    private final String stderr;
    
    /**
     * Create an OutputBuffer, a class for storing and managing stdout and stderr
     * results separately
     *
     * @param stdout stdout result
     * @param stderr stderr result
     */
    public OutputBuffer(String stdout, String stderr) {
        this.stdout = stdout;
        this.stderr = stderr;
    }
    
    /**
     * Returns the stdout result
     *
     * @return stdout result
     */
    public String getStdout() {
        return stdout;
    }
    
    /**
     * Returns the stderr result
     *
     * @return stderr result
     */
    public String getStderr() {
        return stderr;
    }
}

class StreamPumper implements Runnable {
    
    private static final int BUF_SIZE = 256;
    
    private final OutputStream out;
    private final InputStream in;
    
    /**
     * Create a StreamPumper that reads from in and writes to out.
     *
     * @param in The stream to read from.
     * @param out The stream to write to.
     */
    public StreamPumper(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }
    
    /**
     * Implements Thread.run(). Continuously read from <code>in</code> and write
     * to <code>out</code> until <code>in</code> has reached end of stream. Abort
     * on interruption. Abort on IOExceptions.
     */
    @Override
    public void run() {
        int length;
        InputStream localIn = in;
        OutputStream localOut = out;
        byte[] buffer = new byte[BUF_SIZE];
        
        try {
            while (!Thread.interrupted() && (length = localIn.read(buffer)) > 0) {
                localOut.write(buffer, 0, length);
            }
        } catch (IOException e) {
            // Just abort if something like this happens.
            e.printStackTrace();
        } finally {
            try {
                localOut.flush();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}