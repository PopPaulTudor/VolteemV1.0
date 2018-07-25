package com.volunteer.thc.volunteerapp.presentation.volunteer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.InterviewQuestion;
import com.volunteer.thc.volunteerapp.util.DatabaseUtils;

import java.util.ArrayList;

/**
 * Created by poppa on 17.01.2018.
 */

public class VolunteerProfileQuestionFragment extends Fragment {

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private ArrayList<InterviewQuestion> questions = new ArrayList<>();
    private TextView questionText, allQuestionsAnsweredText;
    private RelativeLayout questionAnswerLayout;
    private Button nextQuestionButton;
    private Boolean isQuestionAnswered;
    private String selectOneAnswerKey;
    private ArrayList<String> selectManyAnswerKeys;
    private ArrayList<String> answeredQuestionsIds = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volunteer_profile_question, container, false);

        questionText = view.findViewById(R.id.question_text);
        questionAnswerLayout = view.findViewById(R.id.answerLayout);
        nextQuestionButton = view.findViewById(R.id.next_question);
        allQuestionsAnsweredText = view.findViewById(R.id.all_questions_answered_text);

        mDatabase.child("users/volunteers/" + DatabaseUtils.getUserID() + "/answeredQuestions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot answeredQuestion : dataSnapshot.getChildren()) {
                    answeredQuestionsIds.add(answeredQuestion.getKey());
                }
                mDatabase.child("questions").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            InterviewQuestion interviewQuestion = dataSnapshot1.getValue(InterviewQuestion.class);
                            if (!answeredQuestionsIds.contains(interviewQuestion.getId())) {
                                questions.add(interviewQuestion);
                            } else {
                                Log.i(interviewQuestion.getId(), "already answered");
                            }
                        }
                        if(isFragmentActive()) {
                            if (questions.isEmpty()) {
                                allQuestionsAnswered();
                            } else {
                                showQuestions();
                                displayQuestion(0);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("QuestionsError", databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AnsweredQuestionsRead", databaseError.getMessage());
            }
        });

        return view;
    }

    private void displayQuestion(final int position) {

        final InterviewQuestion currentQuestion = questions.get(position);
        questionText.setText(currentQuestion.getQuestionText());

        final RadioGroup selectOneAnswerRadioGroup = new RadioGroup(getActivity());
        final LinearLayout selectManyLayout = new LinearLayout(getActivity());
        selectManyLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText answerEditText = new EditText(getActivity());
        selectOneAnswerRadioGroup.setOrientation(RadioGroup.VERTICAL);
        int radioButtonId;

        isQuestionAnswered = false;

        switch (currentQuestion.getAnswerType()) {
            case YES_NO:
                final RadioButton buttonYes = new RadioButton(getActivity());
                buttonYes.setText("Yes");
                buttonYes.setId((int)100);
                selectOneAnswerRadioGroup.addView(buttonYes);

                RadioButton buttonNo = new RadioButton(getActivity());
                buttonNo.setText("No");
                buttonNo.setId((int) 101);
                selectOneAnswerRadioGroup.addView(buttonNo);
                questionAnswerLayout.addView(selectOneAnswerRadioGroup);

                selectOneAnswerRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        Log.i("Answer", selectOneAnswerRadioGroup.getCheckedRadioButtonId() == buttonYes.getId() ? "true" : "false");
                        isQuestionAnswered = true; //Checked state changed, so the question was answered
                        if (selectOneAnswerRadioGroup.getCheckedRadioButtonId() == buttonYes.getId()) {
                            currentQuestion.getAnswerList().put("Answer", true);
                        } else {
                            currentQuestion.getAnswerList().put("Answer", false);
                        }
                    }
                });
                break;
            case SELECT_ONE:
                radioButtonId = 0;
                final RadioButton radioButtons[] = new RadioButton[10];
                for (String answer : currentQuestion.getAnswerList().keySet()) {
                    radioButtons[radioButtonId] = new RadioButton(getActivity());
                    radioButtons[radioButtonId].setText(answer);
                    radioButtons[radioButtonId].setId(radioButtonId + 100);
                    selectOneAnswerRadioGroup.addView(radioButtons[radioButtonId]);
                    ++radioButtonId;
                }
                questionAnswerLayout.addView(selectOneAnswerRadioGroup);

                selectOneAnswerRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        isQuestionAnswered = true;
                        selectOneAnswerKey = ((RadioButton) radioButtons[(selectOneAnswerRadioGroup.getCheckedRadioButtonId()) - 100]).getText().toString();
                    }
                });

                break;
            case SELECT_MANY:
                radioButtonId = 0;
                selectManyAnswerKeys = new ArrayList<>();
                for (final String answer : currentQuestion.getAnswerList().keySet()) {
                    final AppCompatCheckBox answerCheckBox = new AppCompatCheckBox(getActivity());
                    answerCheckBox.setText(answer);
                    answerCheckBox.setId(radioButtonId + 100);
                    selectManyLayout.addView(answerCheckBox);
                    ++radioButtonId;
                    answerCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            Log.i(answer, answerCheckBox.isChecked() ? "true" : "false");
                            if (answerCheckBox.isChecked()) {
                                selectManyAnswerKeys.add(answer);
                            } else {
                                selectManyAnswerKeys.remove(answer);
                            }
                        }
                    });
                }
                questionAnswerLayout.addView(selectManyLayout);
                break;
            case WRITE_ANSWER:
                answerEditText.setHint("Write your answer here");
                questionAnswerLayout.addView(answerEditText);
                break;
        }

        nextQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (currentQuestion.getAnswerType()) {
                    case YES_NO:
                        questionAnswerLayout.removeView(selectOneAnswerRadioGroup);
                        selectOneAnswerRadioGroup.removeAllViews();
                        break;
                    case SELECT_ONE:
                        if (isQuestionAnswered) {
                            currentQuestion.getAnswerList().put(selectOneAnswerKey, true);
                        }
                        questionAnswerLayout.removeView(selectOneAnswerRadioGroup);
                        selectOneAnswerRadioGroup.removeAllViews();
                        break;
                    case SELECT_MANY:
                        questionAnswerLayout.removeView(selectManyLayout);
                        selectManyLayout.removeAllViews();
                        if (!selectManyAnswerKeys.isEmpty()) {
                            isQuestionAnswered = true;
                            for (String answer : selectManyAnswerKeys) {
                                currentQuestion.getAnswerList().put(answer, true);
                            }
                        }
                        break;
                    case WRITE_ANSWER:
                        if (!answerEditText.getText().toString().isEmpty()) {
                            currentQuestion.setAnswerText(answerEditText.getText().toString());
                            isQuestionAnswered = true;
                        }
                        questionAnswerLayout.removeView(answerEditText);
                }
                if (isQuestionAnswered) {
                    Log.e("question", currentQuestion.getQuestionText());
                    mDatabase.child("users/volunteers/" + DatabaseUtils.getUserID() + "/answeredQuestions/" + currentQuestion.getId()).setValue(currentQuestion);
                }
                if (position == questions.size() - 1) {
                    allQuestionsAnswered();
                } else {
                    if (isQuestionAnswered) {
                        displayQuestion(position + 1);
                    } else {
                        questions.remove(currentQuestion);
                        questions.add(currentQuestion);
                        displayQuestion(position);
                    }
                }
            }
        });
    }

    private void showQuestions() {
        nextQuestionButton.setVisibility(View.VISIBLE);
        questionAnswerLayout.setVisibility(View.VISIBLE);
    }

    private void allQuestionsAnswered() {
        allQuestionsAnsweredText.setVisibility(View.VISIBLE);
        questionText.setVisibility(View.GONE);
        nextQuestionButton.setVisibility(View.GONE);
        questionAnswerLayout.setVisibility(View.GONE);
    }

    private boolean isFragmentActive() {
        return isAdded() && !isDetached() && !isRemoving();
    }
}
