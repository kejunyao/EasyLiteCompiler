package com.kejunyao.db.generator;

import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * Java文件生成器协议
 *
 * @author kejunyao
 * @since 2019年12月09日
 */
public interface IGenerator {
    void generate(Set<? extends TypeElement> annotations, RoundEnvironment env);
}
