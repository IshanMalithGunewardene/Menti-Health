package com.s23010494.mentihealth;

public class JournalEntry {
    private final String mood;
    private final String text;
    private final String date;

    public JournalEntry(String mood, String text, String date) {
        this.mood = mood;
        this.text = text;
        this.date = date;
    }

    public String getMood() {
        return mood;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }
}

