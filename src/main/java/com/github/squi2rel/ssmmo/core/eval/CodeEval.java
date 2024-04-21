package com.github.squi2rel.ssmmo.core.eval;

import com.github.squi2rel.ssmmo.utils.FastStringWriter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.util.concurrent.*;

@Service
@SuppressWarnings("removal")
public class CodeEval {
    ExecutorService threadPool = Executors.newFixedThreadPool(10);

    @Async
    public Future<String> runCode(String s) {
        CodeContext c;
        try {
            c = HotCompiler.compile(s).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return runCode(c).exceptionally(CodeEval::format);
    }

    public CompletableFuture<String> runCode(CodeContext ctx) {
        return CompletableFuture.supplyAsync(() -> {
            if (ctx.errored) return ctx.errorString;
            if (!ctx.hasInstance()) {
                try {
                    ctx.newInstance();
                } catch (Exception e) {
                    return format(e);
                }
            }
            try {
                ctx.run();
            } catch (Exception e) {
                return format(e);
            }
            return "success";
        }, threadPool).orTimeout(200, TimeUnit.MILLISECONDS);
    }

    public static String format(Throwable t) {
        FastStringWriter o = new FastStringWriter();
        t.printStackTrace(new PrintWriter(o));
        return o.toString();
    }
}
