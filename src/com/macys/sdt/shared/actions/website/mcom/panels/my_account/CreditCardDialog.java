package com.macys.sdt.shared.actions.website.mcom.panels.my_account;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.DropDowns;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.TextBoxes;
import com.macys.sdt.framework.model.CreditCard;
import com.macys.sdt.framework.model.ProfileAddress;
import com.macys.sdt.framework.utils.StatesUtils;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.TestUsers;
import org.openqa.selenium.WebElement;

import java.util.HashMap;

public class CreditCardDialog extends StepUtils{

    public static void addCreditCard() throws Exception{
        CreditCard visaCreditCard = TestUsers.getValidVisaCreditCard();
        addCreditCard(visaCreditCard);
    }

    public static void addCreditCard(CreditCard creditCard) {
        if (bloomingdales()) {
            DropDowns.selectCustomText("credit_card_dialog.card_type_list", "credit_card_dialog.card_type_options", creditCard.getCardType().name);
        } else {
            DropDowns.selectByText("credit_card_dialog.card_type", creditCard.getCardType().name);
        }
        TextBoxes.typeTextbox("credit_card_dialog.card_number", creditCard.getCardNumber());
        if (bloomingdales()) {
            DropDowns.selectCustomText("credit_card_dialog.expiry_month_list", "credit_card_dialog.expiry_month_options", creditCard.getExpiryMonthIndex() + " - " + creditCard.getExpiryMonth());
            DropDowns.selectCustomText("credit_card_dialog.expiry_year_list", "credit_card_dialog.expiry_year_options", creditCard.getExpiryYear());
        } else {
            DropDowns.selectByText("credit_card_dialog.expiry_month", creditCard.getExpiryMonthIndex() + " - " + creditCard.getExpiryMonth());
            DropDowns.selectByText("credit_card_dialog.expiry_year", creditCard.getExpiryYear());
        }
        HashMap<String, String> opts = new HashMap<>();
        opts.put("checkout_eligible", "true");
        ProfileAddress address = TestUsers.getRandomValidAddress(opts);
        TextBoxes.typeTextbox("credit_card_dialog.first_name", address.getFirstName());
        TextBoxes.typeTextbox("credit_card_dialog.last_name", address.getLastName());
        TextBoxes.typeTextbox("credit_card_dialog.address_line_1", address.getAddressLine1());
        TextBoxes.typeTextbox("credit_card_dialog.address_city", address.getCity());
        if (bloomingdales())  {
            DropDowns.selectCustomText("credit_card_dialog.address_state_list", "credit_card_dialog.address_state_options", address.getState());
        }  else  {
            DropDowns.selectByText("credit_card_dialog.address_state", StatesUtils.translateAbbreviation(address.getState()));
        }
        TextBoxes.typeTextbox("credit_card_dialog.address_zip_code", address.getZipCode().toString());
        String phoneNumber = TestUsers.generateRandomPhoneNumber();
        TextBoxes.typeTextbox("credit_card_dialog.card_phone_area_code", phoneNumber.substring(0, 3));
        TextBoxes.typeTextbox("credit_card_dialog.card_phone_exchange", phoneNumber.substring(3, 6));
        TextBoxes.typeTextbox("credit_card_dialog.card_phone_subscriber", phoneNumber.substring(6));
        TextBoxes.typeTextbox("credit_card_dialog.payment_email", TestUsers.generateRandomEmail(30));
        WebElement set_as_a_default_card = Elements.findElement("credit_card_dialog.set_as_a_default_card");
        if (!set_as_a_default_card.isSelected())
            Clicks.click(set_as_a_default_card);
        Clicks.click("credit_card_dialog.save_card");
    }
}
