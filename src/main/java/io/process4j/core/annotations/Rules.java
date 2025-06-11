package io.process4j.core.annotations;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface Rules
{
   String resource();

   String key();

   boolean lexicographical() default true;

   boolean shortlex() default false;
}
