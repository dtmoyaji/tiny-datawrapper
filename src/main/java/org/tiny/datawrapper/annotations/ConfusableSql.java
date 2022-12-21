/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tiny.datawrapper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * エラーを発生させる原因になりうる埋め込みコード.
 * やむを得ず使用しているが、移植などでチェックが必要なものにタグ付けする。
 * @author Takahiro MURAKAMI
 */
@Target({ElementType.LOCAL_VARIABLE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfusableSql {
    
}
