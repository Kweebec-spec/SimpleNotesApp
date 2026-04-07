package com.example.simplenotesapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplenotesapp.R;
import com.example.simplenotesapp.model.ThemeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер для отображения списка тем (модель ThemeModel).
 */
public class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder> {

    private List<ThemeModel> themes = new ArrayList<>();
    private OnThemeDeleteListener deleteListener;
    private OnThemeEditListener editListener;

    public interface OnThemeDeleteListener {
        void onDelete(ThemeModel theme);
    }

    public interface OnThemeEditListener {
        void onEdit(ThemeModel theme);
    }

    public void setOnThemeDeleteListener(OnThemeDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setOnThemeEditListener(OnThemeEditListener listener) {
        this.editListener = listener;
    }

    public void submitList(List<ThemeModel> newThemes) {
        this.themes = newThemes != null ? newThemes : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_theme, parent, false);
        return new ThemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position) {
        ThemeModel theme = themes.get(position);
        holder.textThemeName.setText(theme.getName());
        holder.textThemeColor.setText(theme.getColor());

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEdit(theme);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(theme);
            }
        });
    }

    @Override
    public int getItemCount() {
        return themes.size();
    }

    static class ThemeViewHolder extends RecyclerView.ViewHolder {
        TextView textThemeName, textThemeColor;
        ImageButton btnEdit, btnDelete;

        public ThemeViewHolder(@NonNull View itemView) {
            super(itemView);
            textThemeName = itemView.findViewById(R.id.textThemeName);
            textThemeColor = itemView.findViewById(R.id.textThemeColor);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}