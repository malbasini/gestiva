package com.gestiva.help.web;

public class HelpChatResponse {

    private String answer;

    public HelpChatResponse() {
    }

    public HelpChatResponse(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}