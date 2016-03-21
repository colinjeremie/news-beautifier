package com.github.colinjeremie.newsbeautifier.adapters;


import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.colinjeremie.newsbeautifier.R;
import com.github.colinjeremie.newsbeautifier.models.RSSItem;

import java.util.ArrayList;
import java.util.List;

/**
 ** NewsBeautifier
 * Created by james_000 on 2/25/2016.
 */

public class StaggeredRecyclerViewAdapter  extends RecyclerView.Adapter<StaggeredViewHolder> {

    private List<RSSItem> itemList;
    private Activity mActivity;

    public StaggeredRecyclerViewAdapter(Activity activity, List<RSSItem> itemList) {
        this.itemList = new ArrayList<>(itemList);
        this.mActivity = activity;
    }

    @Override
    public StaggeredViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.staggered_list_item, parent, false);
        return new StaggeredViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(StaggeredViewHolder holder, int position) {
        holder.article = itemList.get(position);
        holder.articleTitle.setText(itemList.get(position).getTitle());
        Glide.with(mActivity).load(itemList.get(position).getImage())
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(holder.articlePhoto);
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    public List<RSSItem> getitemList(){
        return itemList;
    }

    public RSSItem removeItem(int position) {
        final RSSItem model = itemList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, RSSItem model) {
        itemList.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final RSSItem model = itemList.remove(fromPosition);
        itemList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<RSSItem> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<RSSItem> newModels) {
        for (int i = itemList.size() - 1; i >= 0; i--) {
            final RSSItem model = itemList.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<RSSItem> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final RSSItem model = newModels.get(i);
            if (!itemList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<RSSItem> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final RSSItem model = newModels.get(toPosition);
            final int fromPosition = itemList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }
}