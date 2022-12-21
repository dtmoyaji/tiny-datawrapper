/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tiny.datawrapper.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 *
 * @author Takahiro MURAKAMI
 */
@Retention(RetentionPolicy.RUNTIME)
@Component
@Configuration
public @interface ClearfyTable {
    @AliasFor(annotation=Component.class, attribute="value")
    String value() default "";
}
