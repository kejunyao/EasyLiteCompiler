package com.kejunyao.db.annotation;

/**
 * 数据库表字段类型枚举
 *
 * @author kejunyao
 * @since 2019年12月09日
 */
public enum ColumnType {
    TEXT("TEXT"),
    BOOLEAN("BOOLEAN"),
    REAL("REAL"),
    NVARCHAR("NVARCHAR"),
    FLOAT("FLOAT"),
    DOUBLE("DOUBLE"),
    INTEGER("INTEGER"),
    LONG("LONG");
    String value;
    ColumnType(String value) {
        this.value = value;
    }

    public final String value() {
        return this.value;
    }
}
