package org.neo4j.ogm.context;

import java.util.Collection;
import java.util.Map;

import org.neo4j.ogm.entity.io.EntityAccess;
import org.neo4j.ogm.entity.io.EntityAccessManager;
import org.neo4j.ogm.entity.io.PropertyReader;
import org.neo4j.ogm.entity.io.PropertyWriter;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.response.model.PropertyModel;
import org.neo4j.ogm.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared functionality for all Entity Mappers.
 *
 * @author Mark Angrish
 */
public class AbstractEntityMapper {

	private final Logger logger = LoggerFactory.getLogger(AbstractEntityMapper.class);

	private static Property<?, ?> convertToMapEntry(Map.Entry<?, ?> entry) {
		return new PropertyModel<>(entry.getKey(), entry.getValue());
	}

	protected void writeProperty(ClassInfo classInfo, Object instance, Map.Entry<?, ?> property) {
		writeProperty(classInfo, instance, convertToMapEntry(property));
	}

	protected void writeProperty(ClassInfo classInfo, Object instance, Property<?, ?> property) {

		PropertyWriter writer = EntityAccessManager.getPropertyWriter(classInfo, property.getKey().toString());

		if (writer == null) {
			logger.debug("Unable to find property: {} on class: {} for writing", property.getKey(), classInfo.name());
		} else {
			Object value = property.getValue();
			// merge iterable / arrays and co-erce to the correct attribute type
			if (writer.type().isArray() || Iterable.class.isAssignableFrom(writer.type())) {
				PropertyReader reader = EntityAccessManager.getPropertyReader(classInfo, property.getKey().toString());
				if (reader != null) {
					Object currentValue = reader.readProperty(instance);
					Class<?> paramType = writer.type();
					Class elementType = underlyingElementType(classInfo, property.getKey().toString());
					if (paramType.isArray()) {
						value = EntityAccess.merge(paramType, value, (Object[]) currentValue, elementType);
					} else {
						value = EntityAccess.merge(paramType, value, (Collection) currentValue, elementType);
					}
				}
			}
			writer.write(instance, value);
		}
	}

	private Class underlyingElementType(ClassInfo classInfo, String propertyName) {
		FieldInfo fieldInfo = fieldInfoForPropertyName(propertyName, classInfo);
		Class clazz = null;
		if (fieldInfo != null) {
			clazz = ClassUtils.getType(fieldInfo.getTypeDescriptor());
		}
		return clazz;
	}

	private FieldInfo fieldInfoForPropertyName(String propertyName, ClassInfo classInfo) {
		FieldInfo labelField = classInfo.labelFieldOrNull();
		if (labelField != null && labelField.getName().toLowerCase().equals(propertyName.toLowerCase())) {
			return labelField;
		}
		return classInfo.propertyField(propertyName);
	}
}
