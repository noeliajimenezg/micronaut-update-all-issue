package org.example.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation to add where clauses to the insert or update clauses. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SkipUpdateOnCondition {

    /**
     * Name of the attribute in JPA/Hibernate.
     *
     * @return Attribute name.
     */
    String attributeName() default "";

    /**
     * Type of the data in JPA/Hibernate.
     *
     * @return Data type.
     */
    String type() default "java.lang.String";
}
