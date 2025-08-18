package com.s23010494.mentihealth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditJournalActivity extends AppCompatActivity {
    private EditText etEditEntry;
    private TextView tvMoodTitle, tvSelectedMood;
    private ImageButton[] emojiButtons;
    private Button btnSaveChanges;
    private String selectedMood = "";
    private int selectedEmojiIndex = -1;
    private DBHelper dbHelper;
    private int entryId;
    private String originalMood, originalText, originalDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_journal);

        dbHelper = new DBHelper(this);
        
        // Get data from intent
        entryId = getIntent().getIntExtra("ENTRY_ID", -1);
        originalMood = getIntent().getStringExtra("ENTRY_MOOD");
        originalText = getIntent().getStringExtra("ENTRY_TEXT");
        originalDate = getIntent().getStringExtra("ENTRY_DATE");

        if (entryId == -1) {
            Toast.makeText(this, "Error: Entry not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupEmojiListeners();
        setupSaveButton();
        
        // Populate fields with existing data
        etEditEntry.setText(originalText);
        selectedMood = originalMood;
        tvSelectedMood.setText(originalMood);
        selectEmojiFromMood(originalMood);
    }

    private void initViews() {
        etEditEntry = findViewById(R.id.et_edit_entry);
        tvMoodTitle = findViewById(R.id.tv_mood_title);
        tvSelectedMood = findViewById(R.id.tv_selected_mood);
        btnSaveChanges = findViewById(R.id.btn_save_changes);
        
        emojiButtons = new ImageButton[]{
                findViewById(R.id.emoji_yay),
                findViewById(R.id.emoji_nice),
                findViewById(R.id.emoji_meh),
                findViewById(R.id.emoji_eh),
                findViewById(R.id.emoji_ugh)
        };
    }

    private void setupEmojiListeners() {
        String[] moodTexts = {"Excellent!", "Good!", "Meh", "Not great", "Terrible"};
        for (int i = 0; i < emojiButtons.length; i++) {
            final int index = i;
            emojiButtons[i].setOnClickListener(v -> {
                selectEmoji(index);
                tvSelectedMood.setText(moodTexts[index]);
                selectedMood = moodTexts[index];
            });
        }
    }

    private void setupSaveButton() {
        btnSaveChanges.setOnClickListener(v -> {
            String entryText = etEditEntry.getText().toString().trim();
            
            if (entryText.isEmpty()) {
                Toast.makeText(this, "Please write something", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (selectedMood.isEmpty()) {
                Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update the journal entry
            boolean updated = dbHelper.updateJournalEntry(entryId, selectedMood, entryText, originalDate);
            
            if (updated) {
                Toast.makeText(this, "Entry updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update entry", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectEmoji(int selectedIndex) {
        for (int i = 0; i < emojiButtons.length; i++) {
            emojiButtons[i].setSelected(false);
        }
        emojiButtons[selectedIndex].setSelected(true);
        selectedEmojiIndex = selectedIndex;
    }

    private void selectEmojiFromMood(String mood) {
        String[] moodTexts = {"Excellent!", "Good!", "Meh", "Not great", "Terrible"};
        for (int i = 0; i < moodTexts.length; i++) {
            if (moodTexts[i].equals(mood)) {
                selectEmoji(i);
                break;
            }
        }
    }
}
