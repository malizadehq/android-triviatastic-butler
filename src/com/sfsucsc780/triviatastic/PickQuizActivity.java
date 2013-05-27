package com.sfsucsc780.triviatastic;

import android.app.Activity;
import java.lang.reflect.Field;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class PickQuizActivity extends Activity {
	
	private Integer coinTotal = new Integer(0);
	private String coinKey = "com.sfsucsc780.triviatastic.totalCoins";
	
	//this array keeps track of which quizzes are locked. Using zero-indexes.
	//Ex. if quiz #4 in unlocked: isQuizLocked[3] = true;
	boolean[] isQuizLocked = new boolean[] {false, false, false, true, true, true, true};
	
	//this array represents the corresponding quiz unlock costs
	int [] quizUnlockCost = new int[] {0, 0, 0, 20, 20, 20, 60};


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
        
        ((TextView) findViewById(R.id.lockcost4)).setTypeface(tfFancy);
        ((TextView) findViewById(R.id.lockcost5)).setTypeface(tfFancy);
        ((TextView) findViewById(R.id.lockcost6)).setTypeface(tfFancy);
        ((TextView) findViewById(R.id.lockcost7)).setTypeface(tfFancy);
        
       
        
    }
    
	@Override
	public void onStart() {
		super.onStart();

		//Get saved coin total from sharedPrefs
		SharedPreferences sharedPref = this
				.getSharedPreferences("coins", Context.MODE_PRIVATE);

		//retrieves the coin total, if it doesn't exist yet: coins set to 0.
		coinTotal = sharedPref.getInt(coinKey, 0);
		
		//update ui coin count
		((TextView) findViewById(R.id.coins)).setText(coinTotal.toString());
			
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
        case R.id.quizbutton3:
          startQuiz.putExtra("quizID", 2);
          break;
        case R.id.quizbutton4:
          startQuiz.putExtra("quizID", 2); //using quiz # 2 for debugging locked-quizzes
          break;
        case R.id.quizbutton5:
            startQuiz.putExtra("quizID", 2); //using quiz # 2 for debugging locked-quizzes
            break;
        case R.id.quizbutton6:
            startQuiz.putExtra("quizID", 2); //using quiz # 2 for debugging locked-quizzes
            break;
        case R.id.quizbutton7:
            startQuiz.putExtra("quizID", 2); //using quiz # 2 for debugging locked-quizzes
            break;
        }
    	
        startActivityForResult(startQuiz, 0);
    }
    
    //handles the clicks made to locked quizzes
    public void onLockedClick(View v) {
    	
    	switch (v.getId()) {
        case R.id.lockoverlay4:
        	if(coinTotal >= quizUnlockCost[3]){
        		unlockQuiz(4);
        	}
            break;
        case R.id.lockoverlay5:
        	if(coinTotal >= quizUnlockCost[4]){
        		unlockQuiz(5);
        	}
            break;
        case R.id.lockoverlay6:
        	if(coinTotal >= quizUnlockCost[5]){
        		unlockQuiz(6);
        	}
            break;
        case R.id.lockoverlay7:
        	if(coinTotal >= quizUnlockCost[6]){
        		unlockQuiz(7);
        	}
            break;
        }
    	
    }
    
    //handles the unlocking of the passed-in quizNumber
    private void unlockQuiz(int quizNumber){
    	
    	//subtract the cost of quiz from coinTotal
    	coinTotal -= quizUnlockCost[quizNumber-1];
    	
    	//update ui coin count
    	((TextView) findViewById(R.id.coins)).setText(coinTotal.toString());
    	
    	//write coins to sharedPrefs
    	SharedPreferences sharedPref = this.getSharedPreferences("coins", Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = sharedPref.edit();
    	editor.putInt(coinKey, coinTotal);
    	editor.commit();
    	
    	
    	//using java reflection to obtain the appropriate lock overlay from input quizNumber
    	int lockOverlayID = 0;
		try {
			lockOverlayID = R.id.class.getField("lockoverlay" + quizNumber).getInt(0);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
    	
    	//hide the requested lock overlay
    	((View) findViewById(lockOverlayID)).setVisibility(View.GONE);
    	
    	//make the newly unlocked quiz clickable - more java reflection
    	int quizButtonID = 0;
		try {
			quizButtonID = R.id.class.getField("quizbutton" + quizNumber).getInt(0);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		
    	((View) findViewById(quizButtonID)).setClickable(true);
    	
    	
    }
}

