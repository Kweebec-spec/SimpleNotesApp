package com.example.simplenotesapp.adapters;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplenotesapp.R;
import com.example.simplenotesapp.dataBase.entity.NoteEntity;

import java.text.DateFormat;
import java.util.Date;

public class NotesAdapter extends ListAdapter<NoteEntity, NotesAdapter.NoteViewHolder> {

    private OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onNoteClick(NoteEntity note);
    }

    public void setOnNoteClickListener(OnNoteClickListener listener) {
        this.listener = listener;
    }

    public NotesAdapter() {
        super(DIFF_CALLBACK);
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    private static final DiffUtil.ItemCallback<NoteEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<NoteEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull NoteEntity oldItem, @NonNull NoteEntity newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull NoteEntity oldItem, @NonNull NoteEntity newItem) {
                    // Title
                    String oldTitle = oldItem.getPreviewTitle();
                    String newTitle = newItem.getPreviewTitle();
                    if (!(oldTitle == null ? newTitle == null : oldTitle.equals(newTitle)))
                        return false;

                    // Text content
                    String oldText = oldItem.getText();
                    String newText = newItem.getText();
                    if (!(oldText == null ? newText == null : oldText.equals(newText)))
                        return false;

                    // Color
                    String oldColor = oldItem.getColor();
                    String newColor = newItem.getColor();
                    if (!(oldColor == null ? newColor == null : oldColor.equals(newColor)))
                        return false;

                    // Date
                    if (oldItem.getCreatedAt() != newItem.getCreatedAt())
                        return false;

                    return true;
                }
            };

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteEntity current = getItem(position);
        if (current == null) return;

        // Title
        String title = current.getPreviewTitle();
        holder.noteTitle.setText(title != null && !title.isEmpty() ? title : "Unnamed");

        // Preview text (first 100 chars of note content)
        String text = current.getText();
        String preview = "";
        if (text != null && !text.isEmpty()) {
            preview = text.length() > 100 ? text.substring(0, 100) + "..." : text;
        }
        holder.previewText.setText(preview);

        // Date
        String time = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                .format(new Date(current.getCreatedAt()));
        holder.noteTimeStamp.setText(time);

        // Color
        String color = current.getColor();
        setColor(holder.themeColorView, color != null ? color : "#D3D3D3");

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onNoteClick(getItem(pos));
            }
        });
    }

    private void setColor(View view, String colorHex) {
        if (view == null || colorHex == null) return;
        Drawable backgroundDrawable = view.getBackground();
        if (backgroundDrawable instanceof GradientDrawable) {
            GradientDrawable shape = (GradientDrawable) backgroundDrawable.mutate();
            try {
                shape.setColor(android.graphics.Color.parseColor(colorHex));
            } catch (Exception e) {
                shape.setColor(android.graphics.Color.LTGRAY);
            }
        }
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle, noteTimeStamp, previewText;
        View themeColorView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.noteTitle);
            noteTimeStamp = itemView.findViewById(R.id.noteDate);
            previewText = itemView.findViewById(R.id.noteText);
            themeColorView = itemView.findViewById(R.id.themeColorView);
        }
    }
}