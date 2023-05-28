package com.codz.okah.school_grades.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.codz.okah.school_grades.R;
import com.codz.okah.school_grades.listener.StandardListener;
import com.codz.okah.school_grades.tools.Const;
import com.codz.okah.school_grades.tools.Item;

import java.util.ArrayList;

public class StandardAdapter extends RecyclerView.Adapter<StandardAdapter.StandardHolder> {

    private ArrayList<Item> items;
    private Context context;
    private StandardListener listener;
    private int source;

    public StandardAdapter(ArrayList<Item> items, int source, Context context, StandardListener listener) {
        this.items = items;
        this.context = context;
        this.listener = listener;
        this.source = source;
    }

    @NonNull
    @Override
    public StandardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater =LayoutInflater.from(parent.getContext());
        View item = layoutInflater.inflate(R.layout.item_standard, parent, false);
        return new StandardHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull StandardHolder holder, int position) {

        String formattedId = String.format("%01d", position+1);
        if(items.size()>9) formattedId = String.format("%02d", position=1);
        if(items.size()>99) formattedId = String.format("%03d", position+1);
        if(items.size()>999) formattedId = String.format("%04d", position+1);
        holder.number.setText(formattedId);

        holder.text.setText(items.get(position).getValue());

        if(source== Const.SECTION){
            holder.number_end.setText(String.format("%02d", items.get(position).getNumber()));
            holder.number_end.setVisibility(View.VISIBLE);
        }else holder.number_end.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class StandardHolder extends RecyclerView.ViewHolder{
        TextView text, number, number_end;
        public StandardHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            number = itemView.findViewById(R.id.number);
            number_end = itemView.findViewById(R.id.number_end);

            itemView.findViewById(R.id.drag_item).setOnClickListener(new View.OnClickListener() {
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


    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
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

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }
}
