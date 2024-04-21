package com.github.squi2rel.ssmmo.core.eval;

import java.lang.reflect.Method;

public class CodeContext {
    public Class<?> clazz;
    public String errorString;
    public final boolean errored;
    private Object instance;
    private Method loop;

    public CodeContext(Class<?> clazz) {
        this.clazz = clazz;
        errored = false;
    }

    public CodeContext(String errorString) {
        this.errorString = errorString;
        this.errored = true;
    }

    public boolean hasInstance() {
        return instance != null;
    }

    public void newInstance() throws Exception {
        if (errored) throw new IllegalStateException();
        if (instance != null) return;
        try {
            loop = clazz.getDeclaredMethod("loop");
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            loop = null;
            throw e;
        }
    }

    public void run() throws Exception {
        if (loop == null) return;
        loop.invoke(instance);
    }
}
