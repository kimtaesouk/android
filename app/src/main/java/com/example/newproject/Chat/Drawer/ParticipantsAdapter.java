package com.example.newproject.Chat.Drawer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newproject.R;

import java.util.List;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ParticipantsViewHolder>{
    private List<Participants> userList;

    public ParticipantsAdapter(List<Participants> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ParticipantsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_list, parent, false);
        return new ParticipantsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantsViewHolder holder, int position) {
        Participants user = userList.get(position);
        holder.tv_list_fdname.setText(user.getName());
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class ParticipantsViewHolder extends RecyclerView.ViewHolder {
        TextView tv_list_fdname;

        ParticipantsViewHolder(View itemView) {
            super(itemView);
            tv_list_fdname = itemView.findViewById(R.id.tv_list_fdname);

        }
    }
}

