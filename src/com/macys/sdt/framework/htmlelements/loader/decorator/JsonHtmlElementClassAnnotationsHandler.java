package com.macys.sdt.framework.htmlelements.loader.decorator;

import com.macys.sdt.framework.interactions.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.AbstractAnnotations;
import ru.yandex.qatools.htmlelements.element.HtmlElement;
import ru.yandex.qatools.htmlelements.exceptions.HtmlElementsException;

/**
 * Created by atepliashin on 10/6/16.
 */
public class JsonHtmlElementClassAnnotationsHandler<T extends HtmlElement> extends AbstractAnnotations {

    private final Class<T> elementClass;

    public JsonHtmlElementClassAnnotationsHandler(Class<T> elementClass) {
        this.elementClass = elementClass;
    }

    @Override
    public By buildBy() {
        Class<?> clazz = elementClass;
        while (clazz != Object.class) {
            if (clazz.isAnnotationPresent(com.macys.sdt.framework.htmlelements.annotations.FindBy.class)) {
                return Elements.element(clazz.getAnnotation(com.macys.sdt.framework.htmlelements.annotations.FindBy.class).jsonPath());
            }
            if (clazz.isAnnotationPresent(FindBy.class)) {
                return buildByFromFindBy(clazz.getAnnotation(FindBy.class));
            }
            clazz = clazz.getSuperclass();
        }
        throw new HtmlElementsException(String.format("Cannot determine how to locate instance of %s", elementClass));
    }

    @Override
    public boolean isLookupCached() {
        return false;
    }
}