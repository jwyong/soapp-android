package com.soapp.schedule_class.group_appt;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.sql.room.entity.Appointment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

/* Created by Jayyong on 01/05/2018. */

public class GroupScheAdapter extends ListAdapter<Appointment, GroupScheHolder> {

    private static final DiffUtil.ItemCallback<Appointment> DIFF_CALLBACK = new DiffUtil.ItemCallback<Appointment>() {
        @Override
        public boolean areItemsTheSame(@NonNull Appointment oldUser, @NonNull Appointment newUser) {
            return oldUser.getApptRow() != null && oldUser.getApptRow().equals(newUser.getApptRow());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Appointment oldUser, @NonNull Appointment newUser) {
            return oldUser.equals(newUser);
        }
    };

    GroupScheAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public void submitList(List<Appointment> list) {
        super.submitList(list);
    }

    @NonNull
    @Override
    public GroupScheHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupScheHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.scheduletab_grp_appt_item,
                parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull GroupScheHolder holder, int position) {
        holder.setData(getItem(position));
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getApptRow();
    }
}