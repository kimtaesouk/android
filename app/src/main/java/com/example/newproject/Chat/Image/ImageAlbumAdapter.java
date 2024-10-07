package com.example.newproject.Chat.Image;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newproject.Chat.ChattingActivity;
import com.example.newproject.R;

import java.util.ArrayList;
import java.util.List;

public class ImageAlbumAdapter extends RecyclerView.Adapter<ImageAlbumAdapter.ImageViewHolder> {
    private ArrayList<Uri> imageUris;
    private Context context;
    private List<Uri> selectedImages = new ArrayList<>();
    private ChattingActivity listener;  // 리스너 선언

    // 리스너를 생성자에 추가
    public ImageAlbumAdapter(ArrayList<Uri> imageUris, Context context, ChattingActivity listener) {
        this.imageUris = imageUris;
        this.context = context;
        this.listener = listener;  // 리스너 초기화
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

        // 이미지 설정
        holder.iv_album_im.setImageURI(imageUri);

        // 선택된 이미지를 표시하고 선택된 순서를 보여줌
        if (selectedImages.contains(imageUri)) {
            holder.tv_selected_order.setVisibility(View.VISIBLE);
            int order = selectedImages.indexOf(imageUri) + 1;
            holder.tv_selected_order.setText(String.valueOf(order));
            holder.rl_item_image.setBackgroundResource(R.drawable.nonround_background);
        } else {
            holder.tv_selected_order.setVisibility(View.GONE);
            holder.rl_item_image.setBackground(null);
        }

        // 클릭 이벤트 처리
        holder.itemView.setOnClickListener(v -> {
            // 선택된 이미지를 클릭하면 리스트에서 제거
            if (selectedImages.contains(imageUri)) {
                selectedImages.remove(imageUri);
            } else {
                // 선택된 이미지 리스트에 추가
                selectedImages.add(imageUri);
            }
            // 아이템 변화 반영
            notifyItemChanged(position);

            // 선택된 이미지 순서 업데이트
            updateSelectedImageOrder();

            // 클릭된 이미지 URI 리스트를 리스너에 전달
            listener.onImageClick(new ArrayList<>(selectedImages));
        });
    }
    public void clearSelectedImages() {
        selectedImages.clear(); // 선택된 이미지 리스트 초기화
        notifyDataSetChanged();  // 어댑터 갱신
    }

    // 선택된 이미지의 순서를 다시 갱신하는 메서드
    private void updateSelectedImageOrder() {
        for (int i = 0; i < imageUris.size(); i++) {
            Uri imageUri = imageUris.get(i);
            if (selectedImages.contains(imageUri)) {
                notifyItemChanged(i);
            }
        }
    }


    @Override
    public int getItemCount() {
        return imageUris.size();
    }


    public interface OnImageClickListener {
        void onImageClick(ArrayList<Uri> selectedImages);  // 클릭된 이미지 URI들의 리스트를 전달
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_album_im;
        TextView tv_selected_order;
        RelativeLayout rl_item_image;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_album_im = itemView.findViewById(R.id.iv_album_im);
            tv_selected_order = itemView.findViewById(R.id.tv_selected_order);
            rl_item_image = itemView.findViewById(R.id.rl_item_image);
        }
    }
}


