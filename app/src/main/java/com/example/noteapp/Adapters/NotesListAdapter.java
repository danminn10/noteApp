package com.example.noteapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noteapp.Models.Notes;
import com.example.noteapp.NotesClickListener;
import com.example.noteapp.R;
import java.util.Arrays;
import java.util.List;

public class NotesListAdapter extends RecyclerView.Adapter<NotesViewHolder> {
    Context context;
    List<Notes> list;
    NotesClickListener listener;


    public NotesListAdapter(Context context, List<Notes> list, NotesClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }
    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_list, parent, false);
        return new NotesViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        Notes note = list.get(position);
        holder.textView_title.setText(list.get(position).getTitle());
        holder.textView_notes.setText(list.get(position).getNotes());
        holder.textView_date.setText(list.get(position).getDate());

        // Kích hoạt marquee cho tiêu đề và thời gian
        holder.textView_title.setSelected(true);
        holder.textView_date.setSelected(true);
//        // Hiển thị biểu tượng ghim nếu ghi chú được pin
        if (note.isPinned()) {
            holder.imageView_pin.setImageResource(R.drawable.ic_pin);
        } else {
            holder.imageView_pin.setImageResource(0); // Ẩn biểu tượng ghim
        }

        int color_code = getRandomColor();
        holder.notes_container.setCardBackgroundColor(
                holder.itemView.getResources().getColor(color_code, null)
        );
        holder.notes_container.setOnClickListener(v -> {
            listener.onClick(list.get(holder.getAdapterPosition()));
        });
        holder.notes_container.setOnLongClickListener(v -> {
            listener.onLongClick(list.get(holder.getAdapterPosition()), holder.notes_container);
            return true;
        });
    }

    private int getRandomColor(){
        List<Integer> colorCode = Arrays.asList(
                R.color.color1, R.color.color2, R.color.color3, R.color.color4, R.color.color5,
                R.color.color6, R.color.color7, R.color.color8, R.color.color9, R.color.color10,
                R.color.color11, R.color.color12, R.color.color13, R.color.color14, R.color.color15,
                R.color.color16
        );
        return colorCode.get((int) (Math.random() * colorCode.size()));
    }

    public void updateList(List<Notes> newNotes) {
        list.clear();
        list.addAll(newNotes);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


}
class NotesViewHolder extends RecyclerView.ViewHolder {

    CardView notes_container;
    TextView textView_title, textView_notes, textView_date;
    ImageView imageView_pin;
    @SuppressLint("WrongViewCast")
    public NotesViewHolder(@NonNull View itemView) {
        super(itemView);
        notes_container = itemView.findViewById(R.id.notes_container);
        textView_title = itemView.findViewById(R.id.textView_title);
        textView_notes = itemView.findViewById(R.id.textView_notes);
        textView_date = itemView.findViewById(R.id.textView_date);
        imageView_pin = itemView.findViewById(R.id.imageView_pin);
    }
}