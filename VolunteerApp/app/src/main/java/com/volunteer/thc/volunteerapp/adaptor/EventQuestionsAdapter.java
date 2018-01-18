package com.volunteer.thc.volunteerapp.adaptor;

import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.InterviewQuestion;

import java.util.ArrayList;

/**
 * Created by Cristi on 1/12/2018.
 */

public class EventQuestionsAdapter extends RecyclerView.Adapter<EventQuestionsAdapter.EventQuestionsViewHolder> {

    private ArrayList<InterviewQuestion> questionsList;
    private ArrayList<String> selectedQuestionsList;

    public EventQuestionsAdapter(ArrayList<InterviewQuestion> questionsList) {
        this.questionsList = questionsList;
        selectedQuestionsList = new ArrayList<>();
    }

    @Override
    public EventQuestionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_questions_element, parent, false);

        return new EventQuestionsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final EventQuestionsViewHolder holder, final int position) {

        holder.questionText.setText(questionsList.get(position).getQuestionText());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.selectQuestionCheckBox.setChecked(!holder.selectQuestionCheckBox.isChecked());
            }
        });
        holder.selectQuestionCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (holder.selectQuestionCheckBox.isChecked()) {
                    selectedQuestionsList.add(questionsList.get(position).getId());
                } else {
                    selectedQuestionsList.remove(questionsList.get(position).getId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return questionsList.size();
    }

    class EventQuestionsViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout itemView;
        private TextView questionText;
        private AppCompatCheckBox selectQuestionCheckBox;

        EventQuestionsViewHolder(View v) {
            super(v);
            itemView = (RelativeLayout) v.findViewById(R.id.item_view);
            questionText = (TextView) v.findViewById(R.id.content);
            selectQuestionCheckBox = (AppCompatCheckBox) v.findViewById(R.id.select_question);
        }
    }

    public ArrayList<String> getSelectedQuestionsList() {
        return selectedQuestionsList;
    }
}
