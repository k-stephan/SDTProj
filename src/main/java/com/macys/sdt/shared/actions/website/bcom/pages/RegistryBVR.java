package com.macys.sdt.shared.actions.website.bcom.pages;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.DropDowns;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegistryBVR extends StepUtils {

    public static ArrayList<Object> getProducts() {

        HashMap<String, String> prod;
        Wait.secondsUntilElementPresent("registry_bvr.bvr_prod_list", 10);
        List<WebElement> prod_count = Elements.findElements("registry_bvr.bvr_prod_list");
        int size = prod_count.size();
        ArrayList<Object> productHashes = new ArrayList<>();
        if (size > 0) {
            for (int i = 0; i < size; i++) {

                prod = new HashMap<>();
                prod.put("product_desc", prod_count.get(i).findElement(Elements.element("registry_bvr.prod_desc")).getText());
                prod.put("product_price", prod_count.get(i).findElement(Elements.element("registry_bvr.prod_price")).getText());
                if(url().contains("wgl")){
                    String purchaseMessage = null;
                    for(WebElement element : prod_count.get(i).findElement(Elements.element("registry_bvr.product_thumbnail")).findElements(By.tagName("p"))){
                        Pattern p = Pattern.compile("(\\d+) of (\\d+) purchased");
                        Matcher m = p.matcher(element.getText());
                        if(m.find())
                            purchaseMessage = element.getText();
                    }
                    if(purchaseMessage == null)
                        Assert.fail("ERROR ENV: purchase summary is not found on page!!");
                    prod.put("product_reqs_quat", purchaseMessage.split(" ")[0]);
                    prod.put("product_open_quat", purchaseMessage.split(" ")[2]);
                } else {
                    prod.put("product_reqs_quat", prod_count.get(i).findElement(Elements.element("registry_bvr.prod_reqs_qunt")).getAttribute("value"));
                    prod.put("product_open_quat", prod_count.get(i).findElement(Elements.element("registry_bvr.prod_open_qunt")).getAttribute("value"));
                }
                productHashes.add(prod);
            }
        }
        return productHashes;
    }

    public static void selectProdQuantity(int index, String quantity) {
        try {
            if (macys()) {
                List<WebElement> prod_count = Elements.findElements("registry_bvr.prod_sel_qunt");
                DropDowns.selectByText(prod_count.get(index), quantity);
            } else {
                List<WebElement> prod_count = Elements.findElements("registry_bvr.prod_sel_qunt");
                DropDowns.selectByText(prod_count.get(index), quantity);
            }
        } catch (Exception e) {
            Assert.fail("Unable to select quantity of prod on BVR page" + e);
        }
    }

    public static void registryAddToBag() {
        Wait.untilElementPresent("registry_bvr.prod_add_to_bag");
        Clicks.click("registry_bvr.prod_add_to_bag");
        Wait.untilElementPresent("registry_add_to_bag.registry_chkout_button");
        if (Elements.elementPresent("registry_add_to_bag.registry_chkout_button"))
            Clicks.click("registry_add_to_bag.registry_chkout_button");
        else
            Assert.fail("Unable to add registry product to bag");
    }
}
