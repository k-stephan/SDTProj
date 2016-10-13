package com.macys.sdt.framework.htmlelements.loader.decorator;

import com.google.common.base.CaseFormat;
import com.macys.sdt.framework.interactions.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.Annotations;
import org.openqa.selenium.support.pagefactory.ByAll;
import org.openqa.selenium.support.pagefactory.ByChained;
import ru.yandex.qatools.htmlelements.exceptions.HtmlElementsException;

import java.lang.reflect.Field;

import static ru.yandex.qatools.htmlelements.utils.HtmlElementUtils.*;
import static ru.yandex.qatools.htmlelements.utils.HtmlElementUtils.getGenericParameterClass;

/**
 * Created by atepliashin on 10/6/16.
 */
public class JsonHtmlElementFieldAnnotationsHandler extends Annotations {
    public JsonHtmlElementFieldAnnotationsHandler(Field field) {
        super(field);
    }

    @Override
    public By buildBy() {

        if (isHtmlElement(getField()) || isTypifiedElement(getField())) {
            return buildByFromHtmlElementAnnotations();
        }
        if (isHtmlElementList(getField()) || isTypifiedElementList(getField())) {
            return buildByFromHtmlElementListAnnotations();
        }

        return buildWebElementBy();
    }

    private By buildByFromFindAnnotations() {
        if (getField().isAnnotationPresent(com.macys.sdt.framework.htmlelements.annotations.FindBys.class)) {
            com.macys.sdt.framework.htmlelements.annotations.FindBys findBys = getField().getAnnotation(com.macys.sdt.framework.htmlelements.annotations.FindBys.class);
            return new ByChained(findBysToBys(findBys.value()));
        }
        if (getField().isAnnotationPresent(FindBys.class)) {
            FindBys findBys = getField().getAnnotation(FindBys.class);
            return buildByFromFindBys(findBys);
        }
        if (getField().isAnnotationPresent(com.macys.sdt.framework.htmlelements.annotations.FindAll.class)) {
            com.macys.sdt.framework.htmlelements.annotations.FindAll findAll = getField().getAnnotation(com.macys.sdt.framework.htmlelements.annotations.FindAll.class);
            return new ByAll(findBysToBys(findAll.value()));
        }
        if (getField().isAnnotationPresent(FindAll.class)) {
            FindAll findBys = getField().getAnnotation(FindAll.class);
            return buildBysFromFindByOneOf(findBys);
        }
        if (getField().isAnnotationPresent(com.macys.sdt.framework.htmlelements.annotations.FindBy.class)) {
            com.macys.sdt.framework.htmlelements.annotations.FindBy findBy = getField().getAnnotation(com.macys.sdt.framework.htmlelements.annotations.FindBy.class);
            return Elements.element(findBy.jsonPath());
        }
        if (getField().isAnnotationPresent(FindBy.class)) {
            FindBy findBy = getField().getAnnotation(FindBy.class);
            return buildByFromFindBy(findBy);
        }

        // this block allows to map fields to selectors from json implicitly
        // it makes sense only for web elements. for html elements it's needed to exclude the case when it has its own
        // selector to find it.

        // example:
        // @FindBy(jsonPath = "some_page")
        // public class SomePage extends Page { WebElement someField; }
        //
        // will allow to extract selector by path "some_page.some_field"

        Class<?> declaringClass = getField().getDeclaringClass();
        if (declaringClass.isAnnotationPresent(com.macys.sdt.framework.htmlelements.annotations.FindBy.class)
                && (isWebElement(getField()) || isWebElementList(getField())) ) {
            String jsonPath = declaringClass.getAnnotation(com.macys.sdt.framework.htmlelements.annotations.FindBy.class).jsonPath();
            if ((!isHtmlElement(getField()) && !isHtmlElementList(getField())) || !jsonPath.matches(".+\\..+")) {
                return Elements.element(String.format("%s.%s", jsonPath, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, getField().getName())));
            }
        }

        return null;
    }

    private By[] findBysToBys(com.macys.sdt.framework.htmlelements.annotations.FindBy[] findByArray) {
        By[] byArray = new By[findByArray.length];
        for (int i = 0; i < findByArray.length; ++i) {
            byArray[i] = Elements.element(findByArray[i].jsonPath());
        }
        return byArray;
    }

    private By buildByFromHtmlElementAnnotations() {
        assertValidAnnotations();

        By result = buildByFromFindAnnotations();
        if (result != null) {
            return result;
        }

        Class<?> fieldClass = getField().getType();
        while (fieldClass != Object.class) {
            if (fieldClass.isAnnotationPresent(com.macys.sdt.framework.htmlelements.annotations.FindBy.class)) {
                return Elements.element(
                        fieldClass.getAnnotation(com.macys.sdt.framework.htmlelements.annotations.FindBy.class).jsonPath());
            }
            if (fieldClass.isAnnotationPresent(FindBy.class)) {
                return buildByFromFindBy(fieldClass.getAnnotation(FindBy.class));
            }
            fieldClass = fieldClass.getSuperclass();
        }

        return buildByFromDefault();
    }

    private By buildByFromHtmlElementListAnnotations() {
        assertValidAnnotations();

        By result = buildByFromFindAnnotations();
        if (result != null) {
            return result;
        }

        Class<?> listParameterClass = getGenericParameterClass(getField());
        while (listParameterClass != Object.class) {
            if (listParameterClass.isAnnotationPresent(com.macys.sdt.framework.htmlelements.annotations.FindBy.class)) {
                return Elements.element(
                        listParameterClass.getAnnotation(com.macys.sdt.framework.htmlelements.annotations.FindBy.class).jsonPath());
            }
            if (listParameterClass.isAnnotationPresent(FindBy.class)) {
                return buildByFromFindBy(listParameterClass.getAnnotation(FindBy.class));
            }
            listParameterClass = listParameterClass.getSuperclass();
        }

        throw new HtmlElementsException(String.format("Cannot determine how to locate element %s", getField()));
    }

    private By buildWebElementBy() {
        assertValidAnnotations();

        By by = buildByFromFindAnnotations();

        if (by == null) {
            by = buildByFromDefault();
        }

        if (by == null) {
            throw new IllegalArgumentException("Cannot determine how to locate element " + getField());
        } else {
            return by;
        }
    }

    @Override
    protected void assertValidAnnotations() {
        FindBys findBys = getField().getAnnotation(FindBys.class);
        com.macys.sdt.framework.htmlelements.annotations.FindBys jsonFindBys = getField().getAnnotation(com.macys.sdt.framework.htmlelements.annotations.FindBys.class);
        FindAll findAll = getField().getAnnotation(FindAll.class);
        com.macys.sdt.framework.htmlelements.annotations.FindAll jsonFindAll = getField().getAnnotation(com.macys.sdt.framework.htmlelements.annotations.FindAll.class);
        FindBy findBy = getField().getAnnotation(FindBy.class);
        com.macys.sdt.framework.htmlelements.annotations.FindBy jsonFindBy = getField().getAnnotation(com.macys.sdt.framework.htmlelements.annotations.FindBy.class);
        if ((findBys != null && findBy != null) || (jsonFindBys != null && jsonFindBy != null)) {
            throw new IllegalArgumentException("If you use a \'@FindBys\' annotation, you must not also use a \'@FindBy\' annotation");
        } else if (findAll != null && findBy != null || (jsonFindAll != null && jsonFindBy != null)) {
            throw new IllegalArgumentException("If you use a \'@FindAll\' annotation, you must not also use a \'@FindBy\' annotation");
        } else if ((findAll != null && findBys != null) || (jsonFindAll != null && jsonFindBys != null)) {
            throw new IllegalArgumentException("If you use a \'@FindAll\' annotation, you must not also use a \'@FindBys\' annotation");
        }
    }
}
