package com.kejunyao.db.generator.dao;

import com.squareup.javapoet.MethodSpec;
import com.kejunyao.db.Utils;
import com.kejunyao.db.annotation.Association;
import com.kejunyao.db.annotation.AssociationType;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * Association注解Mapper
 *
 * @author kejunyao
 * @since 2019年12月10日
 */
public class AssociationAnnotationMapper extends FieldAnnotationMapper {
    AssociationType type;
    String where;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("type=").append(type);
        sb.append(", where='").append(where).append('\'');
        sb.append(", filed='").append(filed).append('\'');
        sb.append(", filedType=").append(filedType);
        sb.append(", modifiers=").append(modifiers);
        sb.append('}');
        return sb.toString();
    }

    /**
     * 将{@link Element}解析为 {@link AssociationAnnotationMapper}
     * @param element {@link Element}
     * @return {@link AssociationAnnotationMapper}
     */
    static AssociationAnnotationMapper parseElement(Element element) {
        Utils.assertIllegalArgument(
                element.getModifiers().contains(Modifier.STATIC),
                Utils.concat("Can not support static modifier: ", element.getSimpleName().toString(), ", modifiers: ", element.getModifiers())
        );
        AssociationAnnotationMapper associationMapper = new AssociationAnnotationMapper();
        associationMapper.modifiers = element.getModifiers();
        Association association = element.getAnnotation(Association.class);
        associationMapper.filed = element.getSimpleName().toString();
        associationMapper.filedType = element.asType();
        associationMapper.type = association.type();
        final String where = association.where();
        associationMapper.parseWhere(where);
        return associationMapper;
    }

    boolean isToOne() {
        return AssociationType.TO_ONE.equals(type);
    }

    MethodSpec toEntity(EntityAnnotationMapper entityMapper) {
        return SpecUtils.toAssociationEntity(entityMapper, this);
    }

    private static final String REGEX = "\\#\\{(.*?)\\}";
    private final Set<String> filedSet = new LinkedHashSet<>();
    private void parseWhere(final String where) {
        filedSet.clear();
        if (Utils.isEmpty(where)) {
            this.where = null;
            return;
        }
        this.where = where;
        Matcher matcher = Pattern.compile(REGEX).matcher(where);
        while (matcher.find()) {
            String group = matcher.group();
            this.where = this.where.replace(group, "?");
            filedSet.add(group.substring(2, group.length() - 1));
        }
    }

    /**
     * 获取where条件数组字符串
     * @param entityMapper 关联属性所属Entity的Mapper
     * @return
     */
    String getWhereArgs(EntityAnnotationMapper entityMapper) {
        StringBuilder sb = new StringBuilder();
        sb.append("new String[] {\n");
        int index = 0;
        for (String filed : filedSet) {
            if (index > 0) {
                sb.append(", ");
            }
            ColumnAnnotationMapper fileMapper = entityMapper.findFiledMapper(filed);
            if (fileMapper.isBoolean()) {
                sb.append(fileMapper.getter()).append(" ? \"1\" : \"0\"");
            } else if (fileMapper.isNumber()) {
                sb.append("java.lang.String.valueOf(").append(fileMapper.getter()).append(')');
            } else {
                sb.append(fileMapper.getter());
            }
            index += 1;
        }
        sb.append("\n}");
        return sb.toString();
    }

}
