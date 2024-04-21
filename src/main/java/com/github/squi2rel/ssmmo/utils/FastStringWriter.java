package com.github.squi2rel.ssmmo.utils;

import org.jetbrains.annotations.NotNull;

import java.io.Writer;

public class FastStringWriter extends Writer {
    StringBuilder builder = new StringBuilder();

    @Override
    public void write(char @NotNull [] buf, int off, int len) {
        builder.append(buf, off, len);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

    public StringBuilder getBuilder() {
        return builder;
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
