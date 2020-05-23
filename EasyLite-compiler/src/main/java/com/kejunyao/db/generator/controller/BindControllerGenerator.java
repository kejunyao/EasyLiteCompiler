package com.kejunyao.db.generator.controller;

import com.google.auto.common.SuperficialValidation;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.kejunyao.db.Utils;
import com.kejunyao.db.annotation.Entity;
import com.kejunyao.db.generator.AbstractGenerator;
import com.kejunyao.db.generator.dao.EntityAnnotationMapper;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import static com.kejunyao.db.generator.Constant.PACKAGE_ANDROID_CONTENT;
import static com.kejunyao.db.generator.Constant.PACKAGE_DAO_CORE;
import static com.kejunyao.db.generator.Constant.PACKAGE_GENERATOR_CLASS;

/**
 * 生成{@link Entity#bindDatabaseController()}对应的Controller
 *
 * @author kejunyao
 * @since 2019年12月09日
 */
public class BindControllerGenerator extends AbstractGenerator {
    /** easydao-core核心库中com.wandu.db.AbstractSQLiteDaoImpl类的类名称 */
    private static final String CLASS_NAME_DATABASE_CONTROLLER = "DatabaseController";
    /** android.database.sqlite.SQLiteOpenHelper包名 */
    private static final String PACKAGE_ANDROID_SQLITE = "android.database.sqlite";
    /** android.database.sqlite.SQLiteOpenHelper类名称 */
    private static final String CLASS_NAME_SQLITE_OPEN_HELPER = "SQLiteOpenHelper";
    /** android.database.sqlite.SQLiteOpenHelper类名称 */
    private static final String CLASS_NAME_CONTENT_RESOLVER = "ContentResolver";

    private Map<String, HashSet<EntityAnnotationMapper>> entityMap = new LinkedHashMap<>();

    public BindControllerGenerator(ProcessingEnvironment env) {
        super(env);
    }

    @Override
    public void generate(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        findBindController(env);
        generateDaoImplClassFiles();
    }

    private void findBindController(RoundEnvironment env) {
        for (Element element : env.getElementsAnnotatedWith(Entity.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            Entity entity = element.getAnnotation(Entity.class);
            String bindController = entity.bindDatabaseController();
            if (Utils.isEmpty(bindController)) {
                continue;
            }
            HashSet<EntityAnnotationMapper> entitySet = entityMap.get(bindController);
            if (entitySet == null) {
                entitySet = new HashSet<>();
                entityMap.put(bindController, entitySet);
            }
            entitySet.add(new EntityAnnotationMapper().set(element));
        }
    }

    private void generateDaoImplClassFiles() {
        for (String key : entityMap.keySet()) {
            generateDaoImplClassFile(key, entityMap.get(key));
        }
    }

    private void generateDaoImplClassFile(String bindController, HashSet<EntityAnnotationMapper> mapperSet) {
        ClassName databaseController = ClassName.get(PACKAGE_DAO_CORE, CLASS_NAME_DATABASE_CONTROLLER);
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(bindController)
                .addModifiers(Modifier.PUBLIC);
        typeBuilder.superclass(databaseController);
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ExecutorService.class, "executor")
                .addParameter(ClassName.get(PACKAGE_ANDROID_SQLITE, CLASS_NAME_SQLITE_OPEN_HELPER), "openHelper")
                .addParameter(ClassName.get(PACKAGE_ANDROID_CONTENT, CLASS_NAME_CONTENT_RESOLVER), "resolver")
                .addParameter(String.class, "providerAuthority");
        builder.addStatement("super(executor, openHelper, resolver, providerAuthority)");
        for (EntityAnnotationMapper entityMapper : mapperSet) {
            builder.addStatement("addDao(new $T())", ClassName.get(PACKAGE_GENERATOR_CLASS, entityMapper.daoImplClassName()));
        }
        typeBuilder.addMethod(builder.build());
        Utils.writeJavaFile(environment.getFiler(), PACKAGE_DAO_CORE, typeBuilder.build());
    }
}
