package com.example.newproject;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

enum ButtonsState {
    GONE,
    LEFT_VISIBLE,
    RIGHT_VISIBLE
}

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperListener listener;
    private boolean swipeBack = false;
    private ButtonsState buttonsShowedState = ButtonsState.GONE;
    private static final float BUTTON_WIDTH = 115;
    private RectF leftButtonInstance = null;
    private RectF rightButtonInstance = null;
    private RecyclerView.ViewHolder currentItemViewHolder = null;

    public ItemTouchHelperCallback(ItemTouchHelperListener listener, FragmentActivity activity) {
        this.listener = listener;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return listener.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onItemSwipe(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (buttonsShowedState != ButtonsState.GONE) {
                if (buttonsShowedState == ButtonsState.LEFT_VISIBLE) dX = Math.max(dX, BUTTON_WIDTH);
                if (buttonsShowedState == ButtonsState.RIGHT_VISIBLE) dX = Math.min(dX, -BUTTON_WIDTH);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            } else {
                setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            // Draw buttons after drawing the swiped view
            currentItemViewHolder = viewHolder;
            drawButtons(c, viewHolder);
        }
    }

    private void drawButtons(Canvas c, RecyclerView.ViewHolder viewHolder) {
        float buttonWidthWithOutPadding = BUTTON_WIDTH - 10;
        float corners = 5;
        View itemView = viewHolder.itemView;
        Paint p = new Paint();
        leftButtonInstance = null;
        rightButtonInstance = null;

        if (buttonsShowedState == ButtonsState.RIGHT_VISIBLE) {
            float buttonWidth = (buttonWidthWithOutPadding + 40) * 2;

            RectF hideButton = new RectF(itemView.getRight() - buttonWidth, itemView.getTop() + 10,
                    itemView.getRight() - buttonWidthWithOutPadding - 40, itemView.getBottom() - 10);
            p.setColor(Color.BLUE);
            c.drawRoundRect(hideButton, corners, corners, p);
            drawText("숨김", c, hideButton, p);
            rightButtonInstance = hideButton;

            RectF blockButton = new RectF(itemView.getRight() - buttonWidthWithOutPadding - 40, itemView.getTop() + 10,
                    itemView.getRight() - 10, itemView.getBottom() - 10);
            p.setColor(Color.RED);
            c.drawRoundRect(blockButton, corners, corners, p);
            drawText("차단", c, blockButton, p);
            leftButtonInstance = blockButton;
        }
    }

    private void drawText(String text, Canvas canvas, RectF button, Paint p) {
        float textSize = 50;
        p.setColor(Color.WHITE);
        p.setAntiAlias(true);
        p.setTextSize(textSize);

        float textWidth = p.measureText(text);
        float textHeight = p.descent() - p.ascent();
        float textOffset = (textHeight / 2) - p.descent();

        float x = button.centerX() - textWidth / 2;
        float y = button.centerY() + textOffset;

        canvas.drawText(text, x, y, p);
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (swipeBack) {
            swipeBack = false;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    private void setTouchListener(final Canvas c, final RecyclerView recyclerView,
                                  final RecyclerView.ViewHolder viewHolder,
                                  final float dX, final float dY, final int actionState,
                                  final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
                if (swipeBack) {
                    if (dX < -BUTTON_WIDTH) buttonsShowedState = ButtonsState.RIGHT_VISIBLE;
                    else if (dX > BUTTON_WIDTH) buttonsShowedState = ButtonsState.LEFT_VISIBLE;

                    if (buttonsShowedState != ButtonsState.GONE) {
                        setTouchDownListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        setItemsClickable(recyclerView, false);
                    }
                }
                return false;
            }
        });
    }

    private void setTouchDownListener(final Canvas c, final RecyclerView recyclerView,
                                      final RecyclerView.ViewHolder viewHolder, final float dX,
                                      final float dY, final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    setTouchUpListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
                return false;
            }
        });
    }

    private void setTouchUpListener(final Canvas c, final RecyclerView recyclerView,
                                    final RecyclerView.ViewHolder viewHolder, final float dX,
                                    final float dY, final int actionState, final boolean isCurrentlyActive) {

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ItemTouchHelperCallback.super.onChildDraw(c, recyclerView, viewHolder, 0F, dY, actionState, isCurrentlyActive);
                recyclerView.setOnTouchListener(null);

                setItemsClickable(recyclerView, true);
                swipeBack = false;

                if (listener != null) {
                    if (leftButtonInstance != null && leftButtonInstance.contains(event.getX(), event.getY())) {
                        listener.onBlockClick(viewHolder.getAdapterPosition(), viewHolder);
                    } else if (rightButtonInstance != null && rightButtonInstance.contains(event.getX(), event.getY())) {
                        listener.onHideClick(viewHolder.getAdapterPosition(), viewHolder);
                    }
                }

                // Reset button state and clear current item holder
                buttonsShowedState = ButtonsState.GONE;
                leftButtonInstance = null;
                rightButtonInstance = null;
                currentItemViewHolder = null;

                // Notify the ItemTouchHelper to reset the view to its original position
                recyclerView.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());

                return false;
            }
        });
    }

    private void setItemsClickable(RecyclerView recyclerView, boolean isClickable) {
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            recyclerView.getChildAt(i).setClickable(isClickable);
        }
    }
}
