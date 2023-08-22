package org.tiny.datawrapper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * エラーを発生させる原因になりうる埋め込みコード.
 * やむを得ず使用しているが、移植などでチェックが必要なものにタグ付けする。
 * @author dtmoyaji
 */
@Target({ElementType.LOCAL_VARIABLE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfusableSql {
    
}
