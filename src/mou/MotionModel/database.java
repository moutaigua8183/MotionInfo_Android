package mou.MotionModel;

import android.content.Context;
import android.database.*;
import android.database.sqlite.*;
import android.util.Log;
import mou.MotionModel.databaseHelper;

public class database{

	//变量名申明
	private static final String DATABASE_TABLE = "MM_History";
	private static final String DATABASE_TABLE_TEMP = "MM_Temp";
	public final static String DATA_YEAR = "Year";
	public final static String DATA_MONTH = "Month";
	public final static String DATA_DAY = "Day";
	public final static String DATA_HOUR = "Hour";
	public final static String DATA_MINUTE = "Minute";
	public final static String DATA_STATUS = "Status";

	databaseHelper myHelper = null;
	private SQLiteDatabase db = null;
	private final Context context;
	
	//新建一个数据库
	public SQLiteDatabase CreateDatabase(Context context,String dbName)
	{
		myHelper = new databaseHelper(context, dbName);
		return myHelper.getWritableDatabase();
	}
	//新建TABLE1
	public void CreateTable()
	{
		db = myHelper.getWritableDatabase();
		try 
		{
			db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE + " (" 
				+ DATA_YEAR + " int not null," 
				+ DATA_MONTH + " int not null," 
				+ DATA_DAY + " int not null,"
				+ DATA_HOUR+ " int not null,"
				+ DATA_MINUTE + " int not null,"
				+ DATA_STATUS + " text not null);" 
				);
		} 
		catch (SQLException e) 
		{
			Log.i("err", "create history table failed" );
		}
	}	
	
	//新建TABLE1
	public void CreateTempTable()
	{
		db = myHelper.getWritableDatabase();
		try 
		{
			db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_TEMP + " (" 
				+ DATA_HOUR+ " text not null,"
				+ DATA_MINUTE + " text not null,"
				+ DATA_STATUS + " text not null);" 
				);
		} 
		catch (SQLException e) 
		{
			Log.i("err", "create temporary table failed" );
		}
	}	
		
	
	//插入数据到TABLE1
	public void InsertData(int year, 
						   int month, 
						   int day, 
						   int hour, 
						   int minute,
						   String status)
	{
		db = myHelper.getWritableDatabase();
		String sql = "insert into " + DATABASE_TABLE + " values ("
					+ String.valueOf(year) + "," + String.valueOf(month) + "," + String.valueOf(day) + "," 
					+ String.valueOf(hour) + "," + String.valueOf(minute) + ",'" + String.valueOf(status) 
					+ "');";
		try 
		{
			db.execSQL(sql);
		} 
		catch (SQLException e) 
		{
			Log.i("err", "insert data failed");
		}
	}
	
	//插入数据到TABLE2
	public void InsertData2Temp(String strhour, String strminute, String strstatus)
	{
		db = myHelper.getWritableDatabase();
		String sql = "insert into " + DATABASE_TABLE_TEMP + " values ('"
					+ strhour + "','" + strminute + "','" + strstatus + "');";
		try 
		{
			db.execSQL(sql);
		} 
		catch (SQLException e) 
		{
			Log.i("err", "insert data to temp failed");
		}
	}
	
	//读取数据
	public Cursor PickFromTable()
	{
		/*return db.query(DATABASE_TABLE, null,
						DATA_YEAR + "=" + String.valueOf(filter_year) + "and" 
						+ DATA_MONTH + "=" + String.valueOf(filter_month) + "and"
						+ DATA_DAY + "=" + String.valueOf(filter_day),
						null, null, null, null, null);*/
		return db.query(DATABASE_TABLE, null,
				null,
				null, null, null, null, null);
	}
	
	//读取数据
	public Cursor PickFromTempTable()
	{
		/*return db.query(DATABASE_TABLE, null,
						DATA_YEAR + "=" + String.valueOf(filter_year) + "and" 
						+ DATA_MONTH + "=" + String.valueOf(filter_month) + "and"
						+ DATA_DAY + "=" + String.valueOf(filter_day),
						null, null, null, null, null);*/
		return db.query(DATABASE_TABLE_TEMP, null,
				null,
				null, null, null, null, null);
	}
	
	//删除表
	public void DeleteTable()
	{
		db = myHelper.getWritableDatabase();
		String sql = "drop table '" + DATABASE_TABLE_TEMP + "';";
		try 
		{
			db.execSQL(sql);
		} 
		catch (SQLException e) 
		{
			Log.i("err", "Delete temp_table failed");
		}
	}
	
	
	public database(Context _context) 
	{
		this.context = _context;
		myHelper = new databaseHelper(context, databaseHelper.DATABASE_NAME, null, databaseHelper.DATABASE_VERSION);
	}

	//打开数据库
	public void OpenDb() throws SQLiteException 
	{
		try 
		{
			db = myHelper.getWritableDatabase();
		} 
		catch (SQLiteException ex) 
		{
			db = myHelper.getReadableDatabase();
		}
	}

	//关闭数据库
	public void CloseDb()
	{
		myHelper.close();
	}

}