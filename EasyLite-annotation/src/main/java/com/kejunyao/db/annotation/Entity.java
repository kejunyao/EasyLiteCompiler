package com.kejunyao.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Java实体类与数据库表对应的annotation
 *
 * @author kejunyao
 * @since 2019年15月05日
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Entity {
    /** daoMode,
     * 取值SQLITE时，EntityDaoImpl将继承AbstractSQLiteDaoImpl；
     * 取值PROVIDER时，EntityDaoImpl将继承AbstractProviderDaoImpl。
     *  */
    DaoMode daoMode();
    /** 实体绑定在哪一个com.wandu.db.DatabaseController上 */
    String bindDatabaseController() default "";
    /** 表名称 */
    String tableName() default "";
}