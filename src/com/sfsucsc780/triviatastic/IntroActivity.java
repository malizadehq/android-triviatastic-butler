/*
 * This activity class is simply the intro screen seen
 * when launching the app. It can either redirect the user
 * to the quiz selection activity, or the question creation
 * activity.
 */
package com.sfsucsc780.triviatastic;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class IntroActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.title);

		// Set fancy font for begin button
		Typeface tfFancy = Typeface.createFromAsset(this.getAssets(),
				"fonts/Molle-Regular.ttf");

		View beginButton = findViewById(R.id.begin);
		((TextView) beginButton).setTypeface(tfFancy);
		
		View createButton = findViewById(R.id.createquiz);
		((TextView) createButton).setTypeface(tfFancy);
		// End of fancy font code

		// copy database from assets into filesystem where it can be utilized.
		DataBaseHelper myDbHelper = new DataBaseHelper(null);
		myDbHelper = new DataBaseHelper(this);

		try {

			myDbHelper.createDataBase();

		} catch (IOException ioe) {

			throw new Error("Unable to create database");

		}


	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {

		}
	}

	public void onClick(View v) {

		// Start quiz-selection activity
		Intent intent = new Intent(this, PickQuizActivity.class);

		startActivityForResult(intent, 0);
	}
	
	public void onClickCreate(View v) {

		// Start quiz-creation activity
		Intent intent = new Intent(this, CreateQuizActivity.class);

		startActivityForResult(intent, 0);
	}
}