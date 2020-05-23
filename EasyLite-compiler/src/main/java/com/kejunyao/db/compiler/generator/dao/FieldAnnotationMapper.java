package com.kejunyao.db.compiler.generator.dao;

import com.kejunyao.db.compiler.Utils;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import static com.kejunyao.db.compiler.generator.dao.SpecUtils.PARAM_NAME_ENTITY;
import static javax.lang.model.type.TypeKind.BOOLEAN;

/**
 * Entity属性注解Mapper
 *
 * @author kejunyao
 * @since 2019年12月10日
 */
public class FieldAnnotationMapper {

    /** Java Entity属性名称 */
    String filed;
    /** Java Entity属性类型 */
    TypeMirror filedType;
    /** 实体Entity属性field的若干修饰符 */
    Set<Modifier> modifiers;

    /**
     * 为Java类文件属性生成getter方法
     * 如：entity.getId()
     */
    String getter() {
        if (isPublic()) {
            return filed;
        }
        if (filedType.getKind() == BOOLEAN) {
            return getterForBoolean();
        }
        return String.format("%1$s.get%2$s()", PARAM_NAME_ENTITY, firstUpperCase());
    }

    /**
     * 生成使用时的Java setter方法
     */
    String setter(String param) {
        if (filedType.getKind() == BOOLEAN) {
            if (filed.length() >= 3) {
                if (filed.startsWith("is")) {
                    String thirdLetter = filed.substring(2, 3);
                    if (thirdLetter.equals(thirdLetter.toUpperCase())) {
                        return Utils.concat("set", filed.substring(2), '(', param, ')');
                    }
                }
            }
        }
        return Utils.concat("set", firstUpperCase(), '(', param, ')');
    }

    /**
     * 生成boolean类型属性的getter方法
     * 如：entity.isFave()
     */
    private String getterForBoolean() {
        if (filed.length() >= 3) {
            if (filed.startsWith("is")) {
                String thirdLetter = filed.substring(2, 3);
                if (thirdLetter.equals(thirdLetter.toUpperCase())) {
                    return String.format("%1$s.%2$s()", PARAM_NAME_ENTITY, filed);
                }
            }
        }
        return String.format("%1$s.is%2$s()", PARAM_NAME_ENTITY, firstUpperCase());
    }

    /**
     * 属性首字母大写，非首字母不变
     */
    private String firstUpperCase() {
        if (filed.length() > 1) {
            return String.format(SpecUtils.TWO_FORMAT_TAB, filed.substring(0, 1).toUpperCase(), filed.substring(1));
        }
        return filed.toUpperCase();
    }

    /**
     * 属性是否为public类型的
     */
    boolean isPublic() {
        return modifiers.contains(Modifier.PUBLIC);
    }

    /**
     * 属性是否为boolean类型的
     */
    boolean isBoolean() {
        return filedType.getKind() == BOOLEAN;
    }

    boolean isNumber() {
        switch (filedType.getKind()) {
            case BYTE:
            case FLOAT:
            case INT:
            case CHAR:
            case LONG:
            case SHORT:
            case DOUBLE:
                return true;
            default:
                return false;
        }
    }

    public String entityName() {
        String name = filedType.toString();
        int start = name.indexOf('<');
        int end = name.indexOf('>');
        if (start < 0 || end < 0) {
            return name;
        }
        return name.substring(start + 1, end);
    }
}
