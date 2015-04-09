/* Copyright (c) 2008-2011 -- CommonsWare, LLC

	 Licensed under the Apache License, Version 2.0 (the "License");
	 you may not use this file except in compliance with the License.
	 You may obtain a copy of the License at

		 http://www.apache.org/licenses/LICENSE-2.0

	 Unless required by applicable law or agreed to in writing, software
	 distributed under the License is distributed on an "AS IS" BASIS,
	 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 See the License for the specific language governing permissions and
	 limitations under the License.
 */

package com.ece1778.project.myAnkle.Helpers;

import com.ece1778.project.myAnkleUser.BuildConfig;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	public static final String DATABASE_NAME = "myAnkle";
	public static final int DATABASE_VERSION = 4;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		
		// enable foreign key support for the current database session
		db.execSQL("PRAGMA foreign_keys=ON;");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		

		
		// enable foreign key support for the first time
		db.execSQL("PRAGMA foreign_keys=ON;");
		
		db.execSQL("CREATE TABLE users ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "serverId INTEGER NOT NULL, "
				+ "name TEXT NOT NULL, "
				+ "age INTEGER NOT NULL, "
				+ "gender TEXT NOT NULL, "
				+ "level INTEGER NOT NULL, "
				+ "consent INTEGER NOT NULL"
				+ ");");
		
		db.execSQL("CREATE TABLE exercises ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "position INTEGER NOT NULL, "
				+ "name TEXT NOT NULL, "
				+ "instruction TEXT NOT NULL, "
				+ "equipment TEXT NOT NULL, "
				+ "eyeState TEXT NOT NULL, "
				+ "difficulty TEXT NOT NULL, "
				+ "picture TEXT NOT NULL, "
				+ "upgradeThreshold REAL NOT NULL, "
				+ "downgradeThreshold REAL NOT NULL"
				+ ");");
		
		db.execSQL("CREATE TABLE sessions ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "userId INTEGER NOT NULL, "
				+ "exerciseId INTEGER NOT NULL, "
				+ "ankleSide TEXT NOT NULL, "
				+ "meanR REAL NOT NULL, "
				+ "date TEXT NOT NULL, " 
				+ "FOREIGN KEY(userId) REFERENCES users(_id), "
				+ "FOREIGN KEY(exerciseId) REFERENCES exercises(_id)"
				+ ");");
		
		db.execSQL("CREATE TABLE injuries ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "userId INTEGER NOT NULL, "
				+ "ankleSide TEXT NOT NULL, "
				+ "injuryDate TEXT NOT NULL, "
				+ "severity INT, "
				+ "FOREIGN KEY(userId) REFERENCES users(_id)"
				+ ");");
		
		/* EXERCISES - EYES OPEN */
		
		//1 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	1," +
				"		'Standing'," +
				"		'Stand on the floor with both feet shoulder distance apart for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Floor'," +
				"		'Open'," +
				"		'Easy', " +
				"		'exercise_01'," +
				"		0.0," +
				"		0.0		);");
		
		//2 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	2," +
				"		'Feet Together'," +
				"		'Stand on the floor with both feet together for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Floor'," +
				"		'Open'," +
				"		'Easy', " +
				"		'exercise_02'," +
				"		0.0," +
				"		0.0		);");
		
		//3 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	3," +
				"		'Tandem'," +
				"		'Stand on the floor with the chosen foot directly in front of the other, with the heel of the front foot touching the toe of the back foot. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Floor'," +
				"		'Open'," +
				"		'Moderate', " +
				"		'exercise_03'," +
				"		0.0," +
				"		0.0		);");
		
		//4 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	4," +
				"		'One Foot'," +
				"		'Stand with only chosen foot on the floor for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Floor'," +
				"		'Open'," +
				"		'Moderate', " +
				"		'exercise_04'," +
				"		0.0," +
				"		0.0		);");
		
		//5 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	5," +
				"		'Front and Back'," +
				"		'Stand on a small balance board tiliting from front to back for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Balance Board Small'," +
				"		'Open'," +
				"		'Moderate', " +
				"		'exercise_05'," +
				"		0.0," +
				"		0.0		);");
		
		//6 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	6," +
				"		'Side to Side'," +
				"		'Stand on a small balance board tiliting from side to side for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Balance Board Small'," +
				"		'Open'," +
				"		'Moderate', " +
				"		'exercise_06'," +
				"		0.0," +
				"		0.0		);");
		
		//7 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	7," +
				"		'Feet Together (Front and Back)'," +
				"		'Stand on a small balance board tiliting from front to back, with both feet together, for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Balance Board Small'," +
				"		'Open'," +
				"		'Moderate', " +
				"		'exercise_07'," +
				"		0.0," +
				"		0.0		);");
		
		//8 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	8," +
				"		'Feet Together (Side to Side)'," +
				"		'Stand on a small balance board tiliting from side to side, with both feet together, for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Balance Board Small'," +
				"		'Open'," +
				"		'Moderate', " +
				"		'exercise_08'," +
				"		0.0," +
				"		0.0		);");
		
		//9 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	9," +
				"		'Tandem (Front and Back)'," +
				"		'Stand on a small balance board tiliting from front to back. Keep the chosen foot in front of the other, with the heel of the front foot touching the toe of the back foot, for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Balance Board Small'," +
				"		'Open'," +
				"		'Moderate', " +
				"		'exercise_09'," +
				"		0.0," +
				"		0.0		);");
		
		//10 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	10," +
				"		'One Foot (Front and Back)'," +
				"		'Stand on a small balance board with only chosen foot  tiliting from front to back for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Balance Board Small'," +
				"		'Open'," +
				"		'Hard', " +
				"		'exercise_10'," +
				"		0.0," +
				"		0.0		);");
		
		//11 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	11," +
				"		'One Foot (Side to Side)'," +
				"		'Stand on a small balance board with only chosen foot  tiliting from Side to Side for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Balance Board Small'," +
				"		'Open'," +
				"		'Hard', " +
				"		'exercise_11'," +
				"		0.0," +
				"		0.0		);");
		
		//12 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	12," +
				"		'Feet Apart'," +
				"		'Stand on a balance disc with both feet apart for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Balance Disc'," +
				"		'Open'," +
				"		'Hard', " +
				"		'exercise_12'," +
				"		0.0," +
				"		0.0		);");
		
		//13 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	13," +
				"		'Feet Together'," +
				"		'Stand on a balance disc with both feet together for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Balance Disc'," +
				"		'Open'," +
				"		'Hard', " +
				"		'exercise_13'," +
				"		0.0," +
				"		0.0		);");
		
		//14 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	14," +
				"		'One Foot'," +
				"		'Stand with only chosen foot on a balance disk for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Balance Disc'," +
				"		'Open'," +
				"		'Very Hard', " +
				"		'exercise_14'," +
				"		0.0," +
				"		0.0		);");
		
		//15 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	15," +
				"		'Standing'," +
				"		'Stand on a round balance board with both feet shoulder distance apart for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Balance Board Round'," +
				"		'Open'," +
				"		'Very Hard', " +
				"		'exercise_15'," +
				"		0.0," +
				"		0.0		);");
		
		//16 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	16," +
				"		'Feet Together'," +
				"		'Stand on a round balance board with both feet together for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Balance Board Round'," +
				"		'Open'," +
				"		'Very Hard', " +
				"		'exercise_16'," +
				"		0.0," +
				"		0.0		);");
		
		// 17 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	17," +
				"		'Tandem'," +
				"		'Stand on a round balance board with the chosen foot in front of the other, with the heel of the front foot touching the toe of the back foot. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Balance Board Round'," +
				"		'Open'," +
				"		'Very Hard', " +
				"		'exercise_17'," +
				"		0.0," +
				"		0.0		);");
		
		//18 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	18," +
				"		'One Foot'," +
				"		'Stand with only chosen foot on a round balance board for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Balance Board Round'," +
				"		'Open'," +
				"		'Very Hard', " +
				"		'exercise_18'," +
				"		0.0," +
				"		0.0		);");
		
		//19 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	19," +
				"		'Standing'," +
				"		'Stand on a balance pad with both feet shoulder distance apart for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Balance Pad'," +
				"		'Open'," +
				"		'Easy', " +
				"		'exercise_19'," +
				"		0.0," +
				"		0.0		);");
		
		//20 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	20," +
				"		'Feet Together'," +
				"		'Stand on a balance pad with both feet together for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Balance Pad'," +
				"		'Open'," +
				"		'Moderate', " +
				"		'exercise_20'," +
				"		0.0," +
				"		0.0		);");
		
		//21 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	21," +
				"		'Tandem'," +
				"		'Stand on a balance pad with chosen foot directly in front of the other, with the heel of the front foot touching the toe of the back foot, for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Balance Pad'," +
				"		'Open'," +
				"		'Moderate', " +
				"		'exercise_21'," +
				"		0.0," +
				"		0.0		);");
		
		//22 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	22," +
				"		'One Foot'," +
				"		'Stand with only chosen foot on a balance pad for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'Balance Pad'," +
				"		'Open'," +
				"		'Hard', " +
				"		'exercise_22'," +
				"		0.0," +
				"		0.0		);");
		
		//23 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	23," +
				"		'Standing (Round Side Up)'," +
				"		'Stand on a BOSU ball (round side up) with both feet shoulder distance apart for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'BOSU Ball'," +
				"		'Open'," +
				"		'Very Hard', " +
				"		'exercise_23'," +
				"		0.0," +
				"		0.0		);");
		
		//24 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	24," +
				"		'Standing (Flat Side Up)'," +
				"		'Stand on a BOSU ball (flat side up) with both feet shoulder distance apart for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'BOSU Ball'," +
				"		'Open'," +
				"		'Very Hard', " +
				"		'exercise_24'," +
				"		0.0," +
				"		0.0		);");
		
		//25 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	25," +
				"		'Feet Together (Round Side Up)'," +
				"		'Stand on a BOSU ball (round side up) with both feet together for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'BOSU Ball'," +
				"		'Open'," +
				"		'Very Hard', " +
				"		'exercise_25'," +
				"		0.0," +
				"		0.0		);");
		
		//26 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	26," +
				"		'Feet Together (Flat Side Up)'," +
				"		'Stand on a BOSU ball (flat side up) with both feet together for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'BOSU Ball'," +
				"		'Open'," +
				"		'Very Hard', " +
				"		'exercise_26'," +
				"		0.0," +
				"		0.0		);");
		
		//27 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	27," +
				"		'Tandem (Round Side Up)'," +
				"		'Stand on a BOSU ball (round side up) with chosen foot directly in front of the other, with the heel of the front foot touching the toe of the back foot, for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'BOSU Ball'," +
				"		'Open'," +
				"		'Very Hard', " +
				"		'exercise_27'," +
				"		0.0," +
				"		0.0		);");
		
		//28 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	28," +
				"		'Tandem (Flat Side Up)'," +
				"		'Stand on a BOSU ball (flat side up) with chosen foot directly in front of the other, with the heel of the front foot touching the toe of the back foot, for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'BOSU Ball'," +
				"		'Open'," +
				"		'Very Hard', " +
				"		'exercise_28'," +
				"		0.0," +
				"		0.0		);");
		
		//29 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	29," +
				"		'One Foot (Round Side Up)'," +
				"		'Stand with only chosen foot on a BOSU ball (round side up) for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'BOSU Ball'," +
				"		'Open'," +
				"		'Very Hard', " +
				"		'exercise_29'," +
				"		0.0," +
				"		0.0		);");
		
		//30 - Open
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	30," +
				"		'One Foot (Flat Side Up)'," +
				"		'Stand with only chosen foot on a a BOSU ball (flat side up) for exercise duration. Keep both eyes open and fixated at a point straight ahead.'," +
				"		'BOSU Ball'," +
				"		'Open'," +
				"		'Very Hard', " +
				"		'exercise_30'," +
				"		0.0," +
				"		0.0		);");
		
		/* EXERCISES - EYES CLOSED */
	
		//1 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	1," +
				"		'Standing'," +
				"		'Stand on the floor with both feet shoulder distance apart for exercise duration. Keep both eyes closed.'," +
				"		'Floor'," +
				"		'Closed'," +
				"		'Easy', " +
				"		'exercise_01'," +
				"		0.0," +
				"		0.0		);");
		
		//2 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	2," +
				"		'Feet Together'," +
				"		'Stand on the floor with both feet together for exercise duration. Keep both eyes closed.'," +
				"		'Floor'," +
				"		'Closed'," +
				"		'Easy', " +
				"		'exercise_02'," +
				"		0.0," +
				"		0.0		);");
		
		//3 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	3," +
				"		'Tandem'," +
				"		'Stand on the floor with the chosen foot directly in front of the other, with the heel of the front foot touching the toe of the back foot. Keep both eyes closed.'," +
				"		'Floor'," +
				"		'Closed'," +
				"		'Moderate', " +
				"		'exercise_03'," +
				"		0.0," +
				"		0.0		);");
				
		//4 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	4," +
				"		'One Foot'," +
				"		'Stand with only chosen foot on the floor for exercise duration. Keep both eyes closed.'," +
				"		'Floor'," +
				"		'Closed'," +
				"		'Moderate', " +
				"		'exercise_04'," +
				"		0.0," +
				"		0.0		);");
				
		//5 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	5," +
				"		'Front and Back'," +
				"		'Stand on a small balance board tiliting from front to back for exercise duration. Keep both eyes closed.'," +
				"		'Balance Board Small'," +
				"		'Closed'," +
				"		'Moderate', " +
				"		'exercise_05'," +
				"		0.0," +
				"		0.0		);");
		
		//6 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	6," +
				"		'Side to Side'," +
				"		'Stand on a small balance board tiliting from side to side for exercise duration. Keep both eyes closed.'," +
				"		'Balance Board Small'," +
				"		'Closed'," +
				"		'Moderate', " +
				"		'exercise_06'," +
				"		0.0," +
				"		0.0		);");
		
		//7 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	7," +
				"		'Feet Together (Front and Back)'," +
				"		'Stand on a small balance board tiliting from front to back, with both feet together, for exercise duration. Keep both eyes closed.'," +
				"		'Balance Board Small'," +
				"		'Closed'," +
				"		'Moderate', " +
				"		'exercise_07'," +
				"		0.0," +
				"		0.0		);");
		
		//8 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	8," +
				"		'Feet Together (Side to Side)'," +
				"		'Stand on a small balance board tiliting from side to side, with both feet together, for exercise duration. Keep both eyes closed.'," +
				"		'Balance Board Small'," +
				"		'Closed'," +
				"		'Moderate', " +
				"		'exercise_08'," +
				"		0.0," +
				"		0.0		);");
		
		//9 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	9," +
				"		'Tandem (Front and Back)'," +
				"		'Stand on a small balance board tiliting from front to back. Keep the chosen foot in front of the other, with the heel of the front foot touching the toe of the back foot, for exercise duration. Keep both eyes closed.'," +
				"		'Balance Board Small'," +
				"		'Closed'," +
				"		'Moderate', " +
				"		'exercise_09'," +
				"		0.0," +
				"		0.0		);");
				
		//10 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	10," +
				"		'One Foot (Front and Back)'," +
				"		'Stand on a small balance board with only chosen foot  tiliting from front to back for exercise duration. Keep both eyes closed.'," +
				"		'Balance Board Small'," +
				"		'Closed'," +
				"		'Hard', " +
				"		'exercise_10'," +
				"		0.0," +
				"		0.0		);");
				
		//11 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	11," +
				"		'One Foot (Side to Side)'," +
				"		'Stand on a small balance board with only chosen foot  tiliting from Side to Side for exercise duration. Keep both eyes closed.'," +
				"		'Balance Board Small'," +
				"		'Closed'," +
				"		'Hard', " +
				"		'exercise_11'," +
				"		0.0," +
				"		0.0		);");
		
		//12 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	12," +
				"		'Feet Apart'," +
				"		'Stand on a balance disc with both feet apart for exercise duration. Keep both eyes closed.'," +
				"		'Balance Disc'," +
				"		'Closed'," +
				"		'Hard', " +
				"		'exercise_12'," +
				"		0.0," +
				"		0.0		);");
		
		//13 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	13," +
				"		'Feet Together'," +
				"		'Stand on a balance disc with both feet together for exercise duration. Keep both eyes closed.'," +
				"		'Balance Disc'," +
				"		'Closed'," +
				"		'Hard', " +
				"		'exercise_13'," +
				"		0.0," +
				"		0.0		);");
				
		//14 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	14," +
				"		'One Foot'," +
				"		'Stand with only chosen foot on a balance disk for exercise duration. Keep both eyes closed.'," +
				"		'Balance Disc'," +
				"		'Closed'," +
				"		'Very Hard', " +
				"		'exercise_14'," +
				"		0.0," +
				"		0.0		);");
				
		//15 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	15," +
				"		'Standing'," +
				"		'Stand on a round balance board with both feet shoulder distance apart for exercise duration. Keep both eyes closed.'," +
				"		'Balance Board Round'," +
				"		'Closed'," +
				"		'Very Hard', " +
				"		'exercise_15'," +
				"		0.0," +
				"		0.0		);");
		
		//16 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	16," +
				"		'Feet Together'," +
				"		'Stand on a round balance board with both feet together for exercise duration. Keep both eyes closed.'," +
				"		'Balance Board Round'," +
				"		'Closed'," +
				"		'Very Hard', " +
				"		'exercise_16'," +
				"		0.0," +
				"		0.0		);");
		
		//17 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	17," +
				"		'Tandem'," +
				"		'Stand on a round balance board with the chosen foot in front of the other, with the heel of the front foot touching the toe of the back foot. Keep both eyes closed.'," +
				"		'Balance Board Round'," +
				"		'Closed'," +
				"		'Very Hard', " +
				"		'exercise_17'," +
				"		0.0," +
				"		0.0		);");
				
		//18 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	18," +
				"		'One Foot'," +
				"		'Stand with only chosen foot on a round balance board for exercise duration. Keep both eyes closed.'," +
				"		'Balance Board Round'," +
				"		'Closed'," +
				"		'Very Hard', " +
				"		'exercise_18'," +
				"		0.0," +
				"		0.0		);");
				
		//19 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	19," +
				"		'Standing'," +
				"		'Stand on a balance pad with both feet shoulder distance apart for exercise duration. Keep both eyes closed.'," +
				"		'Balance Pad'," +
				"		'Closed'," +
				"		'Easy', " +
				"		'exercise_19'," +
				"		0.0," +
				"		0.0		);");
		
		//20 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	20," +
				"		'Feet Together'," +
				"		'Stand on a balance pad with both feet together for exercise duration. Keep both eyes closed.'," +
				"		'Balance Pad'," +
				"		'Closed'," +
				"		'Moderate', " +
				"		'exercise_20'," +
				"		0.0," +
				"		0.0		);");
		
		//21 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	21," +
				"		'Tandem'," +
				"		'Stand on a balance pad with chosen foot directly in front of the other, with the heel of the front foot touching the toe of the back foot, for exercise duration. Keep both eyes closed.'," +
				"		'Balance Pad'," +
				"		'Closed'," +
				"		'Moderate', " +
				"		'exercise_21'," +
				"		0.0," +
				"		0.0		);");
				
		//22 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	22," +
				"		'One Foot'," +
				"		'Stand with only chosen foot on a balance pad for exercise duration. Keep both eyes closed.'," +
				"		'Balance Pad'," +
				"		'Closed'," +
				"		'Hard', " +
				"		'exercise_22'," +
				"		0.0," +
				"		0.0		);");
				
		//23 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	23," +
				"		'Standing (Round Side Up)'," +
				"		'Stand on a BOSU ball (round side up) with both feet shoulder distance apart for exercise duration. Keep both eyes closed.'," +
				"		'BOSU Ball'," +
				"		'Closed'," +
				"		'Very Hard', " +
				"		'exercise_23'," +
				"		0.0," +
				"		0.0		);");
				
		//24 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	24," +
				"		'Standing (Flat Side Up)'," +
				"		'Stand on a BOSU ball (flat side up) with both feet shoulder distance apart for exercise duration. Keep both eyes closed.'," +
				"		'BOSU Ball'," +
				"		'Closed'," +
				"		'Very Hard', " +
				"		'exercise_24'," +
				"		0.0," +
				"		0.0		);");
		
		//25 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	25," +
				"		'Feet Together (Round Side Up)'," +
				"		'Stand on a BOSU ball (round side up) with both feet together for exercise duration. Keep both eyes closed.'," +
				"		'BOSU Ball'," +
				"		'Closed'," +
				"		'Very Hard', " +
				"		'exercise_25'," +
				"		0.0," +
				"		0.0		);");
		
		//26 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	26," +
				"		'Feet Together (Flat Side Up)'," +
				"		'Stand on a BOSU ball (flat side up) with both feet together for exercise duration. Keep both eyes closed.'," +
				"		'BOSU Ball'," +
				"		'Closed'," +
				"		'Very Hard', " +
				"		'exercise_26'," +
				"		0.0," +
				"		0.0		);");
		
		//27 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	27," +
				"		'Tandem (Round Side Up)'," +
				"		'Stand on a BOSU ball (round side up) with chosen foot directly in front of the other, with the heel of the front foot touching the toe of the back foot, for exercise duration. Keep both eyes closed.'," +
				"		'BOSU Ball'," +
				"		'Closed'," +
				"		'Very Hard', " +
				"		'exercise_27'," +
				"		0.0," +
				"		0.0		);");
		
		//28 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	28," +
				"		'Tandem (Flat Side Up)'," +
				"		'Stand on a BOSU ball (flat side up) with chosen foot directly in front of the other, with the heel of the front foot touching the toe of the back foot, for exercise duration. Keep both eyes closed.'," +
				"		'BOSU Ball'," +
				"		'Closed'," +
				"		'Very Hard', " +
				"		'exercise_28'," +
				"		0.0," +
				"		0.0		);");
				
		//29 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	29," +
				"		'One Foot (Round Side Up)'," +
				"		'Stand with only chosen foot on a BOSU ball (round side up) for exercise duration. Keep both eyes closed.'," +
				"		'BOSU Ball'," +
				"		'Closed'," +
				"		'Very Hard', " +
				"		'exercise_29'," +
				"		0.0," +
				"		0.0		);");
		
		//30 - Closed
		db.execSQL("INSERT INTO exercises "
				+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
				+ "(	30," +
				"		'One Foot (Flat Side Up)'," +
				"		'Stand with only chosen foot on a a BOSU ball (flat side up) for exercise duration. Keep both eyes closed.'," +
				"		'BOSU Ball'," +
				"		'Closed'," +
				"		'Very Hard', " +
				"		'exercise_30'," +
				"		0.0," +
				"		0.0		);");
		
		// in debug mode, populate the database with a set of additional 
		// exercises (for clinical use)
		if(BuildConfig.DEBUG) {
			
			//1 - DEBUG, Open
			db.execSQL("INSERT INTO exercises "
					+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
					+ "(	101," +
					"		'Exercise 1'," +
					"		''," +
					"		''," +
					"		'Open'," +
					"		'', " +
					"		'myankle_icon'," +
					"		0.0," +
					"		0.0		);");
			
			//2 - DEBUG, Open
			db.execSQL("INSERT INTO exercises "
					+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
					+ "(	102," +
					"		'Exercise 2'," +
					"		''," +
					"		''," +
					"		'Open'," +
					"		'', " +
					"		'myankle_icon'," +
					"		0.0," +
					"		0.0		);");
			
			//3 - DEBUG, Open
			db.execSQL("INSERT INTO exercises "
					+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
					+ "(	103," +
					"		'Exercise 3'," +
					"		''," +
					"		''," +
					"		'Open'," +
					"		'', " +
					"		'myankle_icon'," +
					"		0.0," +
					"		0.0		);");
			
			//4 - DEBUG, Open
			db.execSQL("INSERT INTO exercises "
					+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
					+ "(	104," +
					"		'Exercise 4'," +
					"		''," +
					"		''," +
					"		'Open'," +
					"		'', " +
					"		'myankle_icon'," +
					"		0.0," +
					"		0.0		);");
			
			//5 - DEBUG, Open
			db.execSQL("INSERT INTO exercises "
					+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
					+ "(	105," +
					"		'Exercise 5'," +
					"		''," +
					"		''," +
					"		'Open'," +
					"		'', " +
					"		'myankle_icon'," +
					"		0.0," +
					"		0.0		);");
			
			//6 - DEBUG, Open
			db.execSQL("INSERT INTO exercises "
					+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
					+ "(	106," +
					"		'Exercise 6'," +
					"		''," +
					"		''," +
					"		'Open'," +
					"		'', " +
					"		'myankle_icon'," +
					"		0.0," +
					"		0.0		);");
			
			//7 - DEBUG, Open
			db.execSQL("INSERT INTO exercises "
					+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
					+ "(	107," +
					"		'Exercise 7'," +
					"		''," +
					"		''," +
					"		'Open'," +
					"		'', " +
					"		'myankle_icon'," +
					"		0.0," +
					"		0.0		);");
			
			//8 - DEBUG, Open
			db.execSQL("INSERT INTO exercises "
					+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
					+ "(	108," +
					"		'Exercise 8'," +
					"		''," +
					"		''," +
					"		'Open'," +
					"		'', " +
					"		'myankle_icon'," +
					"		0.0," +
					"		0.0		);");
			
			//9 - DEBUG, Open
			db.execSQL("INSERT INTO exercises "
					+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
					+ "(	109," +
					"		'Exercise 9'," +
					"		''," +
					"		''," +
					"		'Open'," +
					"		'', " +
					"		'myankle_icon'," +
					"		0.0," +
					"		0.0		);");
			
			//10 - DEBUG, Open
			db.execSQL("INSERT INTO exercises "
					+ "(position, name, instruction, equipment, eyeState, difficulty, picture, upgradeThreshold, downgradeThreshold) VALUES "
					+ "(	110," +
					"		'Exercise 10'," +
					"		''," +
					"		''," +
					"		'Open'," +
					"		'', " +
					"		'myankle_icon'," +
					"		0.0," +
					"		0.0		);");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		// do upgrade from version 1,2,3 to 4
		if (oldVersion < 4){
			// remove all previous tables if they exist
			db.execSQL("DROP TABLE IF EXISTS users");
			db.execSQL("DROP TABLE IF EXISTS exercises");
			db.execSQL("DROP TABLE IF EXISTS sessions");
			db.execSQL("DROP TABLE IF EXISTS injuries");
			
			// recreate the database
			onCreate(db);
		}	
		
		if(oldVersion < 5){
			
		}

	}

}