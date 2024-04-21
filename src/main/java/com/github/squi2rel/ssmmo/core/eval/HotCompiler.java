package com.github.squi2rel.ssmmo.core.eval;

import com.github.squi2rel.ssmmo.utils.FastStringWriter;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class HotCompiler {
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    public static Future<CodeContext> compile(String sourceCode) {
        return threadPool.submit(() -> {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            JavaFileObject sourceObject = new JavaSourceFromString("Loop", sourceCode);
            Iterable<String> options = List.of("-d", "memory");
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            FastStringWriter writer = new FastStringWriter();
            JavaCompiler.CompilationTask task = compiler.getTask(writer, null, diagnostics, options, null, List.of(sourceObject));
            boolean success = task.call();
            if (success) {
                try {
                    ByteArrayClassLoader classLoader = new ByteArrayClassLoader();
                    Class<?> clazz = classLoader.loadClass("Loop");
                    return new CodeContext(clazz);
                } catch (Exception e) {
                    return new CodeContext(e.getMessage());
                }
            } else {
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    writer.getBuilder().append("Error on line ").append(diagnostic.getLineNumber()).append(" in ").append(String.valueOf(diagnostic.getSource().toUri())).append(" :\n").append(diagnostic.getMessage(null));
                }
                return new CodeContext(writer.toString());
            }
        });
    }

    private static class JavaSourceFromString extends SimpleJavaFileObject {
        final String code;

        JavaSourceFromString(String name, String code) {
            super(java.net.URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    private static class ByteArrayClassLoader extends ClassLoader {

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                String fileName = "memory" + File.separator + name.replaceAll("\\.", File.separator) + ".class";
                byte[] bytes = Files.readAllBytes(Paths.get(fileName));
                return defineClass(null, bytes, 0, bytes.length);
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }
    }

}
