package com.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;

import com.balram.library.FotTextView;
import com.github.ornolfr.ratingview.RatingView;
import com.tam.winati.ksa.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    List<JSONObject> items;
    Context con;

    public ReviewAdapter(Context con, List<JSONObject> items) {
        this.items = items;
        this.con = con;
    }

    @Override
    public ReviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_reviews,parent,false);
        return new ReviewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewAdapter.ViewHolder holder, int position) {
        try {
            holder.text.setText(items.get(position).getString("txt"));
            holder.Date.setText(items.get(position).getString("dated"));
            holder.Name.setText(items.get(position).getString("username"));
            holder.rating.setRating(Float.parseFloat(items.get(position).getString("rating")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        RatingView rating;
        FotTextView Name, Date, text;

        public ViewHolder(View itemView) {
            super(itemView);
            rating = itemView.findViewById(R.id.ratingReviews);
            Name = itemView.findViewById(R.id.ratingName);
            Date = itemView.findViewById(R.id.ratingDate);
            text = itemView.findViewById(R.id.ratingText);
        }

    }
}
