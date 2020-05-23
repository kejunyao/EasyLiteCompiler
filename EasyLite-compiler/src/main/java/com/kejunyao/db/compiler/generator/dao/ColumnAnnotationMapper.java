package com.kejunyao.db.compiler.generator.dao;

import com.kejunyao.db.compiler.CompilerUtils;
import com.kejunyao.db.annotation.Column;
import com.kejunyao.db.annotation.ColumnConstraint;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import static com.kejunyao.db.compiler.generator.dao.SpecUtils.PARAM_NAME_CURSOR;

/**
 * 数据库表Column对应的Mapper
 *
 * @author kejunyao
 * @since 2019年12月09日
 */
class ColumnAnnotationMapper extends FieldAnnotationMapper {
    /** 数据库表column名称 */
    String column;
    /** 数据库表column类型 */
    String columnType;
    /** 数据库表column大小 */
    int columnSize;
    /** 数据库表column约束 */
    ColumnConstraint constraint;
    /** 数据库表column默认值 */
    String defaultValue;

    /**
     * 将{@link Element}解析为 {@link ColumnAnnotationMapper}
     * @param element {@link Element}
     * @return {@link ColumnAnnotationMapper}
     */
    static ColumnAnnotationMapper parseElement(Element element) {
        CompilerUtils.assertIllegalArgument(
                element.getModifiers().contains(Modifier.STATIC),
                CompilerUtils.concat("Can not support static modifier: ", element.getSimpleName().toString(), ", modifiers: ", element.getModifiers())
        );
        ColumnAnnotationMapper columnMapper = new ColumnAnnotationMapper();
        columnMapper.modifiers = element.getModifiers();
        Column column = element.getAnnotation(Column.class);
        columnMapper.filed = element.getSimpleName().toString();
        columnMapper.filedType = element.asType();
        columnMapper.column = column.name();
        columnMapper.columnSize = column.size();
        columnMapper.columnType = column.type().value();
        columnMapper.defaultValue = column.defaultValue();
        columnMapper.constraint = column.constraint();
        return columnMapper;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append(", filed='").append(filed).append('\'');
        sb.append(", name='").append(column).append('\'');
        sb.append(", filedType=").append(filedType);
        sb.append(", columnType='").append(columnType).append('\'');
        sb.append(", columnSize=").append(columnSize);
        sb.append(", constraint='").append(constraint).append('\'');
        sb.append(", defaultValue='").append(defaultValue).append('\'');
        sb.append(", modifiers=").append(modifiers);
        sb.append('}');
        return sb.toString();
    }

    /**
     * 数据库表字段静态常量名称
     * 如：COLUMN_ID
     */
    String columnName() {
        return String.format("COLUMN_%s", column.toUpperCase());
    }

    /**
     * 数据库表字段下标静态常量名称
     * 如：INDEX_ID
     */
    String columnIndexName() {
        return String.format("INDEX_%s", column.toUpperCase());
    }

    /**
     * 生成数据库android.database.Cursor各个类型数据的get方法
     * 如：cursor.getLong(INDEX_ID)
     */
    String getCursorColumn() {
        return String.format("%1$s.%2$s", PARAM_NAME_CURSOR, getColumn(columnIndexName()));
    }

    /**
     * 生成数据库android.database.Cursor各个类型数据的get方法
     * 如：getLong(INDEX_ID)
     */
    private String getColumn(String columnIndexName) {
        String method;
        switch (filedType.getKind()) {
            case DECLARED:
                method = "getString";
                break;
            case LONG:
                method = "getLong";
                break;
            case BOOLEAN:
            case INT:
                method = "getInt";
                break;
            case SHORT:
            case BYTE:
                method = "getShort";
                break;
            case FLOAT:
                method = "getFloat";
                break;
            case DOUBLE:
                method = "getDouble";
                break;
            default:
                method = "get";
                break;
        }
        return String.format("%1$s(%2$s)", method, columnIndexName);
    }
}
