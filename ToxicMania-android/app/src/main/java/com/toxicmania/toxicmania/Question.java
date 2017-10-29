package com.toxicmania.toxicmania;

/**
 * Created by Vihanga Liyanage on 10/12/2017.
 */

public class Question {
    private String id;
    private String text;
    private boolean readability;
    private boolean skipped;
    private String toxicity;

    public Question(String id, String text) {
        this.id = id;
        this.text = text;
        skipped = false;
        readability = true;
        toxicity = "";
    }

    @Override
    public String toString() {
        return id + ": " + text + "\nreadability: " + readability + "\ntoxicity: " + toxicity;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setReadability(boolean readability) {
        this.readability = readability;
    }

    public void setToxicity(String toxicity) {
        this.toxicity = toxicity;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public boolean getReadability() {
        return readability;
    }

    public String getToxicity() {
        return toxicity;
    }
}
