package com.s23010494.mentihealth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MoodTrackerActivity extends AppCompatActivity {
    private TextView tvMoodTitle, tvMoodText;
    private ImageButton[] emojiButtons;
    private String selectedMood = "";
    private int selectedEmojiIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_tracker);

        // Get user name from intent
        String name = getIntent().getStringExtra("NAME");
        if (name == null || name.isEmpty()) {
            name = "User";
        }

        tvMoodTitle = findViewById(R.id.tv_mood_title);
        tvMoodText = findViewById(R.id.tv_mood_text);

        tvMoodTitle.setText("How is your mood today, " + name + "?");

        // Initialize emoji buttons using your actual drawable names
        emojiButtons = new ImageButton[]{
                findViewById(R.id.emoji_yay),    // yay.png
                findViewById(R.id.emoji_nice),   // nice.png
                findViewById(R.id.emoji_meh),    // meh.png
                findViewById(R.id.emoji_eh),     // eh.png
                findViewById(R.id.emoji_ugh)     // ugh.png
        };


        // Set click listeners for emojis
        setupEmojiListeners();

        // Move forward button
        Button btnMoveForward = findViewById(R.id.btn_move_forward);
        btnMoveForward.setOnClickListener(v -> {
            // Save mood to database or proceed to next screen
            // Intent intent = new Intent(this, NextActivity.class);
            // intent.putExtra("MOOD", selectedMood);
            // startActivity(intent);
            finish();
        });
    }

    private void setupEmojiListeners() {
        String[] moodTexts = {"Excellent!", "Good!", "Meh", "Not great", "Terrible"};

        for (int i = 0; i < emojiButtons.length; i++) {
            final int index = i;
            emojiButtons[i].setOnClickListener(v -> {
                selectEmoji(index);
                tvMoodText.setText(moodTexts[index]);
                selectedMood = moodTexts[index];
            });
        }
    }

    private void selectEmoji(int selectedIndex) {
        // Reset all emojis
        for (int i = 0; i < emojiButtons.length; i++) {
            emojiButtons[i].setSelected(false);
        }

        // Highlight selected emoji
        emojiButtons[selectedIndex].setSelected(true);
        selectedEmojiIndex = selectedIndex;
    }
}
