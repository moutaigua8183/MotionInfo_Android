package mou.MotionModel;

import android.content.Context;
import android.database.sqlite.*;
import android.database.sqlite.SQLiteDatabase.CursorFactory;



public class databaseHelper extends SQLiteOpenHelper{
	
	public databaseHelper(Context context, String name, CursorFactory factory,int version) {
		super(context, name, factory, version);
		}
	public databaseHelper(Context context,String name,int version){
		 this(context,name,null,version);
		}
	public databaseHelper(Context context,String name){
		this(context,name,DATABASE_VERSION);
		}
	//变量名申明
	public static final String DATABASE_NAME = "HistoryData.db";
	public static final String DATABASE_TABLE = "MM_History";
	private static final String DATABASE_TABLE_TEMP = "MM_Temp";
	public static final int DATABASE_VERSION = 2;
	public final static String DATA_YEAR = "Year";
	public final static String DATA_MONTH = "Month";
	public final static String DATA_DAY = "Day";
	public final static String DATA_HOUR = "Hour";
	public final static String DATA_MINUTE = "Minute";
	public final static String DATA_STATUS = "Status";
	
	

    @Override
    public void onCreate(SQLiteDatabase db) {
    /*db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE1 + " (" 
    			+ USER_NAME + " text not null," 
    			+ USER_YEAR + " int not null," 
    			+ USER_MONTH + " int not null," 
    			+ USER_DAY + " int not null);" );
    db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE2 + " ("   		
    		+ USER_NAME + " text not null," 
			+ USER_AGE + " int not null," 
			+ USER_HISTORY + " text not null,"
			+ USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT);" );*/
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	db.execSQL("DROP TABLE IF EXITSTS " + DATABASE_TABLE);
    	db.execSQL("DROP TABLE IF EXITSTS " + DATABASE_TABLE_TEMP);
    	onCreate(db);
    }

		
  
}