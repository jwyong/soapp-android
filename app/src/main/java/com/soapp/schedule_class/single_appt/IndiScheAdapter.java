package com.soapp.schedule_class.single_appt;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.soapp.R;
import com.soapp.sql.room.entity.Appointment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

/* Created by Jayyong on 29/04/2018. */

public class IndiScheAdapter extends ListAdapter<Appointment, IndiScheHolder> {
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

    IndiScheAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public void submitList(List<Appointment> list) {
        super.submitList(list);
    }

    @NonNull
    @Override
    public IndiScheHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new IndiScheHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.scheduletab_indi_appt_item, parent, false));


    }

    @Override
    public void onBindViewHolder(@NonNull IndiScheHolder holder, int position) {
        holder.setData(getItem(position));
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getApptRow();
    }
}