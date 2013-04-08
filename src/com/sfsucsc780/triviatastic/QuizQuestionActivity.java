package com.sfsucsc780.triviatastic;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class QuizQuestionActivity extends Activity {

	private int answerID1, answerID2, answerID3, answerID4;
	private DataBaseHelper myDbHelper = new DataBaseHelper(this);
	private int curQuestion = 0; // using zero-indexed positions
	private Integer totalQuestionCount = new Integer(0);
	private boolean lastQuestionReached = false;
	private Integer coinCount = new Integer(0);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quizquestion);

		myDbHelper.openDataBase();
		curQuestion = 0;

		int quizID = getIntent().getExtras().getInt("quizID");
		totalQuestionCount = myDbHelper.getQuestionCountByQuizID(quizID);

		// get quizID based on which button was pressed
		// int quizID = getIntent().getExtras().getInt("quizID");

		// set typeface for coins/question counter
		Typeface tfFancy = Typeface.createFromAsset(this.getAssets(),
				"fonts/Molle-Regular.ttf");

		Typeface tfBlocky = Typeface.createFromAsset(this.getAssets(),
				"fonts/RacingSansOne-Regular.ttf");

		View questionCounter = findViewById(R.id.questioncount);
		((TextView) questionCounter).setTypeface(tfFancy);

		View coins = findViewById(R.id.coins);
		((TextView) coins).setTypeface(tfBlocky);
		// end of typeface code

		// testing coin readout
		((TextView) findViewById(R.id.coins)).setText(coinCount.toString());

	}

	@Override
	public void onStart() {
		super.onStart();

		getNextQuestion();

	}

	private void getNextQuestion() {
		String question;

		// set question count text at the top of screen using string
		// concatenation
		Integer currentCount = new Integer(curQuestion + 1);
		((TextView) findViewById(R.id.questioncount)).setText("Question "
				+ currentCount.toString() + " of "
				+ totalQuestionCount.toString());

		// get quizID based on which button was pressed on PickQuizActivity
		int quizID = getIntent().getExtras().getInt("quizID");

		// use returned cursor to get question as string
		Cursor questionCursor = myDbHelper.getQuestionsFromQuizID(quizID);

		questionCursor.moveToPosition(curQuestion);

		// getString(3): gets the zero-indexed column 2 from current row cursor
		// is on
		question = questionCursor.getString(2);

		// update UI to show question pulled from database
		((TextView) findViewById(R.id.question)).setText(question);

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

		// increment question counter, will be used to retrieve next question
		curQuestion++;

		// set flag for last question reached
		if (questionCursor.isLast()) {
			lastQuestionReached = true;
		}
	}

	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.answer1:
			if (myDbHelper.isAnswerCorrect(answerID1)) {
				answerCorrect();
			} else {
				answerIncorrect();
			}
			break;
		case R.id.answer2:
			if (myDbHelper.isAnswerCorrect(answerID2)) {
				answerCorrect();
			} else {
				answerIncorrect();
			}
			break;
		case R.id.answer3:
			if (myDbHelper.isAnswerCorrect(answerID3)) {
				answerCorrect();
			} else {
				answerIncorrect();
			}
			break;
		case R.id.answer4:
			if (myDbHelper.isAnswerCorrect(answerID4)) {
				answerCorrect();
			} else {
				answerIncorrect();
			}
			break;
		}

	}

	private void answerCorrect() {
		coinCount++;
		((TextView) findViewById(R.id.coins)).setText(coinCount.toString());

		if (lastQuestionReached) {
			finish();
		} else {
			getNextQuestion();
		}

	}

	private void answerIncorrect() {
		if (lastQuestionReached) {
			finish();
		} else {
			getNextQuestion();
		}

	}

}
