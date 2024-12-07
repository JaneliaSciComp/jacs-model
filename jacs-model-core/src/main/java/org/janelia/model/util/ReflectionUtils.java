package org.janelia.model.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Helper methods for dealing with reflection.
 * 
 * @author <a href="mailto:rokickik@mail.nih.gov">Konrad Rokicki</a>
 */
public class ReflectionUtils {

    private static final int MAX_CACHE_SIZE = 1000;

    private static final Class[] EMPTY_ARGS_TYPES = {};
    private static final Object[] EMPTY_ARGS_VALUES = {};

    @SuppressWarnings("unchecked")
    private static final LoadingCache<Class<?>, Set<Field>> CLASS_FIELDS_CACHE = CacheBuilder.newBuilder()
            .maximumSize(MAX_CACHE_SIZE)
            .build(new CacheLoader<Class<?>, Set<Field>>() {
                @Override
                public Set<Field> load(Class<?> key) throws Exception {
                    return org.reflections.ReflectionUtils.getAllFields(key);
                }
            });

    @SuppressWarnings("unchecked")
    private static final LoadingCache<Class<?>, Set<Method>> CLASS_METHODS_CACHE = CacheBuilder.newBuilder()
            .maximumSize(MAX_CACHE_SIZE)
            .build(new CacheLoader<Class<?>, Set<Method>>() {
                @Override
                public Set<Method> load(Class<?> key) throws Exception {
                    return org.reflections.ReflectionUtils.getAllMethods(key);
                }
            });

    public static Set<Field> getAllClassFields(Class<?> clazz) {
        try {
            return CLASS_FIELDS_CACHE.get(clazz);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Set<Method> getAllClassMethods(Class<?> clazz) {
        try {
            return CLASS_METHODS_CACHE.get(clazz);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Get the given field value from the specified object. If the field is
     * private, this subverts the security manager to get the value anyway.
     */
    public static Object getFieldValue(Object obj, String fieldName) 
            throws NoSuchFieldException {
        try {
            return getField(obj, fieldName).get(obj);  
        }
        catch (IllegalAccessException e) {
            // shouldn't happen since getField sets the field accessible
            throw new RuntimeException("Field cannot be accessed",e);
        }
    }
    
    /**
     * Set the given field on the specified object. If the field is
     * private, this subverts the security manager to set the value anyway.
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) 
            throws NoSuchFieldException {
        try {
            getField(obj, fieldName).set(obj, value);    
        }
        catch (IllegalAccessException e) {
            // shouldn't happen since getField sets the field accessible
            throw new RuntimeException("Field cannot be accessed",e);
        }
    }

    /**
     * Set the given field on the specified object. If the field is
     * private, this subverts the security manager to set the value anyway.
     */
    public static Object getFieldValue(Object obj, Field field) 
            throws NoSuchFieldException {
        try {
            field.setAccessible(true);
            return field.get(obj); 
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Field cannot be accessed",e);
        }
    }
    
    /**
     * Set the given field on the specified object. If the field is
     * private, this subverts the security manager to set the value anyway.
     */
    public static void setFieldValue(Object obj, Field field, Object value) 
            throws NoSuchFieldException {
        try {
            field.setAccessible(true);
            field.set(obj, value);    
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Field cannot be accessed",e);
        }
    }
    
    /**
     * Get the given attribute from the specified object, 
     * using the public getter method.
     */
    public static Object get(Object obj, String attributeName) 
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String methodName = getAccessor("get", attributeName);
        return obj.getClass().getMethod(methodName, EMPTY_ARGS_TYPES).invoke(
            obj, EMPTY_ARGS_VALUES);
    }

    /**
     * Set the given attribute on the specified object, 
     * using the public setter method.
     */
    public static void set(Object obj, String attributeName, Object value, Class<?> attributeType)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class[] argTypes = {attributeType};
        Object[] argValues = {value};
        String methodName = getAccessor("set", attributeName);
        obj.getClass().getMethod(methodName, argTypes).invoke(obj, argValues);
    }

    private static Field getField(Object obj, String fieldName)
            throws NoSuchFieldException {

        final Field fields[] = obj.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (fieldName.equals(field.getName())) {
                field.setAccessible(true);
                return field;
            }
        }
        
        throw new NoSuchFieldException(fieldName);
    }
    
    private static String getAccessor(String prefix, String attributeName) {
        String firstChar = attributeName.substring(0, 1).toUpperCase();
        return prefix+firstChar+attributeName.substring(1);
    }
}
