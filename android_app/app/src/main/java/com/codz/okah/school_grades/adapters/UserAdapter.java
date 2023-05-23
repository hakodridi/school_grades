package com.codz.okah.school_grades.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codz.okah.school_grades.R;
import com.codz.okah.school_grades.listener.StandardListener;
import com.codz.okah.school_grades.tools.User;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    private ArrayList<User> items;
    private Context context;
    private StandardListener listener;

    public UserAdapter(ArrayList<User> items, Context context, StandardListener listener) {
        this.items = items;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater =LayoutInflater.from(parent.getContext());
        View item = layoutInflater.inflate(R.layout.item_standard, parent, false);
        return new UserHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {

        String formattedId = String.format("%01d", position+1);
        if(items.size()>9) formattedId = String.format("%02d", position=1);
        if(items.size()>99) formattedId = String.format("%03d", position+1);
        if(items.size()>999) formattedId = String.format("%04d", position+1);
        holder.number.setText(formattedId);

        holder.text.setText(items.get(position).getFullName());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class UserHolder extends RecyclerView.ViewHolder{
        TextView text, number, number_end;
        public UserHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            number = itemView.findViewById(R.id.number);
            number_end = itemView.findViewById(R.id.number_end);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(getAdapterPosition());
                }
            });

            itemView.findViewById(R.id.edit_item).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onEdit(getAdapterPosition());
                }
            });

            itemView.findViewById(R.id.delete_item).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDelete(getAdapterPosition());
                }
            });


        }
    }


    public ArrayList<User> getItems() {
        return items;
    }

    public void setItems(ArrayList<User> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public StandardListener getListener() {
        return listener;
    }

    public void setListener(StandardListener listener) {
        this.listener = listener;
    }

}

