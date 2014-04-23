package mou.MotionModel;

import java.util.ArrayList;
import java.util.HashMap;
import android.support.v7.app.ActionBarActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import mou.MotionModel.database;
import mou.MotionModel.databaseHelper;

public class History extends ActionBarActivity {
	
	private databaseHelper myHelper = null;
	private SQLiteDatabase db = null;
	private database mydb = new database(this);
	private Cursor dataCur = null;
	private ListView HistoryList = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_history);
       
       //Create database
       mydb.CreateDatabase(this, databaseHelper.DATABASE_NAME);
       mydb.CreateTable();
       mydb.OpenDb();
       //list loads data
       history_data_load();
       //close database
       mydb.CloseDb();     
	}
	
	
   	private void history_data_load()
   	{
    	HistoryList = (ListView) findViewById(R.id.history_list);
    	ArrayList<HashMap<String,Object>> datalist=new ArrayList<HashMap<String,Object>>();
    	SimpleAdapter adapter = new SimpleAdapter(this,datalist,
                R.layout.history_list_item,      
                new String[] {"history_item_time","history_item_status"},   
                new int[] {R.id.history_item_time,R.id.history_item_status}  
            ); 
    	datalist.clear();
    	HashMap<String, Object> temp_mMap = new HashMap<String, Object>();
    	temp_mMap.put("history_item_time", "时间"); 
        temp_mMap.put("history_item_status", "运动状态"); 
        datalist.add(temp_mMap);
    	dataCur = mydb.PickFromTable();
    	int i = dataCur.getCount();
        if ( i == 0 )
        {
        	HashMap<String, Object> mMap = new HashMap<String, Object>();
        	mMap.put("history_item_time", "No Data"); 
            mMap.put("history_item_status", "Null"); 
            datalist.add(mMap);
        }
        else
        {
	        for ( dataCur.moveToFirst();!dataCur.isAfterLast();dataCur.moveToNext())
	        {    	
	        	HashMap<String, Object> mMap = new HashMap<String, Object>();
	        	String data_time =
	        			String.valueOf(dataCur.getString(dataCur.getColumnIndex(database.DATA_YEAR)))
	        			+ '.'
	        			+ String.valueOf(dataCur.getString(dataCur.getColumnIndex(database.DATA_MONTH)))
	        			+ '.' 
	        			+ String.valueOf(dataCur.getString(dataCur.getColumnIndex(database.DATA_DAY)))
	        			+ "  "
	        			+ String.valueOf(dataCur.getString(dataCur.getColumnIndex(database.DATA_HOUR)))
	        			+ ':'
	        			+ String.valueOf(dataCur.getString(dataCur.getColumnIndex(database.DATA_MINUTE)));
	        	mMap.put("history_item_time",  data_time );
	        	String data_status = dataCur.getString(dataCur.getColumnIndex(database.DATA_STATUS));
	        	mMap.put("history_item_status",  data_status );
	        	datalist.add(mMap);
	        }
        }  
	    HistoryList.setAdapter(adapter);//把arrayadapter和listview绑定
	    //adapter.notifyDataSetChanged();
        HistoryList.setSelection(HistoryList.getCount()-1);
   	}
   
    
    
	public void onClick(View v)
	{
	}

   @Override
   protected void onDestroy() {
	   if(db!=null)
	   {
		   db.close();
	   }
	   if(myHelper!=null)
	   {
		   myHelper.close();
	   }
	   super.onDestroy();
   }

}
