/*
 * Copyright (c) 2011 - Georgios Gousios <gousiosg@gmail.com>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package local.happysixplus.backendcodeanalysis.callgraph.stat;
import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.bcel.classfile.ClassParser;

/**
 * Constructs a callgraph out of a JAR archive. Can combine multiple archives
 * into a single call graph.
 *
 * @author Georgios Gousios <gousiosg@gmail.com>
 */
public class JCallGraph {
    private static ArrayList<String> packageNames=new ArrayList<>();

    public static void main(String[] args) {

        String arg="//Users//tianduyingcai//Desktop//GIT//Hello//target//Hello-1.0-SNAPSHOT.jar";
        /*if(args.length>0)
            packageNames = new String[args.length - 1];
        packageNames=new String[1];
        packageNames[0]="temp";*/
        Function<ClassParser, ClassVisitor> getClassVisitor = (ClassParser cp) -> {
            try {
                return new ClassVisitor(cp.parse());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };

        try {
            //String arg = args[0];
            
            

            File f = new File(arg);

            if (!f.exists()) {
                System.err.println("Jar file " + arg + " does not exist");
            }

            try (JarFile jar = new JarFile(f)) {
                Stream<JarEntry> entries = enumerationAsStream(jar.entries());


                String methodCalls = entries.flatMap(e -> {
                    if (e.isDirectory() || !e.getName().endsWith(".class")){
                        return (new ArrayList<String>()).stream();
                    }
                    String str=e.getName().split("/")[0];
                    if(packageNames.indexOf(str)==-1){
                        packageNames.add(str);
                    }
                    ClassParser cp = new ClassParser(arg, e.getName());
                    return getClassVisitor.apply(cp).start().methodCalls().stream().filter(String->isValid(String)==true);
                }).map(s -> s + "\n").reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append)
                        .toString();

                BufferedWriter log = new BufferedWriter(new OutputStreamWriter(System.out));
                log.write(methodCalls);
                //log.write("???");
                log.close();
               

            }
        } catch (IOException e) {
            System.err.println("Error while processing jar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static <T> Stream<T> enumerationAsStream(Enumeration<T> e) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<T>() {
            public T next() {
                return e.nextElement();
            }

            public boolean hasNext() {
                return e.hasMoreElements();
            }
        }, Spliterator.ORDERED), false);
    }

    public static boolean isValid(String string) {
        String str=string.split(" ")[1].substring(3);
        for(int i=0;i<packageNames.size();i++){
            if(str.startsWith(packageNames.get(i))){
                return true;
            }
        }
        return false;
    }

    public static int print(String s) {
        System.out.println(s);
        return -1;

    }
    
}