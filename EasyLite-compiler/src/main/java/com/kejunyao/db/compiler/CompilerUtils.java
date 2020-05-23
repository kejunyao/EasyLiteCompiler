package com.kejunyao.db.compiler;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import javax.annotation.processing.Filer;

/**
 * easy-dao通用工具类
 *
 * @author kejunyao
 * @since 2019年12月06日
 */
public final class Utils {

    private Utils() {
    }

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 参数非法断言
     * @param isTrue 抛出异常的条件
     * @param msg 提示信息
     */
    public static void assertIllegalArgument(boolean isTrue, String msg) {
        if (isTrue) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * 拼接成字符串
     */
    public static String concat(Object... objects) {
        if (objects == null || objects.length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object obj : objects) {
            sb.append(obj);
        }
        return sb.toString();
    }

    /**
     * 生成.java文件
     * @param filer {@link Filer}
     * @param packageName .java所在包
     * @param typeSpec {@li TypeSpec}
     */
    public static void writeJavaFile(Filer filer, String packageName, TypeSpec typeSpec) {
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
