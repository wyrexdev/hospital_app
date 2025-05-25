package com.hospital.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.ViewHolder> {

    private List<Item> itemList;

    public RoomsAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView titleTextView;

        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.item_image);
            titleTextView = view.findViewById(R.id.item_title);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item currentItem = itemList.get(position);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}

class Item {
    private String username;
    private String ppUrl;
    private String date;
    private String lastMessage;

    public Item(String title, int imageResource) {
        this.username = username;
        this.ppUrl = ppUrl;
        this.date = date;
        this.lastMessage = lastMessage;
    }

    public String getUsername() {
        return username;
    }

    public String getPpUrl() {
        return ppUrl;
    }

    public String getDate() {
        return date;
    }

    public String getLastMessage() {
        return lastMessage;
    }
}

