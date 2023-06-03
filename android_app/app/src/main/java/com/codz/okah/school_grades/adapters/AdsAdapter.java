package com.codz.okah.school_grades.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codz.okah.school_grades.R;
import com.codz.okah.school_grades.tools.Ad;

import java.util.ArrayList;

public class AdsAdapter extends RecyclerView.Adapter<AdsAdapter.AdsHolder> {
    private ArrayList<Ad> ads;

    public AdsAdapter(ArrayList<Ad> ads) {
        this.ads = ads;
    }

    @NonNull
    @Override
    public AdsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater =LayoutInflater.from(parent.getContext());
        View item = layoutInflater.inflate(R.layout.item_ad, parent, false);
        return new AdsHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull AdsHolder holder, int position) {
        holder.text.setText(ads.get(position).getText());
        holder.date.setText(ads.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        return ads.size();
    }

    public class AdsHolder extends RecyclerView.ViewHolder{
        TextView text, date;
        public AdsHolder(@NonNull View itemView) {
            super(itemView);

            text = itemView.findViewById(R.id.text);
            date = itemView.findViewById(R.id.date);
        }
    }

    public ArrayList<Ad> getAds() {
        return ads;
    }

    public void setAds(ArrayList<Ad> ads) {
        this.ads = ads;
        notifyDataSetChanged();
    }
}
