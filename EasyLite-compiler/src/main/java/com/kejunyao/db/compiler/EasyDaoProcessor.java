package com.kejunyao.db.compiler;

import com.google.auto.service.AutoService;
import com.kejunyao.db.annotation.Association;
import com.kejunyao.db.annotation.Column;
import com.kejunyao.db.annotation.Entity;
import com.kejunyao.db.compiler.generator.IGenerator;
import com.kejunyao.db.compiler.generator.controller.BindControllerGenerator;
import com.kejunyao.db.compiler.generator.dao.DaoImplGenerator;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class EasyDaoProcessor extends AbstractProcessor {

    private static final boolean DEBUG = true;

    private Messager messager;

    private final Set<IGenerator> generators = new HashSet<>();

    private boolean processed = false;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        messager = env.getMessager();
        initGenerator(env);
    }

    private void initGenerator(ProcessingEnvironment env) {
        generators.add(new DaoImplGenerator(env));
        generators.add(new BindControllerGenerator(env));
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(Column.class);
        annotations.add(Entity.class);
        annotations.add(Association.class);
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    private void printMessage(String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (processed) {
            return false;
        }
        processed = true;
        if (DEBUG) {
            printMessage("EasyDaoProcessor#process");
        }
        for (IGenerator generator : generators) {
            generator.generate(annotations, env);
        }
        return false;
    }
}
