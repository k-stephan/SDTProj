package com.macys.sdt.framework.shared.pages;

import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.htmlelements.loader.decorator.JsonHtmlElementLocatorFactory;
import com.macys.sdt.framework.runner.MainRunner;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import ru.yandex.qatools.htmlelements.loader.decorator.HtmlElementDecorator;

/**
 * Created by atepliashin on 10/7/16.
 */
abstract public class Page {
    protected WebDriver driver;

    public Page() {
        driver = MainRunner.getWebDriver();
        PageFactory.initElements(new HtmlElementDecorator(new JsonHtmlElementLocatorFactory(driver)), this);
    }

    public WebDriver driver() {
        return driver;
    }

    public void waitForReady() {
        Wait.forPageReady();
    }
}
