package com.sfsucsc780.triviatastic;

import android.app.Activity;
import java.lang.reflect.Field;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class PickQuizActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pickquiz);
        
        //Begin Typeface settings for quiz titles
        Typeface tfFancy = Typeface.createFromAsset(this.getAssets(),
                "fonts/Molle-Regular.ttf");
        
        Typeface tfBlocky = Typeface.createFromAsset(this.getAssets(),
        		"fonts/RacingSansOne-Regular.ttf");
        
        //Loop though finite number of quiz titles (7 in this case)
        //using java reflection to iterate through "quizbutton"+"ith quiz number"
        for(int quizNumber = 1; quizNumber < 8; quizNumber++){
        	int id = 0;
			try {
				id = R.id.class.getField("quizbutton" + quizNumber).getInt(0);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	View quizTitle = findViewById(id);
        	((TextView) quizTitle).setTypeface(tfFancy);
        }
        
        View coins = findViewById(R.id.coins);
        ((TextView) coins).setTypeface(tfBlocky);
        
    }
    
    public void onClick(View v) {
    	
    	Intent startQuiz = new Intent(this, QuizQuestionActivity.class);
     
    	switch (v.getId()) {
        case R.id.quizbutton1:
          startQuiz.putExtra("quizID", 1);
          break;
        case R.id.quizbutton2:
          startQuiz.putExtra("quizID", 2);
          break;
        }
    	
        startActivityForResult(startQuiz, 0);
    }
}

