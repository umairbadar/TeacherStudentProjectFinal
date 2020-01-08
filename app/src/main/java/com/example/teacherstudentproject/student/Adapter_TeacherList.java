package com.example.teacherstudentproject.student;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teacherstudentproject.R;

import java.util.List;

public class Adapter_TeacherList extends RecyclerView.Adapter<Adapter_TeacherList.ViewHolder> {

    List<Model_TeacherList> list;
    Context context;

    public Adapter_TeacherList(List<Model_TeacherList> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Model_TeacherList item = list.get(position);

        holder.tv_teacher_name.setText(item.getName());
        holder.tv_teacher_distance.setText(item.getDistance());

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TeacherDetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("teacher_id", item.getID());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_teacher_name, tv_teacher_distance;
        LinearLayout mainLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_teacher_name = itemView.findViewById(R.id.tv_teacher_name);
            tv_teacher_distance = itemView.findViewById(R.id.tv_teacher_distance);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}
