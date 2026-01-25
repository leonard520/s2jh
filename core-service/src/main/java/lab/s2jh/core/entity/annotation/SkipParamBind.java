package lab.s2jh.core.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark setter methods that should be skipped during parameter binding.
 * Used for security-sensitive properties that should not be bound from request parameters.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface SkipParamBind {

}
