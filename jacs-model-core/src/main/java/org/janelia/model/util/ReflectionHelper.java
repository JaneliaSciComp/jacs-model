package org.janelia.model.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import org.reflections.ReflectionUtils;

/**
 * Utility methods to help with common reflection tasks.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ReflectionHelper {

    private static final Class[] EMPTY_ARGS_TYPES = {};
    private static final Object[] EMPTY_ARGS_VALUES = {};

    public static Boolean getMandatoryBooleanValue(Object obj, Class<? extends Annotation> annotationClass) {
    	return (Boolean)getMandatoryFieldValue(obj, annotationClass);
    }
    
    public static String getMandatoryStringValue(Object obj, Class<? extends Annotation> annotationClass) {
    	return (String)getMandatoryFieldValue(obj, annotationClass);
    }

    public static Integer getMandatoryIntegerValue(Object obj, Class<? extends Annotation> annotationClass) {
    	return (Integer)getMandatoryFieldValue(obj, annotationClass);
    }
    
    /**
     * Get the value of a field with the given annotation. Exactly one field with the given annotation must exist
     * on the object or an IllegalStateException will be thrown. Note that the field may still be null, it just needs
     * to exist.
     * @param obj
     * @param annotationClass
     * @return
     */
    public static Object getMandatoryFieldValue(Object obj, Class<? extends Annotation> annotationClass) {
        try {
            Field idField = ReflectionHelper.getField(obj, annotationClass);
            return ReflectionHelper.getFieldValue(obj, idField);
        }
        catch (NoSuchFieldException e) {
            throw new IllegalStateException(obj.getClass().getName()+" has no field with @"+
                    annotationClass.getSimpleName()+" annotation", e);
        }
    }

    /**
     * Set the value of a field with the given annotation. Exactly one field with the given annotation must exist
     * on the object or an IllegalStateException will be thrown.
     * @param obj
     * @param annotationClass
     * @return
     */
    public static void setMandatoryFieldValue(Object obj, Class<? extends Annotation> annotationClass, Object value) {
        try {
            Field field = ReflectionHelper.getField(obj, annotationClass);
            ReflectionHelper.setFieldValue(obj, field, value);
        }
        catch (NoSuchFieldException e) {
            throw new IllegalStateException(obj.getClass().getName()+" has no field with @"+
                    annotationClass.getSimpleName()+" annotation", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Method getMethod(Object obj, String methodName) {
        Method matchedMethod = null;
        for(Method method : ReflectionUtils.getAllMethods(obj.getClass(), ReflectionUtils.withName(methodName))) {
            if (matchedMethod!=null) {
                throw new IllegalStateException("Found multiple methods with name "+methodName+" on "+obj.getClass().getName());
            }
            matchedMethod = method;
        }
        return matchedMethod;
    }

    @SuppressWarnings("unchecked")
    public static Collection<Method> getMethods(Object obj, String methodName) {
        return ReflectionUtils.getAllMethods(obj.getClass(), ReflectionUtils.withName(methodName));
    }

    @SuppressWarnings("unchecked")
    public static List<Field> getFields(Object obj) {
        return ImmutableList.copyOf(ReflectionUtils.getAllFields(obj.getClass()));
    }

    @SuppressWarnings("unchecked")
    public static Field getField(Object obj, Class<? extends Annotation> annotationClass) throws NoSuchFieldException {
        Field matchedField = null;
        for(Field field : ReflectionUtils.getAllFields(obj.getClass(), ReflectionUtils.withAnnotation(annotationClass))) {
            if (matchedField!=null) {
                throw new IllegalStateException("Found multiple fields with annotation "+annotationClass.getName()+" on "+obj.getClass().getName());
            }
            matchedField = field;
        }
        if (matchedField==null) {
            throw new NoSuchFieldException("Field with annotation "+annotationClass.getName()+" does not exist on "+obj.getClass().getName());
        }
        return matchedField;
    }

    @SuppressWarnings("unchecked")
    public static Collection<Field> getFields(Object obj, Class<? extends Annotation> annotationClass) {
        List<Field> matchedFields = new ArrayList<Field>();
        for(Field field : ReflectionUtils.getAllFields(obj.getClass(), ReflectionUtils.withAnnotation(annotationClass))) {
            matchedFields.add(field);
        }
        return matchedFields;
    }
    
    public static Object getFieldValue(Object obj, String fieldName) throws NoSuchFieldException {
        return getFieldValue(obj, getField(obj, fieldName)); 
    }
    
    public static void setFieldValue(Object obj, String fieldName, Object value) throws NoSuchFieldException {
        setFieldValue(obj, getField(obj, fieldName), value); 
    }

    @SuppressWarnings("unchecked")
    public static Field getField(Object obj, String fieldName) throws NoSuchFieldException {
        Field matchedField = null;
        final Set<Field> fields = ReflectionUtils.getAllFields(obj.getClass(), ReflectionUtils.withName(fieldName));
        for (Field field : fields) {
            if (matchedField!=null) {
                throw new IllegalStateException("Found multiple fields with name "+fieldName+" on "+obj.getClass().getName());
            }
            matchedField = field;
        }
        if (matchedField==null) {
            throw new NoSuchFieldException("Field with name '"+fieldName+"' does not exist on "+obj.getClass().getName());
        }
        return matchedField;
    }

    public static void setFieldValue(Object obj, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
        }
        catch (IllegalAccessException e) {
            // shouldn't happen since getField sets the field accessible
            throw new RuntimeException("Field cannot be accessed",e);
        }
    }

    public static Object getFieldValue(Object obj, Field field) {
        try {
            field.setAccessible(true);
            return field.get(obj);
        }
        catch (IllegalAccessException e) {
            // shouldn't happen since getField sets the field accessible
            throw new RuntimeException("Field cannot be accessed",e);
        }
    }
    
    /**
     * Get the given attribute from the specified object, using the public getter method.
     */
    public static Object getUsingGetter(Object obj, String fieldName) throws Exception {
        return getGetter(obj.getClass(), fieldName).invoke(obj, EMPTY_ARGS_VALUES);
    }

    /**
     * Get the public getter method for the given field.
     */
    public static Method getGetter(Class<?> clazz, String fieldName) throws Exception {
        String methodName = getAccessorName("get", fieldName);
        return clazz.getMethod(methodName, EMPTY_ARGS_TYPES);
    }

    /**
     * Set the given attribute on the specified object, using the public setter method.
     */
    public static void setUsingSetter(Object obj, String fieldName, Object value) throws Exception {
        Object[] argValues = {value};
        getSetter(obj.getClass(), fieldName, value.getClass()).invoke(obj, argValues);
    }

    /**
     * Get the public setter method for the given field.
     */
    public static Method getSetter(Class<?> clazz, String fieldName, Class<?> valueClass) throws Exception {
        Class[] argTypes = {valueClass};
        String methodName = getAccessorName("set", fieldName);
        return clazz.getMethod(methodName, argTypes);
    }
    
    private static String getAccessorName(String prefix, String fieldName) throws NoSuchMethodException {
        String firstChar = fieldName.substring(0, 1).toUpperCase();
        return prefix+firstChar+fieldName.substring(1);
    }
}
