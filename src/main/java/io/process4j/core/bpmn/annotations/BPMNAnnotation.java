package io.process4j.core.bpmn.annotations;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
@Repeatable(BPMNAnnotations.class)
public @interface BPMNAnnotation
{
   String association();

   String textAnnotation();

   String value() default "";

   String waypointCoordinates() default "0,0;100,100";

   String x();

   String y();

   String width() default "100";

   String height() default "30";

}
