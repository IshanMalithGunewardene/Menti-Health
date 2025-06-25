package com.s23010494.mentihealth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class JournalEntriesAdapter extends RecyclerView.Adapter<JournalEntriesAdapter.ViewHolder> {
    private final List<JournalEntry> journalEntries;

    public JournalEntriesAdapter(List<JournalEntry> journalEntries) {
        this.journalEntries = journalEntries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_journal_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JournalEntry entry = journalEntries.get(position);
        
        holder.tvDate.setText(entry.getDate());
        holder.tvEntryText.setText(entry.getText());
        
        String moodEmoji = getMoodEmoji(entry.getMood());
        holder.tvMoodEmoji.setText(moodEmoji);
    }

    @Override
    public int getItemCount() {
        return journalEntries != null ? journalEntries.size() : 0;
    }

    private String getMoodEmoji(String mood) {
        if (mood == null) return "😐";
        
        switch (mood) {
            case "Excellent!": return "😁";
            case "Good!": return "😊";
            case "Meh": return "😐";
            case "Not great": return "😔";
            case "Terrible": return "😢";
            default: return "😐";
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMoodEmoji, tvDate, tvEntryText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMoodEmoji = itemView.findViewById(R.id.tv_mood_emoji);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvEntryText = itemView.findViewById(R.id.tv_entry_text);
        }
    }
}

