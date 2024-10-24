package com.example.newproject.Chat.Drawer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newproject.R;

import java.util.List;

public class DrawerImagesAdapter extends RecyclerView.Adapter<DrawerImagesAdapter.DrawerImagesViewHolder> {

    private List<Drawer_Images> Images;

    public DrawerImagesAdapter(List<Drawer_Images> Images) {
        this.Images = Images;
    }

    @NonNull
    @Override
    public DrawerImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drawer_album, parent, false);
        return new DrawerImagesAdapter.DrawerImagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrawerImagesAdapter.DrawerImagesViewHolder holder, int position) {
        Drawer_Images currentImage = Images.get(position);
        String imagePath = currentImage.getImage_path(); // 이미지 경로 가져오기

        // 서버 경로를 웹 URL로 변환
        String imageUrl = "http://49.247.32.169" + imagePath.replace("/var/www/html", "");
        // 이미지 로딩 라이브러리 사용 예시 (Glide)
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .into(holder.iv_drawer_im);

    }



    @Override
    public int getItemCount() {
        return  Images.size();
    }

    public class DrawerImagesViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_drawer_im;
        public DrawerImagesViewHolder(@NonNull View itemView) {
            super(itemView);

            iv_drawer_im = itemView.findViewById(R.id.iv_drawer_im);
        }
    }
}
