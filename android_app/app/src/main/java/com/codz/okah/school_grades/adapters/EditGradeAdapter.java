package com.codz.okah.school_grades.adapters;

import android.content.Context;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codz.okah.school_grades.R;
import com.codz.okah.school_grades.tools.GradeFilter;
import com.codz.okah.school_grades.tools.User;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class EditGradeAdapter extends RecyclerView.Adapter<EditGradeAdapter.EditGradeHolder> {
    private Context context;
    private ArrayList<User> students;
    private int visibility;

    public EditGradeAdapter(Context context, ArrayList<User> students) {
        this.context = context;
        this.students = students;
        this.visibility = View.GONE;
    }


    @NonNull
    @Override
    public EditGradeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater =LayoutInflater.from(parent.getContext());
        View item = layoutInflater.inflate(R.layout.item_edit_grades, parent, false);
        return new EditGradeHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull EditGradeHolder holder, int position) {
        String formattedId = String.format("%01d", position+1);
        if(students.size()>9) formattedId = String.format("%02d", position=1);
        if(students.size()>99) formattedId = String.format("%03d", position+1);
        if(students.size()>999) formattedId = String.format("%04d", position+1);
        holder.number.setText(formattedId);

        User student = students.get(position);
        holder.name.setText(student.getFullName());

        holder.exam.setText("");
        holder.td.setText("");
        holder.tp.setText("");

        if(student.getGrades() != null){
            holder.exam.setText(student.getGrades().getExam());
            holder.td.setText(student.getGrades().getTd());
            holder.tp.setText(student.getGrades().getTp());
        }
        holder.tp.setVisibility(visibility);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public class EditGradeHolder extends RecyclerView.ViewHolder{
        TextView number, name;
        EditText td, tp, exam;
        public EditGradeHolder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.number);
            name = itemView.findViewById(R.id.name);
            td = itemView.findViewById(R.id.td);
            tp = itemView.findViewById(R.id.tp);
            exam = itemView.findViewById(R.id.exam);

            td.setFilters(new InputFilter[] {new GradeFilter()});
            tp.setFilters(new InputFilter[] {new GradeFilter()});
            exam.setFilters(new InputFilter[] {new GradeFilter()});
        }
    }

    private String getGradeFormat(double grade) {
        return new DecimalFormat("#.00").format(grade);
    }


    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ArrayList<User> getStudents() {
        return students;
    }

    public void setStudents(ArrayList<User> students) {
        this.students = students;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
        notifyDataSetChanged();
    }
}
