package com.kejunyao.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 关联查询annotation
 *
 * @author kejunyao
 * @since 2019年12月10日
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Association {
    /**
     * 关联类型
     */
    AssociationType type();
    /**
     * 查询语句
     */
    String where();
}
