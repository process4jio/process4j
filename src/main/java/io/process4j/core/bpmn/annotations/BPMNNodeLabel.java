package io.process4j.core.bpmn.annotations;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface BPMNNodeLabel
{
   String x();

   String y();

   String width() default "";

   String height() default "";
}
