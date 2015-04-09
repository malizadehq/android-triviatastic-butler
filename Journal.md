Day-to-Day Development journal.

## 2013/5/20 ##
  * XML layout for question creation activity complete
  * Creating new activity that handles the addition of new questions.
  * Added new insertion helper method in the Database helper class. (does all logistics for adding a new question to existing quiz).

## 2013/5/19 ##
  * Began work on a new activity - It will allow the user to add a new question to an existing quiz
  * Working on an XML layout first, will create a corresponding activity soon.

## 2013/5/10 ##
  * Added a UI indicator to notify the user how many 50/50 lifelines have been used

## 2013/4/28 ##
  * Completed XML landscape layouts for quizquestion.xml, and finalresult.xml
  * Implemented onSavedInstanceState() to save data to a Bundle that is passed into onCreate(). This will allow the app to repopulate views with the appropriate data, and keep track of critical data that would otherwise be lost between activity resurrection.
  * Re-implemented shake logic to make it simpler to keep the appropriate views hidden after a 50/50 lifeline has been used and the orientation changes.

## 2013/4/27 ##
  * Beginning work on allowing the quiz activity to be rotated into landscape mode
  * Lifecycle methods will need overhauling, I did not anticipate adding orientation changing and allowed them to get unfocused.

## 2013/4/25 ##
  * Implemented crossfading animation for all pop-ups during the quiz activity
  * Made coins persistant using sharedPreferences.

## 2013/4/24 ##
  * Did some code clean-up work. Things were starting to get messy. Simplified some methods, and consolidated code where necessary. (Trying to make things more readable).

## 2013/4/23 ##
  * Able to detect when a player "shakes" the device via accelerometer. This activates the 50/50 lifeline, in which two wrong answers are hidden to help the player. Currently, only two shake lifelines are allowed per-quiz.

## 2013/4/20 ##
  * Finished implementing the final quiz results window once the quiz is completed. Based on the player's performance, it gives a grade (A+ to F) and also displays the number of questions answered correctly. (ie "6 out of 10").
  * Beginning work on 50/50 lifeline. Created a shakedetector java class... still needs work.

## 2013/4/16 ##
  * Added new database helper methods for fetching the correct answer if the player answers wrong.
  * Finished confirmation pop-up. Tells player if answer was correct or incorrect.
  * Started working on the quiz results screen once a quiz is finished.
  * Added some more content to the quiz database.

## 2013/4/13 ##
  * Started working on confirmation pop-up (to tell player if the question was correct or incorrect).
  * Implemented a UI-Handler to smoothly show and hide the confirmation pop-up.

## 2013/4/8 ##
  * Modified quiz-activity to iterate though all the questions of a single quiz and detect when the last question has been reached.
  * Added functionality to the question counter at the very top of the quiz activity. (ie "Question N of 10")

## 2013/4/7 ##
  * Added new class to handle database handling: it copies the packaged database into the filesystem if the database isn't there yet. Also handles accessing records.
  * Modified GUI-only prototype to pull questions/answers from the database and check if correct answer was pressed.
  * Currently, the quiz activity shows first question of quizID = 1 (For testing purposes).

## 2013/4/6 ##
  * Finalized quiz-launching from Pick Quiz Activity, passes quizID into the following quiz-activity.
  * Created a simple sqlite database for testing purposes
  * Working on how to copy, open, and access the database and display records

## 2013/4/1 ##
  * Currently focusing on developing a nice looking and functional GUI-only prototype for the app.
  * Figured out how to utilize custom .ttf fonts
  * Working on a simple and clean way to start a single "quiz-activity" that stems from the button which selects the desired quiz without having to define N number of onClick methods for each and every quiz.

## 2013/3/6 ##
  * Completed mockups in photoshop. Began working on basic layout for application in eclipse. Utilizing mockup elements created in photoshop as resources for the app.
  * Finished basic design documentation and uploaded to Downloads.