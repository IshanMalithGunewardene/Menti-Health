package com.s23010494.mentihealth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.view.View;
import android.widget.PopupMenu;
import android.view.MenuItem;
import java.util.Calendar;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {
    private TextView tvCurrentMonth;
    private LinearLayout calendarGrid;
    private ImageButton btnPrevMonth, btnNextMonth;
    private DBHelper dbHelper;
    private String email, name;
    private Calendar currentCalendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private SimpleDateFormat yearMonthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        dbHelper = new DBHelper(this);
        email = getIntent().getStringExtra("EMAIL");
        name = getIntent().getStringExtra("NAME");

        currentCalendar = Calendar.getInstance();
        
        initViews();
        setupNavigation();
        setupBottomNavigation();
        updateCalendarDisplay();
    }

    private void initViews() {
        tvCurrentMonth = findViewById(R.id.tv_current_month);
        calendarGrid = findViewById(R.id.calendar_grid);
        btnPrevMonth = findViewById(R.id.btn_prev_month);
        btnNextMonth = findViewById(R.id.btn_next_month);
    }

    private void setupNavigation() {
        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendarDisplay();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendarDisplay();
        });
    }

    private void updateCalendarDisplay() {
        tvCurrentMonth.setText(monthYearFormat.format(currentCalendar.getTime()));
        populateCalendarGrid();
    }

    private void populateCalendarGrid() {
        calendarGrid.removeAllViews();

        // Add day headers row
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT));
        
        String[] dayHeaders = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : dayHeaders) {
            TextView dayHeader = new TextView(this);
            dayHeader.setText(day);
            dayHeader.setTextColor(getResources().getColor(android.R.color.white));
            dayHeader.setTextSize(14);
            dayHeader.setPadding(8, 8, 8, 8);
            dayHeader.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            dayHeader.setLayoutParams(new LinearLayout.LayoutParams(0, 
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            headerRow.addView(dayHeader);
        }
        calendarGrid.addView(headerRow);

        // Get mood data for current month
        String yearMonth = yearMonthFormat.format(currentCalendar.getTime());
        Map<String, String> moodsByDate = dbHelper.getMoodsByDate(email, yearMonth);

        // Get first day of month and number of days
        Calendar monthStart = (Calendar) currentCalendar.clone();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = monthStart.get(Calendar.DAY_OF_WEEK) - 1; // Sunday = 0
        int daysInMonth = monthStart.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Create calendar rows
        LinearLayout currentRow = null;
        int dayOfWeek = 0;
        
        // Add empty cells for days before the first day of month
        for (int i = 0; i < firstDayOfWeek; i++) {
            if (dayOfWeek == 0) {
                currentRow = createNewRow();
                calendarGrid.addView(currentRow);
            }
            View emptyCell = createEmptyCell();
            currentRow.addView(emptyCell);
            dayOfWeek++;
        }

        // Add days of month
        for (int day = 1; day <= daysInMonth; day++) {
            if (dayOfWeek == 0) {
                currentRow = createNewRow();
                calendarGrid.addView(currentRow);
            }
            
            Calendar dayCalendar = (Calendar) monthStart.clone();
            dayCalendar.set(Calendar.DAY_OF_MONTH, day);
            String dateString = dateFormat.format(dayCalendar.getTime());

            View dayCell = createDayCell(day, moodsByDate.get(dateString));
            currentRow.addView(dayCell);
            
            dayOfWeek = (dayOfWeek + 1) % 7;
        }
        
        // Fill remaining cells in the last row if needed
        if (currentRow != null && dayOfWeek != 0) {
            for (int i = dayOfWeek; i < 7; i++) {
                View emptyCell = createEmptyCell();
                currentRow.addView(emptyCell);
            }
        }
    }

    private LinearLayout createNewRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT));
        return row;
    }

    private View createEmptyCell() {
        View emptyCell = new View(this);
        emptyCell.setLayoutParams(new LinearLayout.LayoutParams(0, 120, 1));
        return emptyCell;
    }

    private View createDayCell(int day, String mood) {
        // Create a simple LinearLayout container for the day cell
        LinearLayout cellContainer = new LinearLayout(this);
        cellContainer.setOrientation(LinearLayout.VERTICAL);
        cellContainer.setGravity(android.view.Gravity.CENTER);
        
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(0, 120, 1);
        containerParams.setMargins(4, 4, 4, 4);
        cellContainer.setLayoutParams(containerParams);
        cellContainer.setBackgroundResource(R.drawable.calendar_day_background);
        cellContainer.setPadding(8, 8, 8, 8);

        // Day number
        TextView dayText = new TextView(this);
        dayText.setText(String.valueOf(day));
        dayText.setTextColor(getResources().getColor(android.R.color.white));
        dayText.setTextSize(16);
        dayText.setGravity(android.view.Gravity.CENTER);
        
        cellContainer.addView(dayText);

        // Mood icon if exists
        if (mood != null) {
            ImageView moodIcon = new ImageView(this);
            moodIcon.setImageResource(getEmojiResId(mood));
            
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(36, 36);
            iconParams.topMargin = 4;
            moodIcon.setLayoutParams(iconParams);
            
            cellContainer.addView(moodIcon);
        }

        return cellContainer;
    }

    private int getEmojiResId(String mood) {
        if (mood == null) return R.drawable.meh;
        switch (mood) {
            case "Excellent!": return R.drawable.yay;
            case "Good!": return R.drawable.nice;
            case "Meh": return R.drawable.meh;
            case "Not great": return R.drawable.eh;
            case "Terrible": return R.drawable.ugh;
            default: return R.drawable.meh;
        }
    }

    private void setupBottomNavigation() {
        ImageButton btnJournal = findViewById(R.id.btn_journal);
        ImageButton btnStats = findViewById(R.id.btn_stats);
        ImageButton btnPlus = findViewById(R.id.btn_plus);
        ImageButton btnMenu = findViewById(R.id.btn_menu);

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
        });

        btnPlus.setOnClickListener(v -> {
            Intent intent = new Intent(this, MoodTrackerActivity.class);
            intent.putExtra("EMAIL", email);
            intent.putExtra("NAME", name);
            startActivity(intent);
        });

        btnMenu.setOnClickListener(v -> showPopupMenu(v));
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
        
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_settings) {
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("NAME", name);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_faq) {
                Intent intent = new Intent(this, FAQActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("NAME", name);
                startActivity(intent);
                return true;
            }
            return false;
        });
        
        popupMenu.show();
    }
}
