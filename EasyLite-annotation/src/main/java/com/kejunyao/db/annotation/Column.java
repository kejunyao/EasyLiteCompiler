package com.kejunyao.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Entity属性annotation
 *
 * @author kejunyao
 * @since 2019年15月05日
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    /** 数据库字段名称 */
    String name() default "";
    /** 类型 */
    ColumnType type() default ColumnType.INTEGER;
    /** 大小 */
    int size() default 0;
    /** 约束 */
    ColumnConstraint constraint() default ColumnConstraint.DEFAULT_NULL;
    /** 默认值 */
    String defaultValue() default "";
}