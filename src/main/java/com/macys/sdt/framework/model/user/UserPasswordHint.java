package com.macys.sdt.framework.model.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * This class represents a UserPasswordHint and contains all the information about that UserPasswordHint
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPasswordHint {
    /**
     * This is to set security_question
     */
    private Long id;
    private String answer;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String question;

    public UserPasswordHint() {
    }

    public UserPasswordHint(Long id, String answer) {
        this.id = id;
        this.answer = answer;
    }

    /**
     * Gets the DefaultUserPasswordHint of UserPasswordHint
     *
     * @return UserPasswordHint DefaultUserPasswordHint
     */
    public static UserPasswordHint getDefaultUserPasswordHint() {
        return new UserPasswordHint(1L, "My Name");
    }

    /**
     * Gets the Id of UserPasswordHint
     *
     * @return UserPasswordHint Id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the Id of UserPasswordHint
     *
     * @param id UserPasswordHint
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the Answer of UserPasswordHint
     *
     * @return UserPasswordHint Answer
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Sets the Answer of UserPasswordHint
     *
     * @param answer UserPasswordHint
     */
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    /**
     * Gets the Question of UserPasswordHint
     *
     * @return UserPasswordHint Question
     */
    public String getQuestion() {
        return question;
    }

    /**
     * Sets the Question of UserPasswordHint
     *
     * @param question UserPasswordHint
     */
    public void setQuestion(String question) {
        this.question = question;
    }
}
