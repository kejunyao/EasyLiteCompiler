package com.kejunyao.db.compiler.generator.dao;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.kejunyao.db.compiler.CompilerUtils;
import com.kejunyao.db.annotation.ColumnConstraint;
import com.kejunyao.db.annotation.DaoMode;
import com.kejunyao.db.annotation.Entity;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import static com.kejunyao.db.compiler.generator.Constant.PACKAGE_ANDROID_CONTENT;
import static com.kejunyao.db.compiler.generator.Constant.PACKAGE_DAO_CORE;
import static com.kejunyao.db.compiler.generator.dao.SpecUtils.METHOD_TO_MANY;
import static com.kejunyao.db.compiler.generator.dao.SpecUtils.METHOD_TO_ONE;
import static com.kejunyao.db.compiler.generator.dao.SpecUtils.OBJECT_SEPARATOR;
import static com.kejunyao.db.compiler.generator.dao.SpecUtils.PARAM_NAME_CURSOR;
import static com.kejunyao.db.compiler.generator.dao.SpecUtils.PARAM_NAME_ENTITY;
import static com.kejunyao.db.compiler.generator.dao.SpecUtils.THREE_FORMAT_TAB;
import static com.kejunyao.db.compiler.generator.dao.SpecUtils.VARIABLE_CURSOR_NAME;

/**
 * 实体注解Mapper
 *
 * @author kejunyao
 * @since 2019年12月09日
 */
public class EntityAnnotationMapper {

    /** easydao-core核心库中com.wandu.db.Column类名称 */
    private static final String CLASS_NAME_COLUMN = "Column";
    /** Android android.content.ContentValues类的类名称 */
    private static final String CLASS_NAME_CONTENT_VALUES = "ContentValues";
    /** 实体Entity对应数据库表名称 */
    private static final String TABLE_NAME = "TABLE_NAME";
    /** 实体Entity对应数据库表的所有字段 */
    private static final String TABLE_COLUMNS = "TABLE_COLUMNS";
    /** 为{$Entity}DaoImpl类生成getTableName方法的方法名称 */
    private static final String METHOD_GET_TABLE_NAME = "getTableName";
    /** 为{$Entity}DaoImpl类生成getColumns方法的方法名称 */
    private static final String METHOD_GET_COLUMNS = "getColumns";
    /** 为{$Entity}DaoImpl类生成toContentValues方法的方法名称 */
    private static final String METHOD_TO_CONTENT_VALUES = "toContentValues";

    /** daoMode,
     * 取值SQLITE时，EntityDaoImpl将继承AbstractSQLiteDaoImpl；
     * 取值PROVIDER时，EntityDaoImpl将继承AbstractProviderDaoImpl。
     *  */
    DaoMode daoMode;
    /** 实体绑定的com.wandu.db.DatabaseController */
    String bindController;
    /** 实体名称 */
    String entityName;
    /**  */
    TypeMirror entityType;
    /** 实体对应的表名称 */
    String tableName;
    /** 实体所有属性对应的Mapper */
    final Set<ColumnAnnotationMapper> columnMappers = new LinkedHashSet<>();
    /** 实体所有关联查询属性对应的Mapper */
    final Set<AssociationAnnotationMapper> associationMappers = new LinkedHashSet<>();


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("daoMode=").append(daoMode);
        sb.append(", bindController='").append(bindController).append('\'');
        sb.append(", entityName='").append(entityName).append('\'');
        sb.append(", entityType=").append(entityType);
        sb.append(", tableName='").append(tableName).append('\'');
        sb.append(", columnMappers=").append(columnMappers);
        sb.append(", associationMappers=").append(associationMappers);
        sb.append(", columnsFormat=").append(columnsFormat);
        sb.append(", columnArray=").append(Arrays.toString(columnArray));
        sb.append('}');
        return sb.toString();
    }

    /**
     * 设置{@link EntityAnnotationMapper}各属性值
     */
    public EntityAnnotationMapper set(Element element) {
        Entity entity = element.getAnnotation(Entity.class);
        daoMode = entity.daoMode();
        entityType = element.asType();
        entityName = element.getSimpleName().toString();
        bindController = entity.bindDatabaseController();
        String tableName = entity.tableName();
        if (CompilerUtils.isEmpty(tableName)) {
            tableName = element.getSimpleName().toString();
        }
        this.tableName = tableName;
        return this;
    }

    /**
     * 生成EntityDaoImpl的名称
     * 如：TopicDaoImpl
     */
    public String daoImplClassName() {
        return String.format("%sDaoImpl", entityName);
    }

    /**
     * 生成访问关联Entity对应数据库表外部访问名称
     * 如：TopicDaoImpl.TABLE_NAME
     */
    String associationTableName() {
        return String.format(THREE_FORMAT_TAB, daoImplClassName(), OBJECT_SEPARATOR, TABLE_NAME);
    }

    /**
     * 生成访问关联Entity对应数据库表所有字段的外部访问名称
     * 如：TopicDaoImpl.TABLE_COLUMNS
     */
    String associationTableColumns() {
        return String.format(THREE_FORMAT_TAB, daoImplClassName(), OBJECT_SEPARATOR, TABLE_COLUMNS);
    }

    /**
     * 生成静态toOne方法名称
     * 如：TopicDaoImpl.toOne(cursor)
     */
    String toOne() {
        return String.format("%1$s.%2$s(%3$s)", daoImplClassName(), METHOD_TO_ONE, PARAM_NAME_CURSOR);
    }

    /**
     * 生成静态toMany方法名称
     * 如：TopicDaoImpl.toMany(cursor)
     */
    String toMany() {
        return String.format("%1$s.%2$s(%3$s)", daoImplClassName(), METHOD_TO_MANY, VARIABLE_CURSOR_NAME);
    }

    /**
     * 生成关联toEntity方法名称
     * 如：TopicDaoImpl.toComicEntity
     */
    String associationToEntityMethodName() {
        return String.format("to%sEntity", entityName);
    }

    /**
     * 生成的EntityDaoImpl是否继承
     * @return
     */
    boolean isProviderMode() {
        return daoMode == DaoMode.PROVIDER;
    }

    /**
     * TABLE_NAME
     */
    FieldSpec upperCaseTableName() {
        return FieldSpec.builder(String.class, TABLE_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", tableName)
                .build();
    }

    /**
     * COLUMN_NAME和COLUMN_INDEX
     */
    Set<FieldSpec> upperCaseColumnNames() {
        Set<FieldSpec> set = new LinkedHashSet<>();
        columnsFormat = new StringBuilder();
        columnsFormat.append('{');
        columnArray = new String[columnMappers.size()];
        int index = 0;
        for (ColumnAnnotationMapper column : columnMappers) {
            final String columnName = column.columnName();
            set.add(FieldSpec.builder(int.class, column.columnIndexName())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$L", index)
                    .build());
            set.add(FieldSpec.builder(String.class, columnName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", column.column)
                    .build());
            if (index > 0) {
                columnsFormat.append(", ");
            }
            columnsFormat.append("$L");
            columnArray[index] = columnName;
            index += 1;
        }
        columnsFormat.append("}");
        return set;
    }

    StringBuilder columnsFormat;
    Object[] columnArray;
    /**
     * TABLE_COLUMNS
     */
    FieldSpec upperCaseColumnArray() {
        return FieldSpec.builder(String[].class, TABLE_COLUMNS)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(columnsFormat.toString(), columnArray)
                .build();
    }

    /**
     * getTableName方法
     */
    MethodSpec getTableName() {
        return MethodSpec.methodBuilder(METHOD_GET_TABLE_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return $L", TABLE_NAME)
                .build();
    }

    /**
     * getColumns方法
     */
    MethodSpec getColumns() {
        StringBuilder builder = new StringBuilder();
        builder.append("return new ").append(CLASS_NAME_COLUMN).append("[] {\n");
        final int maxIndex = columnMappers.size() - 1;
        int index = 0;
        for (ColumnAnnotationMapper columnMapper : columnMappers) {
            setOneColumn(builder, columnMapper);
            if (index < maxIndex) {
                builder.append(", ");
            }
            builder.append('\n');
            index += 1;
        }
        builder.append('}');
        return MethodSpec.methodBuilder(METHOD_GET_COLUMNS)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ArrayTypeName.of(ClassName.get(PACKAGE_DAO_CORE, CLASS_NAME_COLUMN)))
                .addStatement(builder.toString())
                .build();
    }

    void setOneColumn(StringBuilder builder, ColumnAnnotationMapper columnMapper) {
        builder.append("new ").append(CLASS_NAME_COLUMN).append('(').append(columnMapper.columnName()).append(')');
        builder.append(OBJECT_SEPARATOR).append("type(\"").append(columnMapper.columnType).append("\")");
        if (columnMapper.columnSize > 0) {
            if (!columnMapper.constraint.primaryKey()
                    && !columnMapper.constraint.primaryKeyAuto()) {
                builder.append(OBJECT_SEPARATOR).append("size(").append(columnMapper.columnSize).append(')');
            }
        }
        builder.append(OBJECT_SEPARATOR).append("constraint(\"").append(columnMapper.constraint.value()).append("\")");
        if (!CompilerUtils.isEmpty(columnMapper.defaultValue)) {
            builder.append(OBJECT_SEPARATOR).append("defaultValue(\"").append(columnMapper.defaultValue).append("\")");
        }
    }

    /**
     * toEntity方法
     */
    Set<MethodSpec> toEntity(Map<String, EntityAnnotationMapper> entityMappers) {
        Set<MethodSpec> set = new LinkedHashSet<>();
        set.add(SpecUtils.toOne(this));
        set.add(SpecUtils.toMany(this));
        set.add(SpecUtils.toEntity(this, entityMappers));
        for (AssociationAnnotationMapper associationMapper : associationMappers) {
            String key = associationMapper.entityName();
            System.out.println(key);
            MethodSpec methodSpec = associationMapper.toEntity(entityMappers.get(key));
            if (methodSpec != null) {
                set.add(methodSpec);
            }
        }
        return set;
    }

    /**
     * toContentValues方法
     */
    MethodSpec toContentValues() {
        StringBuilder builder = new StringBuilder();
        builder.append("ContentValues values = new ContentValues();\n");
        for (ColumnAnnotationMapper columnMapper : columnMappers) {
            if (ColumnConstraint.PRIMARY_KEY_AUTO.equals(columnMapper.constraint)) {
                builder.append("if (").append(columnMapper.getter()).append(" > 0) {\n");
                putValue(builder, columnMapper);
                builder.append("}\n");
            } else {
                putValue(builder, columnMapper);
            }
        }
        builder.append("return values");
        return MethodSpec.methodBuilder(METHOD_TO_CONTENT_VALUES)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(entityType), PARAM_NAME_ENTITY)
                .returns(ClassName.get(PACKAGE_ANDROID_CONTENT, CLASS_NAME_CONTENT_VALUES))
                .addStatement(builder.toString())
                .build();
    }

    private void putValue(StringBuilder builder, ColumnAnnotationMapper columnMapper) {
        builder.append("values.put(")
                .append(columnMapper.columnName()).append(", ")
                .append(columnMapper.getter());
        if (columnMapper.isBoolean()) {
            builder.append(" ? 1 : 0");
        }
        builder.append(");\n");
    }

    ColumnAnnotationMapper findFiledMapper(String filed) {
        for (ColumnAnnotationMapper mapper : columnMappers) {
            if (mapper.filed.equals(filed)) {
                return mapper;
            }
        }
        return null;
    }
}