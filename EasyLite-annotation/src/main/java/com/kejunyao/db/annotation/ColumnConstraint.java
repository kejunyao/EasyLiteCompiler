package com.kejunyao.db.annotation;

/**
 * 数据库表字段约束枚举
 *
 * @author kejunyao
 * @since 2019年12月09日
 */
public enum ColumnConstraint {
    NOT_NULL(" NOT NULL "),
    UNIQUE(" UNIQUE "),
    PRIMARY_KEY(" PRIMARY KEY "),
    PRIMARY_KEY_AUTO(" PRIMARY KEY AUTOINCREMENT "),
    FOREIGN_KEY(" FOREIGN KEY "),
    CHECK(" CHECK "),
    DEFAULT_NULL (" DEFAULT NULL ");

    String value;
    ColumnConstraint(String value) {
        this.value = value;
    }

    public final String value() {
        return this.value;
    }

    public boolean primaryKey() {
        return PRIMARY_KEY.value().equals(this.value);
    }

    public boolean primaryKeyAuto() {
        return PRIMARY_KEY_AUTO.value().equals(this.value);
    }
}
