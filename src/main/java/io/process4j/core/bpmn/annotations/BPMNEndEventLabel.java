package io.process4j.core.bpmn.annotations;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
@Repeatable(BPMNEndEventLabels.class)
public @interface BPMNEndEventLabel
{
   String x();

   String y();

   String width() default "";

   String height() default "";
}
