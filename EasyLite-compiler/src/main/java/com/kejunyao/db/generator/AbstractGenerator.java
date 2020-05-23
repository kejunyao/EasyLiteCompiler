package com.kejunyao.db.generator;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

/**
 * Java文件生成器基类
 *
 * @author kejunyao
 * @since 2019年12月09日
 */
public abstract class AbstractGenerator implements IGenerator {

    protected final ProcessingEnvironment environment;
    protected final Messager messager;

    public AbstractGenerator(ProcessingEnvironment env) {
        this.environment = env;
        messager = env.getMessager();
    }

    protected final void printMessage(String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }
}
