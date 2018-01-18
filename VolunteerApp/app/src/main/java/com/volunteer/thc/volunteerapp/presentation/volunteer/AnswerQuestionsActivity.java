package com.volunteer.thc.volunteerapp.presentation.volunteer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.View;
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

public class AnswerQuestionsActivity extends AppCompatActivity {

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private ArrayList<InterviewQuestion> questions = new ArrayList<>();
    private TextView questionText;
    private RelativeLayout questionAnswerLayout;
    private Button nextQuestionButton;
    private Boolean isQuestionAnswered;
    private String selectOneAnswerKey;
    private ArrayList<String> selectManyAnswerKeys;
    private ArrayList<String> answeredQuestionsIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_questions);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Answer Questions");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        questionText = (TextView) findViewById(R.id.question_text);
        questionAnswerLayout = (RelativeLayout) findViewById(R.id.answerLayout);
        nextQuestionButton = (Button) findViewById(R.id.next_question);

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
                        if (questions.isEmpty()) {
                            allQuestionsAnswered();
                        } else {
                            displayQuestion(0);
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
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void displayQuestion(final int position) {

        final InterviewQuestion currentQuestion = questions.get(position);
        questionText.setText(currentQuestion.getQuestionText());

        final RadioGroup answersRadioGroup = new RadioGroup(this);
        final LinearLayout selectManyLayout = new LinearLayout(this);
        selectManyLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText answerEditText = new EditText(this);
        answersRadioGroup.setOrientation(RadioGroup.VERTICAL);
        int i;

        isQuestionAnswered = false;

        switch (currentQuestion.getAnswerType()) {
            case YES_NO:
                final RadioButton buttonYes = new RadioButton(this);
                buttonYes.setText("Yes");
                buttonYes.setId((int) 100);
                answersRadioGroup.addView(buttonYes);

                RadioButton buttonNo = new RadioButton(this);
                buttonNo.setText("No");
                buttonNo.setId((int) 101);
                answersRadioGroup.addView(buttonNo);
                questionAnswerLayout.addView(answersRadioGroup);

                answersRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        Log.i("Answer", answersRadioGroup.getCheckedRadioButtonId() == buttonYes.getId() ? "true" : "false");
                        isQuestionAnswered = true; //Checked state changed, so the question was answered
                        if (answersRadioGroup.getCheckedRadioButtonId() == buttonYes.getId()) {
                            currentQuestion.getAnswerList().put("Answer", true);
                        } else {
                            currentQuestion.getAnswerList().put("Answer", false);
                        }
                    }
                });
                break;
            case SELECT_ONE:
                i = 0;
                final RadioButton buttons[] = new RadioButton[10];
                for (String answer : currentQuestion.getAnswerList().keySet()) {
                    buttons[i] = new RadioButton(this);
                    buttons[i].setText(answer);
                    buttons[i].setId(i + 100);
                    answersRadioGroup.addView(buttons[i]);
                    ++i;
                }
                questionAnswerLayout.addView(answersRadioGroup);

                answersRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        isQuestionAnswered = true;
                        selectOneAnswerKey = ((RadioButton) buttons[(answersRadioGroup.getCheckedRadioButtonId()) - 100]).getText().toString();
                    }
                });

                break;
            case SELECT_MANY:
                i = 0;
                selectManyAnswerKeys = new ArrayList<>();
                for (final String answer : currentQuestion.getAnswerList().keySet()) {
                    final AppCompatCheckBox answerCheckBox = new AppCompatCheckBox(this);
                    answerCheckBox.setText(answer);
                    answerCheckBox.setId(i + 100);
                    selectManyLayout.addView(answerCheckBox);
                    ++i;
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
                        questionAnswerLayout.removeView(answersRadioGroup);
                        answersRadioGroup.removeAllViews();
                        break;
                    case SELECT_ONE:
                        if (isQuestionAnswered) {
                            currentQuestion.getAnswerList().put(selectOneAnswerKey, true);
                        }
                        questionAnswerLayout.removeView(answersRadioGroup);
                        answersRadioGroup.removeAllViews();
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

    private void allQuestionsAnswered() {
        questionText.setText("You've answered all available questions!");
        nextQuestionButton.setText("COOL!");
        nextQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
