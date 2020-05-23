package com.kejunyao.db.compiler.generator;

/**
 * 公共常量
 *
 * @author kejunyao
 * @since 2019年12月10日
 */
public interface Constant {
    /** Android android.content包名 */
    String PACKAGE_ANDROID_CONTENT = "android.content";
    /** easydao-core核心库的主包名 */
    String PACKAGE_DAO_CORE = "com.kejunyao.db";
    /** 生成{$Entity}DaoImpl实现类的包名 */
    String PACKAGE_GENERATOR_CLASS = "com.kejunyao.db.impl";
}
