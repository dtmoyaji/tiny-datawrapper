package org.tiny.datawrapper.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 *
 * @author dtmoyaji
 */
@Retention(RetentionPolicy.RUNTIME)
@Component
@Configuration
public @interface TinyTable {
    @AliasFor(annotation=Component.class, attribute="value")
    String value() default "";
}
