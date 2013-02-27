package com.cordys.jenkinsci.summareport.helpers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionHelper {
    public static <T> T invokeGetter(String name, Object onObject) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (name.startsWith("get")) {
            name = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        Method method = onObject.getClass().getMethod(name);

        return (T) method.invoke(onObject);
    }
}
