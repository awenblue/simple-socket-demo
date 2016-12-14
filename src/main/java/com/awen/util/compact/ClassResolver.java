package com.awen.util.compact;

public abstract interface ClassResolver {
    public abstract Class<?> resolve(String paramString) throws ClassNotFoundException;
}