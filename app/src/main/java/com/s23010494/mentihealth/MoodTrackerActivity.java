package com.s23010494.mentihealth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MoodTrackerActivity extends AppCompatActivity {
    private TextView tvMoodTitle, tvMoodText;
    private ImageButton[] emojiButtons;
    private String selectedMood = "";
    private int selectedEmojiIndex = -1;
    private DBHelper dbHelper;
    private String email;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_tracker);

        dbHelper = new DBHelper(this);
        email = getIntent().getStringExtra("EMAIL");
        userName = getIntent().getStringExtra("NAME");

        if (userName == null || userName.isEmpty()) {
            userName = "User";
        }

        tvMoodTitle = findViewById(R.id.tv_mood_title);
        tvMoodText = findViewById(R.id.tv_mood_text);
        tvMoodTitle.setText("How is your mood today, " + userName + "?");

        emojiButtons = new ImageButton[]{
                findViewById(R.id.emoji_yay),
                findViewById(R.id.emoji_nice),
                findViewById(R.id.emoji_meh),
                findViewById(R.id.emoji_eh),
                findViewById(R.id.emoji_ugh)
        };

        setupEmojiListeners();

        Button btnMoveForward = findViewById(R.id.btn_move_forward);
        btnMoveForward.setOnClickListener(v -> {
            if (selectedMood.isEmpty()) {
                Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean saved = dbHelper.addUserMood(email, selectedMood);
            if (saved) {
                Intent intent = new Intent(this, HowIsTodayActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("NAME", userName);
                intent.putExtra("MOOD", selectedMood); // CRITICAL FIX: ADD THIS LINE
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to save mood", Toast.LENGTH_SHORT).show();
            }
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
        for (int i = 0; i < emojiButtons.length; i++) {
            emojiButtons[i].setSelected(false);
        }
        emojiButtons[selectedIndex].setSelected(true);
        selectedEmojiIndex = selectedIndex;
    }
}
