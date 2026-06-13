package com.gestiva.help.web;

public class HelpGuideResult {

    private boolean handled;
    private String answer;

    public HelpGuideResult() {
    }

    public HelpGuideResult(boolean handled, String answer) {
        this.handled = handled;
        this.answer = answer;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public static HelpGuideResult handled(String answer) {
        return new HelpGuideResult(true, answer);
    }

    public static HelpGuideResult notHandled() {
        return new HelpGuideResult(false, null);
    }
}
