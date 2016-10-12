package com.macys.sdt.framework.htmlelements.annotations;

import java.lang.annotation.*;

/**
 * Created by atepliashin on 10/6/16.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface FindAll {
    FindBy[] value();
}
