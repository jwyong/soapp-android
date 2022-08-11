package com.soapp.global.chat_log_selection;

import android.view.MotionEvent;
import android.view.View;

import com.soapp.chat_class.group_chat.GroupChatHolder;
import com.soapp.chat_class.group_chat.GroupChatLog;
import com.soapp.chat_class.single_chat.IndiChatHolder;
import com.soapp.chat_class.single_chat.IndiChatLog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

public class DetailsLookup extends ItemDetailsLookup<Long> {

    private RecyclerView recyclerView;
    private int downCount = 0, position;

    public DetailsLookup(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Nullable
    @Override
    public ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            if (downCount == 0) {
                downCount = downCount + 1;
            } else {
                View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (view != null) {
                    RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);

                    if (viewHolder instanceof IndiChatHolder) {
                        position = ((IndiChatHolder) viewHolder).getSelectionDetails().getPosition();

                        //only text and img/vid selectable for now
                        switch (IndiChatLog.indiChatLogDBS.get(position).getMessage().getIsSender()) {
                            case 10:
                            case 11:
                            case 20:
                            case 21:
                            case 24:
                            case 25:
                            case 60:
                            case 61:
                            case 62:
                            case 63:
                                return ((IndiChatHolder) viewHolder).getSelectionDetails();
                        }

                    } else if (viewHolder instanceof GroupChatHolder) {
                        position = ((GroupChatHolder) viewHolder).getSelectionDetails().getPosition();

                        //only text and img/vid selectable for now
                        switch (GroupChatLog.groupChatLogDBS.get(position).getMessage().getIsSender()) {
                            case 10:
                            case 11:
                            case 12:
                            case 20:
                            case 21:
                            case 24:
                            case 25:
                            case 60:
                            case 61:
                            case 62:
                            case 63:
                                return ((GroupChatHolder) viewHolder).getSelectionDetails();
                        }
                    }
                }

                downCount = 0;
            }
        }

        if (e.getAction() == MotionEvent.ACTION_UP) {
            downCount = 0;
        }
        return null;
    }
}
