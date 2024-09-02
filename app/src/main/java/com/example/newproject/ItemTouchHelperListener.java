package com.example.newproject;

import androidx.recyclerview.widget.RecyclerView;

public interface ItemTouchHelperListener {
    boolean onItemMove(int from_position, int to_position);

    void onItemSwipe(int position);

    //왼쪽 버튼 누르면 수정할 다이얼로그 띄우기
    void onBlockClick(int position, RecyclerView.ViewHolder viewHolder);

    void onHideClick(int position, RecyclerView.ViewHolder viewHolder);
}