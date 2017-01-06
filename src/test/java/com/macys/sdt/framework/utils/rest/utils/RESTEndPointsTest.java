package com.macys.sdt.framework.utils.rest.utils;

import com.macys.sdt.framework.model.GiftCard;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.rest.services.GiftCardService;
import org.junit.Assert;
import org.junit.Test;

public class RESTEndPointsTest {

    @Test
    public void testGetGiftCardServiceUrl() throws Exception {
        MainRunner.url = "http://www.qa0codemacys.fds.com";
        Assert.assertNotNull(GiftCardService.getGiftCardServiceUrl(GiftCard.CardType.EGC));
    }
}