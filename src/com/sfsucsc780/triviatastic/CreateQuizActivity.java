package com.sfsucsc780.triviatastic;

import java.util.Timer;
import java.util.TimerTask;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class CreateQuizActivity extends Activity {

	private DataBaseHelper myDbHelper;

	//refrences to EditFields used throughout activity
	private EditText questionText;
	private EditText correctAnswerText;
	private EditText incorrectAnswer1Text;
	private EditText incorrectAnswer2Text;
	private EditText incorrectAnswer3Text;
	
	//system-collected short-animation time
	private int mShortAnimationDuration;
	
	//reference to popup <include> ID
	private View submitPopup;

	private Spinner spinner;
	private int quizID;
	
	// initialize handler for UI changes
	final private Handler handler = new UIHandler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.createquiz);
		
		mShortAnimationDuration = getResources().getInteger(
				android.R.integer.config_shortAnimTime);

		myDbHelper = new DataBaseHelper(this);

		myDbHelper.openDataBase();

		questionText = (EditText) findViewById(R.id.question_text);
		correctAnswerText = (EditText) findViewById(R.id.correct_text);
		incorrectAnswer1Text = (EditText) findViewById(R.id.incorrect_text1);
		incorrectAnswer2Text = (EditText) findViewById(R.id.incorrect_text2);
		incorrectAnswer3Text = (EditText) findViewById(R.id.incorrect_text3);
		spinner = (Spinner) findViewById(R.id.spinner1);
		submitPopup = (View) findViewById(R.id.submit_popup);
		
		Typeface tfFancy = Typeface.createFromAsset(this.getAssets(),
                "fonts/Molle-Regular.ttf");
		
		((TextView) findViewById(R.id.title)).setTypeface(tfFancy);
		((TextView) findViewById(R.id.big_submit_message)).setTypeface(tfFancy);

	}

	public void onClick(View v) {

		//get zero indexed position of the spinner and add 1, quizIDs will
		//be correctly ordered to reflect their quizID value
		quizID = spinner.getSelectedItemPosition()+1;
		
		//get each field's current text, assign to Strings
		String question = questionText.getText().toString();
		String answer = correctAnswerText.getText().toString();
		String wrong1 = incorrectAnswer1Text.getText().toString();
		String wrong2 = incorrectAnswer2Text.getText().toString();
		String wrong3 = incorrectAnswer3Text.getText().toString();
		
		//insert new question/answers into database
		myDbHelper.insertQuestion(quizID, question, answer,
		 wrong1, wrong2, wrong3);
		
		//clear all the text fields
		questionText.setText("");
		correctAnswerText.setText("");
		incorrectAnswer1Text.setText("");
		incorrectAnswer2Text.setText("");
		incorrectAnswer3Text.setText("");
		
		submitPopup.setAlpha(0f);
		submitPopup.setVisibility(View.VISIBLE);
		
		submitPopup.animate()
        .alpha(1f)
        .setDuration(mShortAnimationDuration)
        .setListener(null);
		
		
		TimerTask hideConfirmation = new TimerTask() {
			public void run() {
				
				//dummy Message, nothing passed into handler.
				Message msg = handler.obtainMessage();
				handler.sendMessage(msg);
			}
		};

		Timer timer = new Timer();
		timer.schedule(hideConfirmation, 2000);
	}
	
	private final class UIHandler extends Handler {

		public void handleMessage(Message msg) {
			
			
			submitPopup.animate() //animates the result away
            .alpha(0f)			 //tell animate() what to transition to (0f = completely transparent)
            .setDuration(mShortAnimationDuration)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) { //when animation ends, make sure to hide the view
                	submitPopup.setVisibility(View.GONE);
                   
                }
            });

		}

	};

}