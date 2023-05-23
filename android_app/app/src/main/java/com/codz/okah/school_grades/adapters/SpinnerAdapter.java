package com.codz.okah.school_grades.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.codz.okah.school_grades.R;

public class SpinnerAdapter extends ArrayAdapter {

    private String[] objects;
    private Context context;

    public SpinnerAdapter(@NonNull Context context, int resource, @NonNull String[] objects) {
        super(context, resource, objects);
        this.objects = objects;
        this.context = context;
    }




    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        // modify the holder here
        ViewHolder holder;
        if (view.getTag() == null) {
            holder = new ViewHolder();
            holder.textView = view.findViewById(android.R.id.text1);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // set the text for the item
        String item = objects[position];
        holder.textView.setText(item);

        if(position==0){
            holder.textView.setTextColor(context.getResources().getColor(R.color.hint_grey));
        }else{
            holder.textView.setTextColor(context.getResources().getColor(R.color.dark_grey));
        }



        return view;
    }

    private static class ViewHolder {
        TextView textView;
    }
}
