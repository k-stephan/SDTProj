package com.macys.sdt.framework.model;

import com.macys.sdt.framework.model.user.UserPasswordHint;
import org.junit.Assert;
import org.junit.Test;

public class UserPasswordHintTest {

    private UserPasswordHint passwordHint;

    public UserPasswordHintTest() {
        passwordHint = new UserPasswordHint();
    }

    @Test
    public void testGetDefaultUserPasswordHint() throws Exception {
        passwordHint = UserPasswordHint.getDefaultUserPasswordHint();
        Assert.assertNotNull(passwordHint.getId());
        Assert.assertNotNull(passwordHint.getAnswer());
    }

    @Test
    public void testGetId() throws Exception {
        Long id = 1234L;
        passwordHint.setId(id);
        Assert.assertEquals(passwordHint.getId(), id);
    }

    @Test
    public void testGetAnswer() throws Exception {
        String password = "Macys@123";
        passwordHint.setAnswer(password);
        Assert.assertEquals(passwordHint.getAnswer(), password);
    }

    @Test
    public void testGetQuestion() throws Exception {
        String question = "What was the name of your first pet?";
        passwordHint.setQuestion(question);
        Assert.assertEquals(passwordHint.getQuestion(), question);
    }
}