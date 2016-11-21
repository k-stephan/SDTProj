package com.macys.sdt.framework.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public class UserPasswordHint {
    /**
     * This is to set security_question
     */
    private Long id;
    private String answer;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String question;

    public static UserPasswordHint getDefaultUserPasswordHint() {
        return new UserPasswordHint(1L, "My Name");
    }

    public UserPasswordHint() {
    }

    public UserPasswordHint(Long id, String answer) {
        this.id = id;
        this.answer = answer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
