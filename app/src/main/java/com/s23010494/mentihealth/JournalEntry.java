package com.s23010494.mentihealth;

public class JournalEntry {
    private final int id;
    private final String mood;
    private final String text;
    private final String date;

    public JournalEntry(int id, String mood, String text, String date) {
        this.id = id;
        this.mood = mood;
        this.text = text;
        this.date = date;
    }

    // Constructor for backward compatibility (without ID)
    public JournalEntry(String mood, String text, String date) {
        this(-1, mood, text, date);
    }

    public int getId() {
        return id;
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

