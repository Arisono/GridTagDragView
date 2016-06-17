/*
 * Copyright 2015 - 2016 solartisan/imilk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wenhuaijun.easytagdragview.adapter;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;

import com.wenhuaijun.easytagdragview.widget.DragDropGirdView;
import com.wenhuaijun.easytagdragview.bean.Tip;
import com.wenhuaijun.easytagdragview.listener.OnDragDropListener;
import com.wenhuaijun.easytagdragview.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Also allows for a configurable number of columns as well as a maximum row of tiled view.
 */
public abstract class AbsTipAdapter extends BaseAdapter implements
        OnDragDropListener {


    private static final String TAG = "AbsTipAdapter";

    protected DragDropListener mDragDropListener;

    protected Context mContext;

    private List<Boolean> mTextSelected = new ArrayList<Boolean>(); // 定义一个向量作为选中与否容器
    /**
     * Contact data stored in cache. This is used to populate the associated view.
     */
    protected ArrayList<Tip> tips = null;
    /**
     * Back up of（备份） the temporarily removed Contact during dragging.
     */
    private Tip tempTip = null;
    /**
     * Position of the temporarily removed contact in the cache.
     */
    private int tempTipIndex = -1;
    /**
     * New position of the temporarily removed contact in the cache.
     */
    private int newTipIndex = -1;
    /**
     * New position of the temporarily entered contact in the cache.
     */
    private int mDragEnteredTipIndex = -1;

    private boolean mAwaitingRemove = false;
    private boolean mDelayCursorUpdates = false;

    private int mAnimationDuration;

    /**
     * Indicates whether a drag is in process.
     */
    private boolean mInDragging = false;

    private int mTilesStartLimit = -1;

    private int mTilesEndLimit = Integer.MAX_VALUE;

    private final HashMap<Long, Integer> mItemIdTopMap = new HashMap<Long, Integer>();
    private final HashMap<Long, Integer> mItemIdLeftMap = new HashMap<Long, Integer>();


    public static Tip BLANK_ENTRY = new Tip() {
        private int id;

        @Override
        public int getId() {
            return id;
        }

        @Override
        public void setId(int id) {
            this.id = id;
        }
    };

    public interface DragDropListener {

        DragDropGirdView getDragDropGirdView();

        void onDataSetChangedForResult(ArrayList<Tip> list);
    }


    public AbsTipAdapter(Context context,
                         DragDropListener dragDropListener) {
        mDragDropListener = dragDropListener;
        mContext = context;
        tips = new ArrayList<Tip>();
        mAnimationDuration = context.getResources().getInteger(R.integer.fade_duration);
    }

    /**
     * thumbtack some view,start
     * @param startLimit
     */
    protected void setTilesStartLimit(int startLimit){
        mTilesStartLimit = startLimit;
    }

    public int getTilesStartLimit() {
        return mTilesStartLimit;
    }

    /**
     * thumbtack some view,end
     * @param endLimit
     */
    protected void setTilesEndLimit(int endLimit){
        mTilesEndLimit = endLimit;
    }


    public int getTilesEndLimit() {
        return mTilesEndLimit;
    }
    /**
     * Indicates whether a drag is in process.
     *
     * @param inDragging Boolean variable indicating whether there is a drag in process.
     */
    public void setInDragging(boolean inDragging) {
        mDelayCursorUpdates = inDragging;
        mInDragging = inDragging;
    }


    //当数据改变时调用
    public void setData(List<Tip> tips) {
        if (!mDelayCursorUpdates && tips != null) {
            this.tips.clear();
            this.tips.addAll(tips);
            for (int i=0;i<tips.size();i++){
                mTextSelected.add(i,false);
            }
            // cause a refreshData of any views that rely on this data
            notifyDataSetChanged();
            // about to start redraw
            onDataSetChangedForAnimation();
        }
    }


    @Override
    public int getCount() {
        if (tips == null) {
            return 0;
        }

        return tips.size();
    }

    /**
     * Returns an ArrayList of the objects that are to appear
     * on the row for the given position.
     */
    @Override
    public Tip getItem(int position) {
        return tips.get(position);
    }

    /**
     * For the top row of tiled contacts, the item id is the position of the row of
     * contacts.
     * For frequent contacts, the item id is the maximum number of rows of tiled contacts +
     * the actual contact id. Since contact ids are always greater than 0, this guarantees that
     * all items within this adapter will always have unique ids.
     */
    @Override
    public long getItemId(int position) {
        return tips.get(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return getCount() > 0;
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    @Override
    public int getViewTypeCount() {
        return ViewTypes.COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return ViewTypes.TILE;
    }

    /**
     * Temporarily(暂时的) removes a contact(接触) from the list for UI refreshData. Stores data for this contact
     * in the back-up variable.
     *
     * @param index Position of the contact to be removed.
     */
    public void popDragEntry(int index) {
        if (isIndexInBound(index)) {
            //备份一份被拖动的数据
            tempTip = tips.get(index);
            tempTipIndex = index;
            mDragEnteredTipIndex = index;
            markDropArea(index);
        }
    }

    /**
     * @param itemIndex Position of the contact in {@link #tips}.
     * @return True if the given index is valid for {@link #tips}.
     */
    public boolean isIndexInBound(int itemIndex) {
        return itemIndex >= 0 && itemIndex < tips.size();
    }

    /**
     * Mark the tile(瓦片) as drop area by given the item index in {@link #tips}.
     *
     * @param itemIndex Position of the contact in {@link #tips}.
     */
    private void markDropArea(int itemIndex) {
        if (tempTip != null && isIndexInBound(mDragEnteredTipIndex) &&
                isIndexInBound(itemIndex)) {
            cacheOffsetsForDataSetChange();
            // Remove the old placeholder item and place the new placeholder item.
            final int oldIndex = mDragEnteredTipIndex;
            tips.remove(mDragEnteredTipIndex);
            mDragEnteredTipIndex = itemIndex;
            tips.add(mDragEnteredTipIndex, BLANK_ENTRY);
            BLANK_ENTRY.setId(tempTip.getId());
            //启动动画
            onDataSetChangedForAnimation();
            notifyDataSetChanged();
        }
    }

    /**
     * Drops the temporarily removed contact to the desired location in the list.
     */
    public void handleDrop() {
        if (tempTip != null) {
            if (isIndexInBound(mDragEnteredTipIndex) &&
                    mDragEnteredTipIndex != tempTipIndex) {
                newTipIndex = mDragEnteredTipIndex;
                tips.set(newTipIndex, tempTip);
                cacheOffsetsForDataSetChange();
                notifyDataSetChanged();
            } else if (isIndexInBound(tempTipIndex)) {
                tips.remove(mDragEnteredTipIndex);
                tips.add(tempTipIndex, tempTip);
                newTipIndex = tempTipIndex;
                notifyDataSetChanged();
            }
            tempTip = null;

            if (tempTipIndex != mDragEnteredTipIndex) {
                mDragDropListener.onDataSetChangedForResult(tips);
            }

        }
    }


    protected static class ViewTypes {
        public static final int TILE = 0;
        public static final int COUNT = 1;
    }

    /**
     * @param view
     * @return
     */
    protected abstract Tip getDragEntity(View view);

    @Override
    public void onDragStarted(int x, int y, View view) {
        setInDragging(true);
        final int itemIndex = tips.indexOf(getDragEntity(view));
        popDragEntry(itemIndex);
    }

    @Override
    public void onDragHovered(int x, int y, View view) {
        if (view == null) {
            return;
        }
        final int itemIndex = tips.indexOf(getDragEntity(view));
        if (mInDragging && mDragEnteredTipIndex != itemIndex
                && isIndexInBound(itemIndex) && itemIndex > mTilesStartLimit &&  itemIndex < mTilesEndLimit) {
            markDropArea(itemIndex);
        }
    }

    @Override
    public void onDragFinished(int x, int y) {
        setInDragging(false);
        if (!mAwaitingRemove) {
            handleDrop();
        }
    }

    @Override
    public void onDroppedOnRemove() {
        if (tempTip != null) {
            //TODO remove op
            mAwaitingRemove = true;
        }
    }

    public void onDataSetChangedForAnimation(long... idsInPlace) {
        animateGridView(idsInPlace);
    }

    public void cacheOffsetsForDataSetChange() {
        saveOffsets();
    }


    /**
     * Performs animations for the gridView
     */
    private void animateGridView(final long... idsInPlace) {

        if (mItemIdTopMap.isEmpty()) {
            return;
        }

        final ViewTreeObserver observer = mDragDropListener.getDragDropGirdView().getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @SuppressWarnings("unchecked")
            @Override
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                final int firstVisiblePosition = mDragDropListener.getDragDropGirdView().getFirstVisiblePosition();

                final AnimatorSet animSet = new AnimatorSet();
                final ArrayList<Animator> animators = new ArrayList<Animator>();
                for (int i = 0; i < mDragDropListener.getDragDropGirdView().getChildCount(); i++) {
                    final View child = mDragDropListener.getDragDropGirdView().getChildAt(i);
                    int position = firstVisiblePosition + i;

                    if (!isIndexInBound(position)) {
                        continue;
                    }

                    final long itemId = getItemId(position);

                    if (containsId(idsInPlace, itemId)) {
                        animators.add(ObjectAnimator.ofFloat(
                                child, "alpha", 0.0f, 1.0f));
                        break;
                    } else {
                        Integer startTop = mItemIdTopMap.get(itemId);
                        Integer startLeft = mItemIdLeftMap.get(itemId);
                        final int top = child.getTop();
                        final int left = child.getLeft();
                        int deltaX = 0;
                        int deltaY = 0;

                        if (startLeft != null) {
                            if (startLeft != left) {
                                deltaX = startLeft - left;
                                animators.add(ObjectAnimator.ofFloat(
                                        child, "translationX", deltaX, 0.0f));
                            }
                        }

                        if (startTop != null) {
                            if (startTop != top) {
                                deltaY = startTop - top;
                                animators.add(ObjectAnimator.ofFloat(
                                        child, "translationY", deltaY, 0.0f));
                            }
                        }

//                        Log.d(TAG, "Found itemId: " + itemId + " for listview child " + i +
//                                " Top: " + top +
//                                " deltaY: " + deltaY +
//                                " Left " + left +
//                                " deltaX: " + deltaX);
                    }
                }

                if (animators.size() > 0) {
                    animSet.setDuration(mAnimationDuration).playTogether(animators);
                    animSet.start();
                }

                mItemIdTopMap.clear();
                mItemIdLeftMap.clear();
                return true;
            }
        });
    }

    private boolean containsId(long[] ids, long target) {
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] == target) {
                return true;
            }
        }
        return false;
    }


    private void saveOffsets() {
        final int firstVisiblePosition = mDragDropListener.getDragDropGirdView().getFirstVisiblePosition();
        for (int i = 0; i < mDragDropListener.getDragDropGirdView().getChildCount(); i++) {
            final View child = mDragDropListener.getDragDropGirdView().getChildAt(i);
            final int position = firstVisiblePosition + i;

            if (!isIndexInBound(position)) {
                continue;
            }
            final long itemId = getItemId(position);

            /*Log.e("heheda", "Saving itemId: " + itemId + " for listview child " + i + " Top: "
                    + child.getTop() + " Left: "
                    + child.getLeft());*/

            mItemIdTopMap.put(itemId, child.getTop());
            mItemIdLeftMap.put(itemId, child.getLeft());
        }
    }


    public List<Boolean> getmTextSelected() {
        return mTextSelected;
    }

    public void setmTextSelected(List<Boolean> mTextSelected) {
        this.mTextSelected = mTextSelected;
    }
}
