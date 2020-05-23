package com.kejunyao.db.generator.dao;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.kejunyao.db.Utils;
import com.kejunyao.db.generator.Constant;
import java.util.Map;
import javax.lang.model.element.Modifier;

/**
 * $类描述$
 *
 * @author kejunyao
 * @since 2019年12月10日
 */
final class SpecUtils {

    static final String TWO_FORMAT_TAB = "%1$s%2$s";
    static final String THREE_FORMAT_TAB = "%1$s%2$s%3$s";
    /** Android数据库包名 */
    static final String PACKAGE_ANDROID_DATABASE = "android.database";
    /** easydao-core核心库中com.wandu.db.Column类的类名称 */
    static final String CLASS_NAME_DATABASE_CURSOR = "Cursor";
    static final String PARAM_NAME_CURSOR = "cursor";
    /** 为{$Entity}DaoImpl类生成toEntity方法的方法名称 */
    static final String METHOD_TO_ENTITY = "toEntity";
    static final String METHOD_TO_ONE = "toOne";
    static final String METHOD_TO_MANY = "toMany";
    /** {$Entity}DaoImpl中{@link EntityAnnotationMapper#METHOD_TO_CONTENT_VALUES}方法的参数名称 */
    static final String PARAM_NAME_ENTITY = "entity";
    static final String VARIABLE_CURSOR_NAME = "c";
    static final String RESULT = "result";

    static final char OBJECT_SEPARATOR = '.';

    private SpecUtils() {
    }

    private static String closeSafely(String cursor) {
        return String.format("%1$s.Utils.closeSafely(%2$s);\n", Constant.PACKAGE_DAO_CORE, cursor);
    }

    static MethodSpec toOne(EntityAnnotationMapper entityMapper) {
        StringBuilder builder = new StringBuilder();
        builder.append(entityMapper.entityName).append(' ').append(PARAM_NAME_ENTITY).append(" = new ").append(entityMapper.entityName).append("();\n");
        for (ColumnAnnotationMapper columnMapper : entityMapper.columnMappers) {
            builder.append(PARAM_NAME_ENTITY).append(OBJECT_SEPARATOR);
            String booleanString = columnMapper.isBoolean() ? " == 1 ? true : false" : "";
            if (columnMapper.isPublic()) {
                builder.append(columnMapper.filed).append(" = ").append(columnMapper.getCursorColumn())
                        .append(booleanString);
            } else {
                builder.append(columnMapper.setter(Utils.concat(columnMapper.getCursorColumn(), booleanString)));
            }
            builder.append(";\n");
        }
        builder.append("return ").append(PARAM_NAME_ENTITY);
        return MethodSpec.methodBuilder(METHOD_TO_ONE)
                .addModifiers(Modifier.PROTECTED, Modifier.STATIC, Modifier.FINAL)
                .addParameter(ClassName.get(PACKAGE_ANDROID_DATABASE, CLASS_NAME_DATABASE_CURSOR), PARAM_NAME_CURSOR)
                .returns(TypeName.get(entityMapper.entityType))
                .addStatement(builder.toString())
                .build();
    }

    static MethodSpec toMany(EntityAnnotationMapper entityMapper) {
        StringBuilder builder = new StringBuilder();
        ClassName list = ClassName.get("java.util", "List");
        TypeName resultTypeName = ParameterizedTypeName.get(list, TypeName.get(entityMapper.entityType));
        builder.append("List<").append(entityMapper.entityName).append("> result = ").append("new java.util.ArrayList<>();\n");
        builder.append("if (").append(PARAM_NAME_CURSOR).append(" != null && !").append(PARAM_NAME_CURSOR).append(".isClosed() && ").append(PARAM_NAME_CURSOR).append(".getCount() > 0) {\n");
        builder.append("while (").append(PARAM_NAME_CURSOR).append(".moveToNext()) {\n");
        builder.append(entityMapper.entityName).append(' ').append(PARAM_NAME_ENTITY).append(" = ").append(entityMapper.toOne()).append(";\n");
        builder.append(RESULT).append(".add(").append(PARAM_NAME_ENTITY).append(");\n");
        builder.append("}\n");
        builder.append(closeSafely(PARAM_NAME_CURSOR));
        builder.append("}\n");
        builder.append("return ").append(RESULT);
        return MethodSpec.methodBuilder(METHOD_TO_MANY)
                .addModifiers(Modifier.PROTECTED, Modifier.STATIC, Modifier.FINAL)
                .addParameter(ClassName.get(PACKAGE_ANDROID_DATABASE, CLASS_NAME_DATABASE_CURSOR), PARAM_NAME_CURSOR)
                .returns(resultTypeName)
                .addStatement(builder.toString())
                .build();
    }

    static MethodSpec toEntity(EntityAnnotationMapper entityMapper, Map<String, EntityAnnotationMapper> allEntityMapper) {
        StringBuilder builder = new StringBuilder();
        if (entityMapper.associationMappers.size() > 0) {
            builder.append(entityMapper.entityName).append(' ').append(PARAM_NAME_ENTITY).append(" = ").append(METHOD_TO_ONE).append('(').append(PARAM_NAME_CURSOR).append(");\n");
            builder.append(CLASS_NAME_DATABASE_CURSOR).append(' ').append(VARIABLE_CURSOR_NAME).append(" = null;\n");
            for (AssociationAnnotationMapper mapper : entityMapper.associationMappers) {
                setAssociationFieldValue(builder, entityMapper, allEntityMapper.get(mapper.entityName()), mapper);
            }
            builder.append("return ").append(PARAM_NAME_ENTITY);
        } else {
            builder.append("return ").append(METHOD_TO_ONE).append('(').append(PARAM_NAME_CURSOR).append(")");
        }
        return MethodSpec.methodBuilder(METHOD_TO_ENTITY)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(PACKAGE_ANDROID_DATABASE, CLASS_NAME_DATABASE_CURSOR), PARAM_NAME_CURSOR)
                .returns(TypeName.get(entityMapper.entityType))
                .addStatement(builder.toString())
                .build();
    }

    /**
     * 设置关联查询类型属性值
     * @param builder {@link StringBuilder}
     * @param entityMapper 关联属性所在的EntityMapper
     * @param filedEntityMapper 关联属性自身的EntityMapper
     * @param am 关联属性Mapper
     */
    private static void setAssociationFieldValue(StringBuilder builder, EntityAnnotationMapper entityMapper, EntityAnnotationMapper filedEntityMapper, AssociationAnnotationMapper am) {
        builder.append(VARIABLE_CURSOR_NAME).append(' ').append("= query(")
                .append(filedEntityMapper.associationTableName()).append(", ")
                .append(filedEntityMapper.associationTableColumns()).append(", ")
                .append("\"").append(am.where).append("\", ")
                .append(am.getWhereArgs(entityMapper)).append("\n);\n");
        if (am.isToOne()) {
            builder.append("if (").append(VARIABLE_CURSOR_NAME).append(" == null || ")
                    .append(VARIABLE_CURSOR_NAME).append(OBJECT_SEPARATOR).append("isClosed() || !")
                    .append(VARIABLE_CURSOR_NAME).append(OBJECT_SEPARATOR).append("moveToFirst()) {\n");
            builder.append("return null;\n");
            builder.append("}\n");
            builder.append(filedEntityMapper.entityName).append(' ').append(filedEntityMapper.entityName.toLowerCase()).append(" = ").append(filedEntityMapper.associationToEntityMethodName()).append('(').append(VARIABLE_CURSOR_NAME).append(");\n");
            builder.append(closeSafely(VARIABLE_CURSOR_NAME));
            builder.append(PARAM_NAME_ENTITY).append(OBJECT_SEPARATOR).append(am.setter(filedEntityMapper.entityName.toLowerCase())).append(";\n");
        } else {
            builder.append("List<").append(filedEntityMapper.entityName).append("> ").append(RESULT).append(" = ").append(filedEntityMapper.toMany()).append(";\n");
            builder.append(closeSafely(VARIABLE_CURSOR_NAME));
            builder.append(PARAM_NAME_ENTITY).append(OBJECT_SEPARATOR).append(am.setter(RESULT)).append(";\n");
        }
    }

    static MethodSpec toAssociationEntity(EntityAnnotationMapper entityMapper, AssociationAnnotationMapper associationMapper) {
        StringBuilder builder = new StringBuilder();
        builder.append(" return ").append(entityMapper.toOne());
        return MethodSpec.methodBuilder(entityMapper.associationToEntityMethodName())
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ClassName.get(PACKAGE_ANDROID_DATABASE, CLASS_NAME_DATABASE_CURSOR), PARAM_NAME_CURSOR)
                .returns(TypeName.get(entityMapper.entityType))
                .addStatement(builder.toString())
                .build();
    }
}
