/*
 * This activity is the core of the application.
 * 
 * A quiz is loaded based on the .putExtra() that is received from
 * the quiz-selection activity's intent. The activity iterates through all
 * the questions of the selected quiz until the end is reached. 
 * Once the end is reached, a result is displayed. An "exit" button
 * is shown, which kills this activity, and returns to the quiz-selection
 * activity.
 * 
 * Each correct question yields the player 1 coin. During this activity
 * the player gets two phone-shake activated 50/50 lifelines.
 * 
 * A majority of the code is UI manipulation and database accessing.
 * 
 */
package com.sfsucsc780.triviatastic;

import java.util.Timer;
import java.util.TimerTask;
//import com.sfsucsc780.triviatastic.ShakeDetector.OnShakeListener;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class QuizQuestionActivity extends Activity {

	private int answerID1, answerID2, answerID3, answerID4; 									
	private Integer totalCorrectAnswers = new Integer(0); 			// used for evaluation
	private DataBaseHelper myDbHelper = new DataBaseHelper(this); 	// Instantiate a DataBaseHelper																	
	private int curQuestion = 0; 									// using zero-indexed positions
	private Integer totalQuestionCount = new Integer(0); 			// used for question counter at top												
	private boolean lastQuestionReached = false; 					// has the last question been reached?
	private boolean lastQuestionAnswered = false;
	private Integer coinCount = new Integer(0); 					// coin-counter - currently for debugging								
	private String correctAnswerText; 								// will house the correct answer for displaying if player answers wrong.
	private boolean hasQuizBeenFinished = false;			
	
	private String coinKey = "com.sfsucsc780.triviatastic.totalCoins";	
	
	private int mShortAnimationDuration;							//used for animated cross-fade
	private View hideResult;										//hide result
	private View questionButtons;
	private View finalResults;
	
	//shake detection data members
	private SensorManager mSensorManager;
	private ShakeEventListener mSensorListener;
	private int shakesDetected = 0;									//number of shaked detected
	private final int SHAKES_ALLOWED_PER_GAME = 2;					//number of shake-lifelines allowed per game
	private boolean hasQuestionBeenShaked = false;					//has current question already had a shake-lifeline?
	
	//private Bundle savedState;

	// Testing out handler (modified from pear.sfsu.edu handler example)
	private final class UIHandler extends Handler {

		public void handleMessage(Message msg) {
			/**
			 * Retrieve the contents of the message and then update the UI
			 * (i.e., lblResult).
			 */
			hideResult = findViewById(R.id.resultView);
			
			hideResult.animate() //animates the result away
            .alpha(0f)			 //tell animate() what to transition to (0f = completely transparent)
            .setDuration(mShortAnimationDuration)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) { //when animation ends, make sure to hide the view
                    hideResult.setVisibility(View.GONE);
                    
                  //re-enable clickable elements when popup is finished
            		((View) findViewById(R.id.answer1)).setClickable(true);
            		((View) findViewById(R.id.answer2)).setClickable(true);
            		((View) findViewById(R.id.answer3)).setClickable(true);
            		((View) findViewById(R.id.answer4)).setClickable(true);
                }
            });

			if (lastQuestionReached) {
				displayFinalResult();
			} else {
				getNextQuestion();
			}

		}

	};

	// initialize handler for UI changes
	final private Handler handler = new UIHandler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//open the database.
		myDbHelper.openDataBase();
		
		//restored saved bundle from screen rotation
		if (savedInstanceState != null){
		    curQuestion = savedInstanceState.getInt("currentQuestion");
		    shakesDetected = savedInstanceState.getInt("shakeTracker");
		    hasQuestionBeenShaked = savedInstanceState.getBoolean("beenShaked");
		    lastQuestionReached = savedInstanceState.getBoolean("lastQuestionReached");
		    totalCorrectAnswers = savedInstanceState.getInt("correctAnswers");
		    lastQuestionAnswered = savedInstanceState.getBoolean("lastQuestionAnswered");
		  }
		
		
		//Get the system's default "short" animation time.
		mShortAnimationDuration = getResources().getInteger(
		android.R.integer.config_shortAnimTime);

		//obtain quizID depending on which button was pressed in PickQuizActivity. (it sends extras to this activity).
		int quizID = getIntent().getExtras().getInt("quizID");
		
		//get total # of questions from database helper method.
		totalQuestionCount = myDbHelper.getQuestionCountByQuizID(quizID);
		
		//display layout xml
		setContentView(R.layout.quizquestion);
		
		//set the questions and answers as gone
		//issue with rotation of screen once quiz is finished (some lag, using this as a quick-fix)
		((View) findViewById(R.id.questionAndAnswers)).setVisibility(View.GONE);
		
		//used if user changes orientation after final result has been displayed.
		if(lastQuestionAnswered)
			displayFinalResult();

		setUpFonts(); // Felt very cluttered - moved font code to clarify onCreate. Sets Fonts.
		
		//re-update shake indicator when rotating screen
		updateShakeIndicator();
		
		// shake detection
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorListener = new ShakeEventListener();

		mSensorListener
				.setOnShakeListener(new ShakeEventListener.OnShakeListener() {

					//hide two answers when shake is detected (Tries to hide bottom answer views first
					public void onShake() {
						int hiddenCount = 0;

						//restricting number of shake lifelines allowed per-game
						if ( (shakesDetected < SHAKES_ALLOWED_PER_GAME) && !hasQuestionBeenShaked) {
							
							//hide 2 out of 4 answer views
							hideAnswers();
							
							//record how many shakes have been detected.
							shakesDetected++;
							
							//makes sure the UI reflects how many shakes have been used
							updateShakeIndicator();
							
							//indicate that the current question has had a shake-lifeline
							hasQuestionBeenShaked = true;
						}
						
						
						
					}
				});
		
		// get shared preferences for coin total
		// Get saved coin total from sharedPrefs
		SharedPreferences sharedPref = this
				.getSharedPreferences("coins",Context.MODE_PRIVATE);

		// retrieves the coin total, if it doesn't exist yet: coins set to 0.
		coinCount = sharedPref.getInt(coinKey, 0);

		// update ui coin count
		((TextView) findViewById(R.id.coins)).setText(coinCount.toString());
		
		//load question when starting the activity.
		if(!lastQuestionAnswered)
			getNextQuestion();
			
		
	}

	// helper class, mainly for de-clutter reasons.
	void setUpFonts() {
		
		// set typeface for coins/question counter
		Typeface tfFancy = Typeface.createFromAsset(this.getAssets(),
				"fonts/Molle-Regular.ttf");

		Typeface tfBlocky = Typeface.createFromAsset(this.getAssets(),
				"fonts/RacingSansOne-Regular.ttf");

		//Set all typefaces appropriately.
		((TextView) findViewById(R.id.questioncount)).setTypeface(tfFancy);
		((TextView) findViewById(R.id.coins)).setTypeface(tfBlocky);
		((TextView) findViewById(R.id.bigConfirmation)).setTypeface(tfFancy);
		((TextView) findViewById(R.id.smallMessage)).setTypeface(tfFancy);
		((TextView) findViewById(R.id.resultsText)).setTypeface(tfFancy);
		((TextView) findViewById(R.id.letterGrade)).setTypeface(tfBlocky);
		((TextView) findViewById(R.id.youAnswered)).setTypeface(tfFancy);
		((TextView) findViewById(R.id.exactResult)).setTypeface(tfFancy);
		((TextView) findViewById(R.id.backtomenu)).setTypeface(tfFancy);
		((TextView) findViewById(R.id.correctionMessage)).setTypeface(tfBlocky);
		((TextView) findViewById(R.id.incorrectMessage)).setTypeface(tfBlocky);
		((TextView) findViewById(R.id.shaketext)).setTypeface(tfFancy);
		
		

		// end of typeface code
	}
	
	//This allows screen rotation to happen without losing critical information
	//about the current state of the quiz
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		  super.onSaveInstanceState(savedInstanceState);
		  savedInstanceState.putInt("currentQuestion", curQuestion);
		  savedInstanceState.putInt("shakeTracker", shakesDetected);
		  savedInstanceState.putBoolean("beenShaked", hasQuestionBeenShaked);
		  savedInstanceState.putBoolean("lastQuestionReached", lastQuestionReached);
		  savedInstanceState.putBoolean("lastQuestionAnswered", lastQuestionAnswered);
		  savedInstanceState.putInt("correctAnswers", totalCorrectAnswers);
		  
		//write coins to sharedPrefs
		SharedPreferences sharedPref = this.getSharedPreferences("coins", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(coinKey, coinCount);
		editor.commit();
		  
		}

	@Override
	public void onStart() {
		super.onStart();

		

	}
	
	@Override
	protected void onResume() {
		super.onResume();

		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);

	}
	
	  @Override
	    protected void onPause() {
		  
		  //disable accelerometer if activity is paused.
		  mSensorManager.unregisterListener(mSensorListener);
		  
		  super.onStop();
	    }   

	// display next question + detect when last question has been reached.
	private void getNextQuestion() {
		String question;
		
		//set the questions and answers as visible
		((View) findViewById(R.id.questionAndAnswers)).setVisibility(View.VISIBLE);
		
		//if any answer button views were hidden by a shake, restore their visibility to View.VISIBLE
		((View) findViewById(R.id.answer1)).setVisibility(View.VISIBLE);
		((View) findViewById(R.id.answer2)).setVisibility(View.VISIBLE);
		((View) findViewById(R.id.answer3)).setVisibility(View.VISIBLE);
		((View) findViewById(R.id.answer4)).setVisibility(View.VISIBLE);
	

		// set question count text at the top of screen using string concatenation
		Integer currentCount = new Integer(curQuestion+1);
		((TextView) findViewById(R.id.questioncount)).setText("Question "
				+ currentCount.toString() + " of "
				+ totalQuestionCount.toString());

		// get quizID based on which button was pressed on PickQuizActivity
		int quizID = getIntent().getExtras().getInt("quizID");

		// use returned cursor to get question as string
		Cursor questionCursor = myDbHelper.getQuestionsFromQuizID(quizID);

		//move database cursor to the correct position
		questionCursor.moveToPosition(curQuestion);

		// getString(3): gets the zero-indexed column 2 from current row cursor is on. (i.e. the question text).
		question = questionCursor.getString(2);

		// update UI to show question pulled from database
		((TextView) findViewById(R.id.question)).setText(question);

		// collect correct answer for current question: only seen if player answers wrong.
		correctAnswerText = myDbHelper.getCorrectAnswerText(curQuestion + 1,
				quizID);

		// retrieve cursor object from DataBaseHelper object
		Cursor answerCursor = myDbHelper.getAnswersFromQuestionID(
				curQuestion + 1, quizID);

		// set each answer text, there will always be 4 answers
		// also collect questionID for isCorrect check when onClick is called
		answerCursor.moveToFirst();
		((TextView) findViewById(R.id.answer1)).setText(answerCursor
				.getString(4));
		answerID1 = answerCursor.getInt(0);

		answerCursor.moveToNext();
		((TextView) findViewById(R.id.answer2)).setText(answerCursor
				.getString(4));
		answerID2 = answerCursor.getInt(0);

		answerCursor.moveToNext();
		((TextView) findViewById(R.id.answer3)).setText(answerCursor
				.getString(4));
		answerID3 = answerCursor.getInt(0);

		answerCursor.moveToNext();
		((TextView) findViewById(R.id.answer4)).setText(answerCursor
				.getString(4));
		answerID4 = answerCursor.getInt(0);

		// set flag for last question reached
		if (questionCursor.isLast()) {
			lastQuestionReached = true;
		}
		
		//if question has already been shaken between changing orientations hide answers
		if(hasQuestionBeenShaked)
			hideAnswers();
		
	}

	//When an answer is clicked, check if answer is correct and call handleAnswer() method.
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.answer1:
			handleAnswer(myDbHelper.isAnswerCorrect(answerID1));
			break;
		case R.id.answer2:
			handleAnswer(myDbHelper.isAnswerCorrect(answerID2));
			break;
		case R.id.answer3:
			handleAnswer(myDbHelper.isAnswerCorrect(answerID3));
			break;
		case R.id.answer4:
			handleAnswer(myDbHelper.isAnswerCorrect(answerID4));
			break;
		}

	}

	//handleAnswer changes the popup-confirmation to the appropriate state: correct or incorrect
	private void handleAnswer(boolean correct) {
		
		// increment question counter, will be used to retrieve next question
		curQuestion++;
		
		if(lastQuestionReached)
			lastQuestionAnswered = true;
		
		//when fetching the next question - reset shake already detected flag
		hasQuestionBeenShaked = false;
		
		//disable clickable elements while popup is being displayed - will reenable in the handler(timer)
		((View) findViewById(R.id.answer1)).setClickable(false);
		((View) findViewById(R.id.answer2)).setClickable(false);
		((View) findViewById(R.id.answer3)).setClickable(false);
		((View) findViewById(R.id.answer4)).setClickable(false);
		

		if (correct) {
			totalCorrectAnswers++; // increment correct answer counter by 1.

			// temporary coin mechanics
			coinCount++;
			((TextView) findViewById(R.id.coins)).setText(coinCount.toString());

			// Change TextView strings to correct state
			((TextView) findViewById(R.id.bigConfirmation)).setText("Nice!");
			((TextView) findViewById(R.id.smallMessage))
					.setText("You answered correctly!");
			
			//reveal bigcoin graphic
			((View) findViewById(R.id.bigCoin)).setVisibility(View.VISIBLE);

			// begin timed confirmation popup code
			View result = findViewById(R.id.resultView);
			result.setAlpha(0f);
			result.setVisibility(View.VISIBLE);
			
			result.animate()
            .alpha(1f)
            .setDuration(mShortAnimationDuration)
            .setListener(null);
			

			// reveal "you answered incorrectly." TextView
			View incorrect = findViewById(R.id.incorrectMessage);
			incorrect.setVisibility(View.GONE);

			// reveal correct answer TextView
			View correctAnswer = findViewById(R.id.correctionMessage);
			correctAnswer.setVisibility(View.GONE);
		} 
		
		//If answer is not correct, the following else is executed.
		else {
			((TextView) findViewById(R.id.correctionMessage))
					.setText(correctAnswerText);

			// Change TextView strings to correct state
			((TextView) findViewById(R.id.bigConfirmation)).setText("Sorry!");
			((TextView) findViewById(R.id.smallMessage))
					.setText("You answered incorrectly.");

			// begin timed confirmation popup code
			View result = findViewById(R.id.resultView);
			result.setAlpha(0f);
			result.setVisibility(View.VISIBLE);
			
			result.animate()
            .alpha(1f)
            .setDuration(mShortAnimationDuration)
            .setListener(null);
			
			//hide coin graphic
			((View) findViewById(R.id.bigCoin)).setVisibility(View.GONE);
			
			// reveal "you answered incorrectly." TextView
			View incorrect = findViewById(R.id.incorrectMessage);
			incorrect.setVisibility(View.VISIBLE);

			// reveal correct answer TextView
			View correctAnswer = findViewById(R.id.correctionMessage);
			correctAnswer.setVisibility(View.VISIBLE);
		}

		//TimerTask allows us to defer run()'s code to happen at a later time
		TimerTask hideConfirmation = new TimerTask() {
			public void run() {
				
				//dummy Message, nothing passed into handler.
				Message msg = handler.obtainMessage();
				handler.sendMessage(msg);
			}
		};

		Timer timer = new Timer();
		if(correct)
			timer.schedule(hideConfirmation, 2000); // when answer is correct, delay visibility=gone for 2 seconds
		else
			timer.schedule(hideConfirmation, 4000); // when answer is incorrect, delay visibility=gone for 4 seconds

	}
	
	//When a shake event is detected, this hides the first two incorrect answers, starting from the bottom answer.
	void hideAnswers(){
		
		//counter used to make sure we only hide 2/4 answers
		int hiddenCount = 0;

			if (!myDbHelper.isAnswerCorrect(answerID4)) {
				((View) findViewById(R.id.answer4))
						.setVisibility(View.GONE);
				hiddenCount++;
			}
			if (!myDbHelper.isAnswerCorrect(answerID3)) {
				((View) findViewById(R.id.answer3))
						.setVisibility(View.GONE);
				hiddenCount++;
			}
			if (!myDbHelper.isAnswerCorrect(answerID2)
					&& (hiddenCount < 2)) {
				((View) findViewById(R.id.answer2))
						.setVisibility(View.GONE);
				hiddenCount++;
			}
			if (!myDbHelper.isAnswerCorrect(answerID1)
					&& (hiddenCount < 2)) {
				((View) findViewById(R.id.answer1))
						.setVisibility(View.GONE);
				hiddenCount++;
			}	
	}

	//When getNextQuestion() determines the last question has been reached, the next call to the UI-handler will 
	//call the following method. (i.e. when handleAnswer() executes).
	void displayFinalResult() {
		
		String grade;
		
		//hide the 50-50 counter at the bottom/on the bar
		((View) findViewById(R.id.shakeIndicator)).setVisibility(View.GONE);
		
		// set question count text at the top of screen using string concatenation
		Integer currentCount = new Integer(curQuestion);
		((TextView) findViewById(R.id.questioncount)).setText("Question "
			+ currentCount.toString() + " of "
			+ totalQuestionCount.toString());
		
		
		//show how many questions were answered correctly.
		String exactResult = totalCorrectAnswers.toString() + " out of "
				+ totalQuestionCount.toString();

		//determine player's grade at end of quiz.
		float ratio = (float) totalCorrectAnswers / (float) totalQuestionCount;

		if (ratio == 1)
			grade = "A+";
		else if (ratio > .94)
			grade = "A";
		else if (ratio > .89)
			grade = "A-";
		else if (ratio > .84)
			grade = "B+";
		else if (ratio > .79)
			grade = "B";
		else if (ratio > .77)
			grade = "B-";
		else if (ratio > .74)
			grade = "C+";
		else if (ratio > .69)
			grade = "C";
		else if (ratio > .64)
			grade = "C-";
		else if (ratio > .59)
			grade = "D";
		else if (ratio > .55)
			grade = "D-";
		else
			grade = "F";

		((TextView) findViewById(R.id.letterGrade)).setText(grade); 			// set letter grade
		((TextView) findViewById(R.id.exactResult)).setText(exactResult); 		// set exact result
		
		
		//((View) findViewById(R.id.questionAndAnswers)).setVisibility(View.GONE);// hide question + answers
		questionButtons = findViewById(R.id.questionAndAnswers);
		finalResults = findViewById(R.id.endResults);
		
		//handles animation of the final result and question/answer views
		if (!hasQuizBeenFinished) {
			questionButtons.animate().alpha(0f)
					.setDuration(mShortAnimationDuration)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							questionButtons.setVisibility(View.GONE);
						}
					});
			
			finalResults.setAlpha(0f);
			finalResults.setVisibility(View.VISIBLE);

			finalResults.animate().alpha(1f)
					.setDuration(mShortAnimationDuration).setListener(null);
		} else {
			questionButtons.setVisibility(View.GONE);
			finalResults.setVisibility(View.VISIBLE);
		}
		
		hasQuizBeenFinished = true;
	}

	//"Back To Menu" button's onClick method. Exits the activity once the end of the quiz has been reached.
	public void backToMenu(View v) {
		
		//write coins to sharedPrefs
		SharedPreferences sharedPref = this.getSharedPreferences("coins", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(coinKey, coinCount);
		editor.commit();
		
		finish();
	}
	
	//updates the UI to reflect how many shakes have been used.
	//Gets called during onCreate() or if a shake event is detected.
	private void updateShakeIndicator(){
		
		View check1 = findViewById(R.id.redcheck1);
		View check2 = findViewById(R.id.redcheck2);
		
		if(shakesDetected >= 1)
			check1.setVisibility(View.VISIBLE);
		
		if(shakesDetected == 2)
			check2.setVisibility(View.VISIBLE);
		
	}

}
