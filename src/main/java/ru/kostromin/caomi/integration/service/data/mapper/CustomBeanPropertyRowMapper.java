package ru.kostromin.caomi.integration.service.data.mapper;

import java.lang.reflect.Field;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

/**
 * Кастомный BeanPropertyRowMapper для чтения имени колонки из @Column
 * (для DTO)
 */
@Slf4j
public class CustomBeanPropertyRowMapper<T> extends BeanPropertyRowMapper<T> {

  public CustomBeanPropertyRowMapper(final Class<T> mappedClass)
  {
    super(mappedClass);
  }

  @Override
  protected String underscoreName(String name) {
    final Column annotation;
    final String columnName;
    Field declaredField = null;

    try{
      declaredField = getMappedClass().getDeclaredField(name);
    } catch (NoSuchFieldException | SecurityException e){
      log.warn("Ups, field «{}» not found in «{}».", name, getMappedClass());
    }
    if (declaredField == null || (annotation = declaredField.getAnnotation(Column.class)) == null
        || StringUtils.isEmpty(columnName = annotation.value()))
    {
      return super.underscoreName(name);
    }

    return StringUtils.lowerCase(columnName);
  }
}
