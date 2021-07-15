package org.smartregister.opd.holders;

import android.view.View;

import androidx.annotation.NonNull;

import org.smartregister.opd.contract.GroupListContract;
import org.smartregister.view.ListContract;
import org.smartregister.view.viewholder.ListableViewHolder;

public abstract class GroupedListableViewHolder<T extends ListContract.Identifiable> extends ListableViewHolder<T> implements GroupListContract.GroupedAdapterViewHolder<T> {
    public GroupedListableViewHolder(@NonNull View itemView) {
        super(itemView);
    }
}

