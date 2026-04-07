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
import com.example.simplenotesapp.model.Note;
import com.example.simplenotesapp.utils.DateUtils;

/**
 * Адаптер для отображения списка заметок в RecyclerView.
 * Работает с моделью Note (не с Entity) и использует DiffUtil для эффективного обновления.
 */
public class NotesAdapter extends ListAdapter<Note, NotesAdapter.NoteViewHolder> {

    private static final int MAX_PREVIEW_LENGTH = 250; // Максимальная длина предпросмотра текста

    private OnNoteLongClickListener longClickListener;
    private OnNoteClickListener noteClickListener;       // Обработчик клика по всей заметке
    private OnThemeClickListener themeClickListener;   // Обработчик клика по кружку цвета (выбор темы)


    public interface OnNoteLongClickListener {
        void onNoteLongClick(Note note, int position);
    }
    /**
     * Интерфейс для обработки клика по заметке.
     */
    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    /**
     * Интерфейс для обработки клика по цветному кружку (выбор темы).
     // @param note Заметка, к которой относится клик
     // @param anchor View, на который был клик (кружок)
     */
    public interface OnThemeClickListener {
        void onThemeClick(Note note, View anchor);
    }

    public void setOnNoteLongClickListener(OnNoteLongClickListener listener) {
        this.longClickListener = listener;
    }
    /**
     * Установить слушатель клика по заметке.
     */
    public void setOnNoteClickListener(OnNoteClickListener listener) {
        this.noteClickListener = listener;
    }

    /**
     * Установить слушатель клика по цветному кружку.
     */
    public void setOnThemeClickListener(OnThemeClickListener listener) {
        this.themeClickListener = listener;
    }

    /**
     * Конструктор адаптера. Передаёт колбэк DiffUtil в родительский класс.
     */
    public NotesAdapter() {
        super(DIFF_CALLBACK);
        setHasStableIds(true); // ID заметок стабильны (берутся из базы)
    }

    /**
     * Возвращает стабильный ID элемента для позиции.
     * Используется для анимаций и сохранения состояния.
     */
    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    /**
     * DiffUtil.Callback для эффективного сравнения старого и нового списка заметок.
     * Позволяет RecyclerView обновляться только для изменённых элементов.
     */
    private static final DiffUtil.ItemCallback<Note> DIFF_CALLBACK = new DiffUtil.ItemCallback<Note>() {
        /**
         * Проверяет, представляют ли два объекта одну и ту же заметку (сравниваем по ID).
         */
        @Override
        public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.getId() == newItem.getId();
        }

        /**
         * Проверяет, изменилось ли содержимое заметки (если ID одинаковы).
         * Если содержимое не изменилось, RecyclerView не будет перерисовывать элемент.
         */
        @Override
        public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            // Сравниваем заголовок (с учётом null)
            String oldTitle = oldItem.getTitle();
            String newTitle = newItem.getTitle();
            if (oldTitle == null && newTitle == null) {
                // оба null – считаем равными
            } else if (oldTitle == null || newTitle == null) {
                return false; // один null, другой нет
            } else if (!oldTitle.equals(newTitle)) {
                return false;
            }

            // Сравниваем содержимое (текст заметки)
            String oldText = oldItem.getContent();
            String newText = newItem.getContent();
            if (oldText == null && newText == null) {
                // оба null
            } else if (oldText == null || newText == null) {
                return false;
            } else if (!oldText.equals(newText)) {
                return false;
            }

            // Сравниваем цвет (может быть null, если тема не выбрана)
            String oldColor = oldItem.getColor();
            String newColor = newItem.getColor();
            if (oldColor == null && newColor == null) {
                // оба null
            } else if (oldColor == null || newColor == null) {
                return false;
            } else if (!oldColor.equals(newColor)) {
                return false;
            }

            // Сравниваем временную метку
            return oldItem.getTimeStamp() == newItem.getTimeStamp();
        }
    };

    /**
     * Создаёт новый ViewHolder, раздувая макет элемента заметки.
     */
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    /**
     * Связывает данные заметки (модель Note) с элементами ViewHolder.
     * @param holder   ViewHolder, содержащий ссылки на views
     * @param position Позиция элемента в списке
     */
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note current = getItem(position);
        if (current == null) return;

        // Установка заголовка: если пустой, показываем "Unnamed"
        String title = current.getTitle();
        holder.noteTitle.setText(title != null && !title.isEmpty() ? title : "Unnamed");

        // Установка текста предпросмотра (обрезаем до MAX_PREVIEW_LENGTH символов)
        String text = current.getContent();
        if (text != null && !text.isEmpty()) {
            holder.previewText.setVisibility(View.VISIBLE);
            if (text.length() > MAX_PREVIEW_LENGTH) {
                holder.previewText.setText(text.substring(0, MAX_PREVIEW_LENGTH) + "...");
            } else {
                holder.previewText.setText(text);
            }
        } else {
            holder.previewText.setText("");
            holder.previewText.setVisibility(View.GONE);
        }

        // Форматирование временной метки в читаемый вид
        try {
            String time = DateUtils.formatTimestamp(current.getTimeStamp());
            holder.noteTimeStamp.setText(time);
        } catch (Exception e) {
            holder.noteTimeStamp.setText("");
        }

        // Установка цвета фона цветного кружка (тема)
        String color = current.getColor();
        setColor(holder.themeColorView, color != null ? color : "#00000000");

        // long press triggers delete dialog
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onNoteLongClick(current, position);
            }
            return true; // true = event consumed, prevents short-click firing too
        });

        // Обработчик клика по всей заметке
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && noteClickListener != null) {
                noteClickListener.onNoteClick(getItem(pos));
            }
        });

        // Обработчик клика по цветному кружку (для смены темы)
        holder.themeColorView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && themeClickListener != null) {
                themeClickListener.onThemeClick(getItem(pos), holder.themeColorView);
            }
        });
    }

    /**
     * Вспомогательный метод для установки цвета фона у view с типом GradientDrawable.
     * @param view     View, чей фон нужно изменить (обычно цветной кружок)
     * @param colorHex Цвет в формате HEX (#RRGGBB или #AARRGGBB)
     */
    private void setColor(View view, String colorHex) {
        if (view == null || colorHex == null) return;
        Drawable backgroundDrawable = view.getBackground();
        if (backgroundDrawable instanceof GradientDrawable) {
            GradientDrawable shape = (GradientDrawable) backgroundDrawable.mutate();
            try {
                shape.setColor(android.graphics.Color.parseColor(colorHex));
            } catch (Exception e) {
                shape.setColor(android.graphics.Color.parseColor("#00000000"));
            }
        }
    }

    /**
     * ViewHolder для элемента заметки. Содержит ссылки на все views в item_note.xml.
     */
    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle;        // Заголовок заметки
        TextView noteTimeStamp;    // Дата/время создания
        TextView previewText;      // Предпросмотр текста
        View themeColorView;       // Цветной кружок (индикатор темы)

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.noteTitle);
            noteTimeStamp = itemView.findViewById(R.id.noteDate);
            previewText = itemView.findViewById(R.id.noteText);
            themeColorView = itemView.findViewById(R.id.themeColorView);

        }
    }
}