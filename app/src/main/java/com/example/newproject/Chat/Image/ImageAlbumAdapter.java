package com.example.newproject.Chat.Image;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newproject.R;

import java.util.ArrayList;
import java.util.List;

public class ImageAlbumAdapter extends RecyclerView.Adapter<ImageAlbumAdapter.ImageViewHolder> {
    private ArrayList<Uri> imageUris;
    private Context context;

    private List<Uri> selectedImages = new ArrayList<>();  // 선택된 이미지를 저장하는 리스트

    public ImageAlbumAdapter(ArrayList<Uri> imageUris, Context context) {
        this.imageUris = imageUris;
        this.context = context;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_album, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);

        // 이미지 URI를 ImageView에 로드
        holder.iv_album_im.setImageURI(imageUri);

        // 이미지가 선택된 경우 순서 표시
        if (selectedImages.contains(imageUri)) {
            holder.tv_selected_order.setVisibility(View.VISIBLE);
            int order = selectedImages.indexOf(imageUri) + 1;  // 선택된 순서
            holder.tv_selected_order.setText(String.valueOf(order));
            // 선택된 이미지 배경 추가
            holder.rl_item_image.setBackgroundResource(R.drawable.nonround_background);
        } else {
            holder.tv_selected_order.setVisibility(View.GONE);  // 선택되지 않았으면 숨김
            // 선택되지 않은 이미지 배경 제거
            holder.rl_item_image.setBackground(null);
        }

        // 클릭 이벤트 처리 (선택된 이미지를 전송하거나 다른 작업을 수행)
        holder.itemView.setOnClickListener(v -> {
            if (selectedImages.contains(imageUri)) {
                // 이미 선택된 이미지를 클릭하면 선택 해제
                selectedImages.remove(imageUri);
            } else {
                // 선택되지 않은 이미지를 클릭하면 선택 추가
                selectedImages.add(imageUri);
            }

            // 선택된 이미지 목록을 갱신
            notifyItemChanged(position);
            updateSelectedOrders();
        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }
    // 선택된 이미지 순서 갱신
    private void updateSelectedOrders() {
        for (int i = 0; i < imageUris.size(); i++) {
            Uri imageUri = imageUris.get(i);
            if (selectedImages.contains(imageUri)) {
                notifyItemChanged(i);  // 선택된 이미지의 순서를 업데이트
            }
        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_album_im;
        TextView tv_selected_order;
        RelativeLayout rl_item_image;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_album_im = itemView.findViewById(R.id.iv_album_im);
            tv_selected_order = itemView.findViewById(R.id.tv_selected_order);  // 순서 표시용 TextView
            rl_item_image = itemView.findViewById(R.id.rl_item_image);
        }
    }
}

