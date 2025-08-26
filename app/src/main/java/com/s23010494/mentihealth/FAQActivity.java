package com.s23010494.mentihealth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class FAQActivity extends AppCompatActivity {
    private String email, name;
    private LinearLayout faqContainer;

    // FAQ data
    private String[][] faqData = {
            {"How do I track my mood?", "Tap the '+' button in the bottom navigation and select your current mood from the available options. Your mood will be saved and displayed in the calendar view."},
            {"How do I view my mood history?", "Go to the Calendar tab to see your mood history displayed as colorful emoji icons on each day. You can navigate between months using the arrow buttons."},
            {"What do the different mood levels mean?", "We have 5 mood levels:\n• Excellent! (😄) - Feeling fantastic\n• Good! (😊) - Feeling positive\n• Meh (😐) - Feeling neutral\n• Not great (😟) - Feeling down\n• Terrible (😢) - Feeling very low"},
            {"How do I write journal entries?", "Navigate to the Journal tab and tap 'Add New Entry' to write about your thoughts and feelings. You can edit or delete entries anytime."},
            {"What does the Stats page show?", "The Stats page displays your mood trends over time with a smooth line chart, your daily step count with a progress arc, and a map showing nearby parks for walking."},
            {"How does step tracking work?", "The app uses your device's built-in step sensor to count your daily steps. Your progress toward the 10,000 steps daily goal is shown with a colorful progress arc."},
            {"How can I reset my password?", "On the login screen, tap 'Forgot Password?' and enter your email address. You'll receive instructions to reset your password."},
            {"Is my data secure?", "Yes, all your mood data and journal entries are stored securely on your device. We prioritize your privacy and don't share personal information."},
            {"Can I export my data?", "Currently, data export is not available, but we're working on adding this feature in future updates."},
            {"How do I delete my account?", "To delete your account and all associated data, please contact our support team through the app settings."}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        // Get user data from intent
        email = getIntent().getStringExtra("EMAIL");
        name = getIntent().getStringExtra("NAME");

        initViews();
        populateFAQ();
        setupBottomNavigation();
        setupBackButton();
    }

    private void initViews() {
        faqContainer = findViewById(R.id.faq_container);
    }

    private void populateFAQ() {
        for (int i = 0; i < faqData.length; i++) {
            addFAQItem(faqData[i][0], faqData[i][1]);
        }
    }

    private void addFAQItem(String question, String answer) {
        // Create the FAQ item container
        LinearLayout faqItem = new LinearLayout(this);
        faqItem.setOrientation(LinearLayout.VERTICAL);
        faqItem.setPadding(16, 12, 16, 12);
        faqItem.setBackgroundResource(R.drawable.faq_item_background);
        
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        itemParams.bottomMargin = 12;
        faqItem.setLayoutParams(itemParams);

        // Question TextView
        TextView questionView = new TextView(this);
        questionView.setText(question);
        questionView.setTextSize(16);
        questionView.setTextColor(getResources().getColor(android.R.color.white));
        questionView.setTypeface(null, android.graphics.Typeface.BOLD);
        questionView.setPadding(0, 0, 0, 8);

        // Answer TextView (initially hidden)
        TextView answerView = new TextView(this);
        answerView.setText(answer);
        answerView.setTextSize(14);
        answerView.setTextColor(getResources().getColor(R.color.light_gray, null));
        answerView.setVisibility(View.GONE);
        answerView.setPadding(0, 8, 0, 0);
        answerView.setLineSpacing(4, 1.0f);

        // Add click listener to toggle answer visibility
        faqItem.setOnClickListener(v -> {
            if (answerView.getVisibility() == View.GONE) {
                answerView.setVisibility(View.VISIBLE);
                faqItem.setBackgroundResource(R.drawable.faq_item_background_expanded);
            } else {
                answerView.setVisibility(View.GONE);
                faqItem.setBackgroundResource(R.drawable.faq_item_background);
            }
        });

        faqItem.addView(questionView);
        faqItem.addView(answerView);
        faqContainer.addView(faqItem);
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupBottomNavigation() {
        ImageButton btnJournal = findViewById(R.id.btn_journal);
        ImageButton btnStats = findViewById(R.id.btn_stats);
        ImageButton btnPlus = findViewById(R.id.btn_plus);
        ImageButton btnCalendar = findViewById(R.id.btn_calendar);

        btnJournal.setOnClickListener(v -> {
            Intent intent = new Intent(this, JournalDashboardActivity.class);
            intent.putExtra("EMAIL", email);
            intent.putExtra("NAME", name);
            startActivity(intent);
            finish();
        });

        btnStats.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatsActivity.class);
            intent.putExtra("EMAIL", email);
            intent.putExtra("NAME", name);
            startActivity(intent);
            finish();
        });

        btnPlus.setOnClickListener(v -> {
            Intent intent = new Intent(this, MoodTrackerActivity.class);
            intent.putExtra("EMAIL", email);
            intent.putExtra("NAME", name);
            startActivity(intent);
        });

        btnCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarActivity.class);
            intent.putExtra("EMAIL", email);
            intent.putExtra("NAME", name);
            startActivity(intent);
            finish();
        });

        ImageButton btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(v -> showPopupMenu(v));
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenu().add(0, 1, 0, "Settings");
        popupMenu.getMenu().add(0, 2, 0, "Logout");
        
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == 1) { // Settings
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("NAME", name);
                startActivity(intent);
                return true;
            } else if (itemId == 2) { // Logout
                showLogoutConfirmationDialog();
                return true;
            }
            return false;
        });
        
        popupMenu.show();
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Clear session data
                    SessionManager sessionManager = new SessionManager(this);
                    sessionManager.logoutUser();
                    
                    // Show logout message
                    Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Redirect to welcome screen
                    Intent intent = new Intent(this, welcome.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
