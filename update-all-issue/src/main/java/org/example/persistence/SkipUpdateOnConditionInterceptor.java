package org.example.persistence;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.stream.IntStream;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.EmptyInterceptor;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.type.Type;


public class SkipUpdateOnConditionInterceptor extends EmptyInterceptor {

    private static final String MODIFICATION_SKIPPED = "modificationSkipped";

    @Override
    public int[] findDirty(
            Object entity,
            Serializable id,
            Object[] currentState,
            Object[] previousState,
            String[] propertyNames,
            Type[] types) {
        System.out.println("findDirty is called");
        if (!isModified(entity, previousState, propertyNames)) {
            restorePreviousState(entity, id, currentState, previousState, propertyNames);
            updateModifiedFlag(true, entity, id);
            System.out.println("modificationSkipped is set to true");
            return ArrayHelper.EMPTY_INT_ARRAY;
        }
        return super.findDirty(entity, id, currentState, previousState, propertyNames, types);
    }

    @Override
    public String onPrepareStatement(String sql) {
        return super.onPrepareStatement(sql);
    }

    private boolean isModified(Object entity, Object[] previousState, String[] propertyNames) {
        if (isAnnotationPresent(entity)) {
            // Extract the values from the annotation
            String attributeName = getAttributeName(entity);
            String attributeType = getAttributeType(entity);
            int propertyPosition = findIndex(propertyNames, attributeName);
            if (isPropertyNotFound(propertyPosition)) {
                return true;
            }
            return isCurrentAttributeEqualsToPreviousState(
                    entity, previousState, propertyPosition, attributeName, attributeType);
        }
        return true;
    }

    private boolean isCurrentAttributeEqualsToPreviousState(
            Object entity,
            Object[] previousState,
            int position,
            String attributeName,
            String attributeType) {
        var attributeValue = new Object();
        // Use Reflection to get the value
        try {
            attributeValue = PropertyUtils.getProperty(entity, attributeName);
        } catch (Exception ex) {
        }
        return checkAreEquals(previousState, position, attributeType, attributeValue);
    }

    private boolean isAnnotationPresent(Object entity) {
        return entity.getClass().isAnnotationPresent(SkipUpdateOnCondition.class);
    }

    private String getAttributeName(Object entity) {
        return entity.getClass().getAnnotation(SkipUpdateOnCondition.class).attributeName();
    }

    private String getAttributeType(Object entity) {
        return entity.getClass().getAnnotation(SkipUpdateOnCondition.class).type();
    }

    private boolean isPropertyNotFound(int position) {
        return position == -1;
    }

    private int findIndex(Object[] arr, String t) {
        int len = arr.length;
        return IntStream.range(0, len).filter(i -> t.equals(arr[i])).findFirst().orElse(-1);
    }

    private boolean checkAreEquals(
            Object[] previousState, int position, String attributeType, Object attributeValue) {
        if (attributeValue == null && previousState[position] == null) {
            return true;
        }
        if (attributeValue == null || previousState[position] == null) {
            return false;
        }
        if (Timestamp.class.getName().equals(attributeType) && attributeValue instanceof Timestamp ts) {
            return isCurrentDateEqualsOrAfterExistingDate(ts, (Timestamp) previousState[position]);
        }
        return attributeValue.equals(previousState[position]);
    }

    private boolean isCurrentDateEqualsOrAfterExistingDate(Timestamp current, Timestamp existing) {
        return current != null
                && existing != null
                && (current.after(existing) || current.equals(existing));
    }

    private void updateModifiedFlag(boolean b, Object entity, Serializable id) {
        try {
            if (PropertyUtils.isReadable(entity, MODIFICATION_SKIPPED)
                    && PropertyUtils.isWriteable(entity, MODIFICATION_SKIPPED)) {
                PropertyUtils.setProperty(entity, MODIFICATION_SKIPPED, b);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {

        }
    }

    private void restorePreviousState(
            Object entity,
            Serializable id,
            Object[] currentState,
            Object[] previousState,
            String[] propertyNames) {
        for (var i = 0; i < propertyNames.length; i++) {
            try {
                if (currentState[i] != null
                        && previousState[i] != null
                        && !currentState[i].equals(previousState[i])) {
                    PropertyUtils.setProperty(entity, propertyNames[i], previousState[i]);
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            }
        }
    }
}
