package com.s23010494.mentihealth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class JournalEntriesAdapter extends RecyclerView.Adapter<JournalEntriesAdapter.ViewHolder> {
    private final List<JournalEntry> journalEntries;

    public interface OnEntryActionListener {
        void onEdit(JournalEntry entry, int position);
        void onDelete(JournalEntry entry, int position);
    }

    private OnEntryActionListener actionListener;

    public JournalEntriesAdapter(List<JournalEntry> journalEntries) {
        this.journalEntries = journalEntries;
    }

    public void setOnEntryActionListener(OnEntryActionListener listener) {
        this.actionListener = listener;
    }

    public void updateEntries(List<JournalEntry> newEntries) {
        this.journalEntries.clear();
        this.journalEntries.addAll(newEntries);
        notifyDataSetChanged();
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
        holder.ivMoodEmoji.setImageResource(getEmojiResId(entry.getMood()));

        // Setup popup menu
        holder.btnMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), holder.btnMenu);
            popupMenu.getMenuInflater().inflate(R.menu.journal_entry_menu, popupMenu.getMenu());
            
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit) {
                    if (actionListener != null) actionListener.onEdit(entry, position);
                    return true;
                } else if (itemId == R.id.action_delete) {
                    if (actionListener != null) actionListener.onDelete(entry, position);
                    return true;
                }
                return false;
            });
            
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return journalEntries != null ? journalEntries.size() : 0;
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivMoodEmoji;
        final TextView tvDate, tvEntryText;
        final ImageButton btnMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMoodEmoji = itemView.findViewById(R.id.iv_mood_emoji);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvEntryText = itemView.findViewById(R.id.tv_entry_text);
            btnMenu = itemView.findViewById(R.id.btn_menu);
        }
    }
}

