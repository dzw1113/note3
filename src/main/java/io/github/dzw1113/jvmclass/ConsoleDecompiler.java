package io.github.dzw1113.jvmclass;



import fudge.forgedflower.main.DecompilerContext;
import fudge.forgedflower.main.Fernflower;
import fudge.forgedflower.main.decompiler.PrintStreamLogger;
import fudge.forgedflower.main.decompiler.SingleFileSaver;
import fudge.forgedflower.main.extern.IBytecodeProvider;
import fudge.forgedflower.main.extern.IFernflowerLogger;
import fudge.forgedflower.main.extern.IResultSaver;
import fudge.forgedflower.main.extern.IFernflowerLogger.Severity;
import fudge.forgedflower.util.InterpreterUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @description:https://github.com/JetBrains/intellij-community/tree/master/plugins/java-decompiler/engine
 * @author: dzw
 * @date: 2021/09/14 10:30
 **/
public class ConsoleDecompiler implements IBytecodeProvider, IResultSaver {
    private final File root;
    private final Fernflower fernflower;
    private final Map<String, ZipOutputStream> mapArchiveStreams = new HashMap();
    private final Map<String, Set<String>> mapArchiveEntries = new HashMap();
    
    private static void addPath(List<File> list, String path) {
        File file = new File(path);
        if (file.exists()) {
            list.add(file);
        } else {
            System.out.println("warn: missing '" + path + "', ignored");
        }
        
    }
    
    protected ConsoleDecompiler(File destination, Map<String, Object> options, IFernflowerLogger logger) {
        this.root = destination;
        this.fernflower = new Fernflower(this, (IResultSaver)(this.root.isDirectory() ? this : new SingleFileSaver(destination)), options, logger);
    }
    
    public void addSpace(File file, boolean isOwn) {
        this.fernflower.getStructContext().addSpace(file, isOwn);
    }
    
    public void decompileContext() {
        try {
            this.fernflower.decompileContext();
        } finally {
            this.fernflower.clearContext();
        }
        
    }
    
    public static void main(String[] args) {
        List<String> params = new ArrayList();
    
        for(int x = 0; x < args.length; ++x) {
            if (args[x].startsWith("-cfg")) {
                String path = null;
                if (args[x].startsWith("-cfg=")) {
                    path = args[x].substring(5);
                } else {
                    if (args.length <= x + 1) {
                        System.out.println("Must specify a file when using -cfg argument.");
                        return;
                    }
                
                    ++x;
                    path = args[x];
                }
            
                Path file = Paths.get(path);
                if (!Files.exists(file, new LinkOption[0])) {
                    System.out.println("error: missing config '" + path + "'");
                    return;
                }
            
                try {
                    Stream<String> stream = Files.lines(file);
                    Throwable var6 = null;
                
                    try {
                        stream.forEach(params::add);
                    } catch (Throwable var17) {
                        var6 = var17;
                        throw var17;
                    } finally {
                        if (stream != null) {
                            if (var6 != null) {
                                try {
                                    stream.close();
                                } catch (Throwable var16) {
                                    var6.addSuppressed(var16);
                                }
                            } else {
                                stream.close();
                            }
                        }
                    
                    }
                } catch (IOException var19) {
                    System.out.println("error: Failed to read config file '" + path + "'");
                    throw new RuntimeException(var19);
                }
            } else {
                params.add(args[x]);
            }
        }
    
        args = (String[])params.toArray(new String[params.size()]);
        if (args.length < 2) {
            System.out.println("Usage: java -jar fernflower.jar [-<option>=<value>]* [<source>]+ <destination>\nExample: java -jar fernflower.jar -dgs=true c:\\my\\source\\ c:\\my.jar d:\\decompiled\\");
        } else {
            Map<String, Object> mapOptions = new HashMap();
            List<File> lstSources = new ArrayList();
            List<File> lstLibraries = new ArrayList();
            boolean isOption = true;
        
            for(int i = 0; i < args.length - 1; ++i) {
                String arg = args[i];
                if (isOption && arg.length() > 5 && arg.charAt(0) == '-' && arg.charAt(4) == '=') {
                    String value = arg.substring(5);
                    if ("true".equalsIgnoreCase(value)) {
                        value = "1";
                    } else if ("false".equalsIgnoreCase(value)) {
                        value = "0";
                    }
                
                    mapOptions.put(arg.substring(1, 4), value);
                } else {
                    isOption = false;
                    if (arg.startsWith("-e=")) {
                        addPath(lstLibraries, arg.substring(3));
                    } else {
                        addPath(lstSources, arg);
                    }
                }
            }
        
            if (lstSources.isEmpty()) {
                System.out.println("error: no sources given");
            } else {
                File destination = new File(args[args.length - 1]);
                if (destination.isDirectory() || lstSources.size() <= 1 && ((File)lstSources.get(0)).isFile()) {
                    PrintStreamLogger logger = new PrintStreamLogger(System.out);
                    ConsoleDecompiler decompiler = new ConsoleDecompiler(destination, mapOptions, logger);
                    Iterator var9 = lstLibraries.iterator();
                
                    File source;
                    while(var9.hasNext()) {
                        source = (File)var9.next();
                        decompiler.addSpace(source, false);
                    }
                
                    var9 = lstSources.iterator();
                
                    while(var9.hasNext()) {
                        source = (File)var9.next();
                        decompiler.addSpace(source, true);
                    }
                
                    decompiler.decompileContext();
                } else {
                    System.out.println("error: destination '" + destination + "' is not a directory");
                }
            }
        }
    }
    
    public byte[] getBytecode(String externalPath, String internalPath) throws IOException {
        File file = new File(externalPath);
        if (internalPath == null) {
            return InterpreterUtil.getBytes(file);
        } else {
            ZipFile archive = new ZipFile(file);
            Throwable var5 = null;
            
            byte[] var7;
            try {
                ZipEntry entry = archive.getEntry(internalPath);
                if (entry == null) {
                    throw new IOException("Entry not found: " + internalPath);
                }
                
                var7 = InterpreterUtil.getBytes(archive, entry);
            } catch (Throwable var16) {
                var5 = var16;
                throw var16;
            } finally {
                if (archive != null) {
                    if (var5 != null) {
                        try {
                            archive.close();
                        } catch (Throwable var15) {
                            var5.addSuppressed(var15);
                        }
                    } else {
                        archive.close();
                    }
                }
                
            }
            
            return var7;
        }
    }
    
    private String getAbsolutePath(String path) {
        return (new File(this.root, path)).getAbsolutePath();
    }
    
    public void saveFolder(String path) {
        File dir = new File(this.getAbsolutePath(path));
        if (!dir.mkdirs() && !dir.isDirectory()) {
            throw new RuntimeException("Cannot create directory " + dir);
        }
    }
    
    public void copyFile(String source, String path, String entryName) {
        try {
            InterpreterUtil.copyFile(new File(source), new File(this.getAbsolutePath(path), entryName));
        } catch (IOException var5) {
            DecompilerContext.getLogger().writeMessage("Cannot copy " + source + " to " + entryName, var5);
        }
        
    }
    
    public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
        File file = new File(this.getAbsolutePath(path), entryName);
        
        try {
            Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF8");
            Throwable var8 = null;
            
            try {
                out.write(content);
            } catch (Throwable var18) {
                var8 = var18;
                throw var18;
            } finally {
                if (out != null) {
                    if (var8 != null) {
                        try {
                            out.close();
                        } catch (Throwable var17) {
                            var8.addSuppressed(var17);
                        }
                    } else {
                        out.close();
                    }
                }
                
            }
        } catch (IOException var20) {
            DecompilerContext.getLogger().writeMessage("Cannot write class file " + file, var20);
        }
        
    }
    
    public void createArchive(String path, String archiveName, Manifest manifest) {
        File file = new File(this.getAbsolutePath(path), archiveName);
        
        try {
            if (!file.createNewFile() && !file.isFile()) {
                throw new IOException("Cannot create file " + file);
            }
            
            FileOutputStream fileStream = new FileOutputStream(file);
            ZipOutputStream zipStream = manifest != null ? new JarOutputStream(fileStream, manifest) : new ZipOutputStream(fileStream);
            this.mapArchiveStreams.put(file.getPath(), zipStream);
        } catch (IOException var7) {
            DecompilerContext.getLogger().writeMessage("Cannot create archive " + file, var7);
        }
        
    }
    
    public void saveDirEntry(String path, String archiveName, String entryName) {
        this.saveClassEntry(path, archiveName, (String)null, entryName, (String)null);
    }
    
    public void copyEntry(String source, String path, String archiveName, String entryName) {
        String file = (new File(this.getAbsolutePath(path), archiveName)).getPath();
        if (this.checkEntry(entryName, file)) {
            try {
                ZipFile srcArchive = new ZipFile(new File(source));
                Throwable var40 = null;
                
                try {
                    ZipEntry entry = srcArchive.getEntry(entryName);
                    if (entry != null) {
                        InputStream in = srcArchive.getInputStream(entry);
                        Throwable var10 = null;
                        
                        try {
                            ZipOutputStream out = (ZipOutputStream)this.mapArchiveStreams.get(file);
                            out.putNextEntry(new ZipEntry(entryName));
                            InterpreterUtil.copyStream(in, out);
                        } catch (Throwable var35) {
                            var10 = var35;
                            throw var35;
                        } finally {
                            if (in != null) {
                                if (var10 != null) {
                                    try {
                                        in.close();
                                    } catch (Throwable var34) {
                                        var10.addSuppressed(var34);
                                    }
                                } else {
                                    in.close();
                                }
                            }
                            
                        }
                    }
                } catch (Throwable var37) {
                    var40 = var37;
                    throw var37;
                } finally {
                    if (srcArchive != null) {
                        if (var40 != null) {
                            try {
                                srcArchive.close();
                            } catch (Throwable var33) {
                                var40.addSuppressed(var33);
                            }
                        } else {
                            srcArchive.close();
                        }
                    }
                    
                }
            } catch (IOException var39) {
                String message = "Cannot copy entry " + entryName + " from " + source + " to " + file;
                DecompilerContext.getLogger().writeMessage(message, var39);
            }
            
        }
    }
    
    public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
        String file = (new File(this.getAbsolutePath(path), archiveName)).getPath();
        if (this.checkEntry(entryName, file)) {
            try {
                ZipOutputStream out = (ZipOutputStream)this.mapArchiveStreams.get(file);
                out.putNextEntry(new ZipEntry(entryName));
                if (content != null) {
                    out.write(content.getBytes("UTF-8"));
                }
            } catch (IOException var9) {
                String message = "Cannot write entry " + entryName + " to " + file;
                DecompilerContext.getLogger().writeMessage(message, var9);
            }
            
        }
    }
    
    private boolean checkEntry(String entryName, String file) {
        Set<String> set = (Set)this.mapArchiveEntries.computeIfAbsent(file, (k) -> {
            return new HashSet();
        });
        boolean added = set.add(entryName);
        if (!added) {
            String message = "Zip entry " + entryName + " already exists in " + file;
            DecompilerContext.getLogger().writeMessage(message, Severity.WARN);
        }
        
        return added;
    }
    
    public void closeArchive(String path, String archiveName) {
        String file = (new File(this.getAbsolutePath(path), archiveName)).getPath();
        
        try {
            this.mapArchiveEntries.remove(file);
            ((ZipOutputStream)this.mapArchiveStreams.remove(file)).close();
        } catch (IOException var5) {
            DecompilerContext.getLogger().writeMessage("Cannot close " + file, Severity.WARN);
        }
        
    }
}

