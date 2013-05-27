package com.sfsucsc780.triviatastic;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

	// The Android's default system path of your application database.
	private static String DB_PATH = "/data/data/com.sfsucsc780.triviatastic/databases/";

	private static String DB_NAME = "quizzes.db";

	private SQLiteDatabase myDataBase;

	private final Context myContext;

	/**
	 * Constructor Takes and keeps a reference of the passed context in order to
	 * access to the application assets and resources.
	 * 
	 * @param context
	 */
	public DataBaseHelper(Context context) {

		super(context, DB_NAME, null, 1);
		this.myContext = context;
	}

	/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 * */
	public void createDataBase() throws IOException {

		boolean dbExist = checkDataBase();

		if (dbExist) {
			// do nothing - database already exist
		} else {

			// By calling this method and empty database will be created into
			// the default system path
			// of your application so we are gonna be able to overwrite that
			// database with our database.
			this.getReadableDatabase();

			try {

				copyDataBase();

			} catch (IOException e) {

				throw new Error("Error copying database");

			}
		}

	}

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {

		SQLiteDatabase checkDB = null;

		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READWRITE);

		} catch (SQLiteException e) {

			// database does't exist yet.

		}

		if (checkDB != null) {

			checkDB.close();

		}

		// ternary return statement to return true or false if database exists
		return checkDB != null ? true : false;
	}

	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException {

		// Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DB_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	public void openDataBase() throws SQLException {

		// Open the database
		String myPath = DB_PATH + DB_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READWRITE);

	}

	@Override
	public synchronized void close() {

		if (myDataBase != null)
			myDataBase.close();

		super.close();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	// Add your public helper methods to access and get content from the
	// database.
	// You could return cursors by doing "return myDataBase.query(....)" so it'd
	// be easy
	// to you to create adapters for your views.

	public Cursor getQuestionsFromQuizID(Integer quizID) {

		return myDataBase.rawQuery("select * from Questions where quizID ="
				+ quizID.toString(), null);

	}

	public Cursor getAnswersFromQuestionID(Integer questionID, Integer quizID) {

		return myDataBase.rawQuery("select * from Answers where questionID = "
				+ questionID.toString() + " and quizID = " + quizID.toString(),
				null);

	}

	public Boolean isAnswerCorrect(Integer answerID) {

		int correctBit;

		Cursor cursor = myDataBase.rawQuery("select * from Answers where _id ="
				+ answerID.toString(), null);

		cursor.moveToFirst();
		correctBit = cursor.getInt(3);

		if (correctBit == 1) {
			return true;
		} else {
			return false;
		}

	}

	public int getQuestionCountByQuizID(Integer quizID) {

		Cursor cursor = myDataBase.rawQuery(
				"select * from Questions where quizID =" + quizID.toString(),
				null);

		cursor.moveToFirst();

		return cursor.getCount();

	}

	public String getCorrectAnswerText(Integer questionID, Integer quizID) {

		String answerString;

		Cursor cursor = myDataBase.rawQuery(
				"select * from Answers where questionID ="
						+ questionID.toString() + " and quizID ="
						+ quizID.toString() + " and isCorrect = 1", null);

		cursor.moveToFirst();

		answerString = cursor.getString(4);

		return answerString;

	}

	public void insertQuestion(int quizID, String questionText,
			String correctAnswer, String wrongAnswer1, String wrongAnswer2,
			String wrongAnswer3) {

		// Get the total number of questions so we know which questionID to
		// assign
		int questionCount = getQuestionCountByQuizID(quizID);

		// increment to the next questionID
		int questionID = questionCount+1;

		//next few blocks create ContentValues maps for insertion into sqlite database
		
		//QUESTION TABLE, setup the new question ContentValues
		ContentValues questionValues = new ContentValues();
		questionValues.put("quizID", quizID);
		questionValues.put("QuestionText", questionText);
		
		//go ahead and insert new question
		myDataBase.insert("Questions", null, questionValues);

		//ANSWERS TABLE, correct answer
		ContentValues correctAnsValues = new ContentValues();
		correctAnsValues.put("quizID", quizID);
		correctAnsValues.put("questionID", questionID);
		correctAnsValues.put("isCorrect", 1);
		correctAnsValues.put("AnswerText", correctAnswer);

		//ANSWERS TABLE, incorrect answer 1
		ContentValues wrongAnswer1Values = new ContentValues();
		wrongAnswer1Values.put("quizID", quizID);
		wrongAnswer1Values.put("questionID", questionID);
		wrongAnswer1Values.put("isCorrect", 0);
		wrongAnswer1Values.put("AnswerText", wrongAnswer1);

		//ANSWERS TABLE, incorrect answer 2
		ContentValues wrongAnswer2Values = new ContentValues();
		wrongAnswer2Values.put("quizID", quizID);
		wrongAnswer2Values.put("questionID", questionID);
		wrongAnswer2Values.put("isCorrect", 0);
		wrongAnswer2Values.put("AnswerText", wrongAnswer2);

		//ANSWERS TABLE, incorrect answer 3
		ContentValues wrongAnswer3Values = new ContentValues();
		wrongAnswer3Values.put("quizID", quizID);
		wrongAnswer3Values.put("questionID", questionID);
		wrongAnswer3Values.put("isCorrect", 0);
		wrongAnswer3Values.put("AnswerText", wrongAnswer3);

		//create a list of four integers from 1 to 4
		ArrayList<Integer> shuffler = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4));
		
		//shuffle the list into a random order
		Collections.shuffle(shuffler);

		//iterate through the list, randomly inserting each answer in a unique order
		for (int i = 0; i < 4; i++) {

			if (shuffler.get(i) == 1) 
				myDataBase.insert("Answers", null, correctAnsValues);
			else if (shuffler.get(i) == 2) 
				myDataBase.insert("Answers", null, wrongAnswer1Values);
			else if (shuffler.get(i) == 3)
				myDataBase.insert("Answers", null, wrongAnswer2Values);
			else if (shuffler.get(i) == 4)
				myDataBase.insert("Answers", null, wrongAnswer3Values);
				
		}

	}

}
