package com.kejunyao.db.compiler.generator.dao;

import com.google.auto.common.SuperficialValidation;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.kejunyao.db.compiler.Utils;
import com.kejunyao.db.annotation.Association;
import com.kejunyao.db.annotation.Column;
import com.kejunyao.db.annotation.Entity;
import com.kejunyao.db.compiler.generator.AbstractGenerator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import static com.kejunyao.db.compiler.generator.Constant.PACKAGE_DAO_CORE;
import static com.kejunyao.db.compiler.generator.Constant.PACKAGE_GENERATOR_CLASS;

/**
 * DAO实现类生成器
 *
 * @author kejunyao
 * @since 2019年12月06日
 */
public class DaoImplGenerator extends AbstractGenerator {

    private static final boolean DEBUG = false;
    /** easydao-core核心库中com.wandu.db.AbstractSQLiteDaoImpl类的类名称 */
    private static final String CLASS_NAME_ABSTRACT_SQLITE_DAO_IMPL = "AbstractSQLiteDaoImpl";
    /** easydao-core核心库中com.wandu.db.AbstractSQLiteDaoImpl类的类名称 */
    private static final String CLASS_NAME_ABSTRACT_PROVIDER_DAO_IMPL = "AbstractProviderDaoImpl";

    private final Map<String, EntityAnnotationMapper> entityMap = new LinkedHashMap<>();

    public DaoImplGenerator(ProcessingEnvironment env) {
        super(env);
    }

    public void generate(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        findAllEntity(env);
        findAllColumn(env);
        findAllAssociation(env);
        generateDaoImplClassFiles();
    }

    /**
     * 解析@Entity注解
     * @param env {@link RoundEnvironment}
     */
    private void findAllEntity(RoundEnvironment env) {
        for (Element element : env.getElementsAnnotatedWith(Entity.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            Element enclosingElement = element.getEnclosingElement();
            if (DEBUG) {
                printMessage("findAllEntity, " + element.asType().toString() + ", " + enclosingElement);
            }
            String key = element.asType().toString();
            EntityAnnotationMapper entityMapper = entityMap.get(key);
            if (entityMapper == null) {
                entityMapper = new EntityAnnotationMapper();
                entityMap.put(key, entityMapper);
            }
            entityMapper.set(element);
        }
    }

    /**
     * 解析@Column注解
     * @param env {@link RoundEnvironment}
     */
    private void findAllColumn(RoundEnvironment env) {
        for (Element element : env.getElementsAnnotatedWith(Column.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            Element enclosingElement = element.getEnclosingElement();
            if (DEBUG) {
                printMessage("findAllColumn, field: " + element.getSimpleName().toString() + ", asType: " + element.asType()
                        + ", asType().getKind(): " + element.asType().getKind()
                        +  ", enclosingElement.asType: " + enclosingElement.asType()
                        + ", enclosingElement.asType.getKind: " + enclosingElement.asType().getKind()
                );
            }
            EntityAnnotationMapper entityMapper = entityMap.get(enclosingElement.asType().toString());
            entityMapper.columnMappers.add(ColumnAnnotationMapper.parseElement(element));
        }
    }

    /**
     * 解析@Association注解
     * @param env {@link RoundEnvironment}
     */
    private void findAllAssociation(RoundEnvironment env) {
        for (Element element : env.getElementsAnnotatedWith(Association.class)) {
            if (!SuperficialValidation.validateElement(element)) continue;
            Element enclosingElement = element.getEnclosingElement();
            if (DEBUG) {
                printMessage("findAllAssociation, field: " + element.getSimpleName().toString() + ", asType: " + element.asType()
                        + ", asType().getKind(): " + element.asType().getKind()
                        + ", enclosingElement: " + enclosingElement
                );
            }
            EntityAnnotationMapper entityMapper = entityMap.get(enclosingElement.asType().toString());
            entityMapper.associationMappers.add(AssociationAnnotationMapper.parseElement(element));
        }
    }

    /**
     * 生成所有EntityDaoImpl.java文件
     */
    private void generateDaoImplClassFiles() {
        if (DEBUG) {
            printMessage("generateClassFile, entityMap.size(): " + entityMap.size());
        }
        for (String key : entityMap.keySet()) {
            generateDaoImplClassFile(entityMap.get(key));
        }
    }

    /**
     * 生成某个Entity对应的EntityDaoImpl.java文件
     * @param entityMapper {@link EntityAnnotationMapper}
     */
    private void generateDaoImplClassFile(EntityAnnotationMapper entityMapper) {
        ClassName abstractDaoImpl = ClassName.get(PACKAGE_DAO_CORE, daoImplSuperClass(entityMapper.isProviderMode()));
        TypeName type = TypeName.get(entityMapper.entityType);
        ParameterizedTypeName typeName = ParameterizedTypeName.get(abstractDaoImpl, type);
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(entityMapper.daoImplClassName())
                .addModifiers(Modifier.PUBLIC);
        typeBuilder.superclass(typeName);
        typeBuilder.addField(entityMapper.upperCaseTableName());
        typeBuilder.addFields(entityMapper.upperCaseColumnNames());
        typeBuilder.addField(entityMapper.upperCaseColumnArray());
        typeBuilder.addMethod(entityMapper.getTableName());
        typeBuilder.addMethod(entityMapper.getColumns());
        typeBuilder.addMethods(entityMapper.toEntity(entityMap));
        typeBuilder.addMethod(entityMapper.toContentValues());
        TypeSpec daoImpl = typeBuilder.build();
        Utils.writeJavaFile(environment.getFiler(), PACKAGE_GENERATOR_CLASS, daoImpl);
    }

    private String daoImplSuperClass(boolean isProvider) {
        return isProvider ? CLASS_NAME_ABSTRACT_PROVIDER_DAO_IMPL : CLASS_NAME_ABSTRACT_SQLITE_DAO_IMPL;
    }
}
