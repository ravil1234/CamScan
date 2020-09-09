package com.example.camscan.Callbacks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.camscan.Adapters.InDocMiniAdapter;

public class ItemMoveCallback extends ItemTouchHelper.Callback {

    ItemTouchHelperContract mAdapter;

    public ItemMoveCallback(ItemTouchHelperContract c){
        mAdapter=c;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags=ItemTouchHelper.RIGHT|ItemTouchHelper.LEFT;
        return makeMovementFlags(dragFlags,0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        mAdapter.onRowMoved(viewHolder.getAdapterPosition(),target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        if(actionState!=ItemTouchHelper.ACTION_STATE_IDLE){
            if(viewHolder instanceof InDocMiniAdapter.MyViewHolder){
                InDocMiniAdapter.MyViewHolder myViewHolder=(InDocMiniAdapter.MyViewHolder)viewHolder;
                mAdapter.onRowSelected(myViewHolder);
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if(viewHolder instanceof InDocMiniAdapter.MyViewHolder){
            InDocMiniAdapter.MyViewHolder myViewHolder=(InDocMiniAdapter.MyViewHolder)viewHolder;
            mAdapter.onRowClear(myViewHolder);
        }
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    public interface ItemTouchHelperContract {

        void onRowMoved(int fromPosition, int toPosition);
        void onRowSelected(InDocMiniAdapter.MyViewHolder myViewHolder);
        void onRowClear(InDocMiniAdapter.MyViewHolder myViewHolder);

    }

}
