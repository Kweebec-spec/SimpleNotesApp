package com.example.simplenotesapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplenotesapp.R;
import com.example.simplenotesapp.dataBase.pojo.PreviewNoteWithItemsThemes;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class previewNotesAdapter extends RecyclerView.Adapter<previewNotesAdapter.NoteViewHolder> {

    private List<PreviewNoteWithItemsThemes> notes = new ArrayList<>();


    public void updateList(List<PreviewNoteWithItemsThemes> newNotes) {
        if (newNotes == null) return; // Защита от null
        this.notes = newNotes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        PreviewNoteWithItemsThemes current = notes.get(position);

        // Устанавливаем заголовок (готовый из SQL)
        holder.noteTitle.setText(current.displayTitle);

        // Устанавливаем текст превью (уже со всеми галочками из SQL)
        holder.previewText.setText(current.previewText);

        // Дата (форматируем в Java)
        String time = DateFormat
                .getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                .format(new Date(current.note.createdAt));
        holder.noteTimeStamp.setText(time);

        try {
            int colorInt = android.graphics.Color.parseColor(current.displayColor);
            holder.top_note_section.setBackgroundColor(colorInt);
        } catch (Exception e) {
            // На случай, если в базе оказался текст, который нельзя превратить в цвет
            holder.top_note_section.setBackgroundColor(android.graphics.Color.LTGRAY);
        }

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView noteTitle;
        TextView noteTimeStamp;
        TextView previewText;
        View themeColorView;
        LinearLayout top_note_section;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            noteTitle = itemView.findViewById(R.id.noteTitle);
            noteTimeStamp = itemView.findViewById(R.id.noteDate);
            previewText = itemView.findViewById(R.id.noteText);
            themeColorView = itemView.findViewById(R.id.themeColorView);
            top_note_section = itemView.findViewById(R.id.top_note_section);
        }
    }
}