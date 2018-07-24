package com.volunteer.thc.volunteerapp.model;

import java.util.Map;

/**
 * Created by Cristi on 1/8/2018.
 */

public class InterviewQuestion {

    private String questionText;
    private String answerText;
    private String id;

    private Map<String, Boolean> answerList;

    private AnswerType answerType;

    public InterviewQuestion() {
        super();
    }

    public InterviewQuestion(String questionText, Map<String, Boolean> answerList, AnswerType answerType) {
        this.questionText = questionText;
        this.answerList = answerList;
        this.answerType = answerType;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public Map<String, Boolean> getAnswerList() {
        return answerList;
    }

    public void setAnswerList(Map<String, Boolean> answerList) {
        this.answerList = answerList;
    }

    public AnswerType getAnswerType() {
        return answerType;
    }

    public void setAnswerType(AnswerType answerType) {
        this.answerType = answerType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public enum AnswerType {
        YES_NO,
        SELECT_ONE,
        SELECT_MANY,
        WRITE_ANSWER;
    }
}
