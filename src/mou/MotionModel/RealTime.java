package mou.MotionModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import mou.MotionModel.database;
import mou.MotionModel.ReceiveThread;

@SuppressLint("HandlerLeak")
public class RealTime extends ActionBarActivity {
	
	
	/**************************��ǰʱ��**********************************/
	private Time current_time;
	private Timer update_time_timer;
	private TimerTask update_time_task;
	private Handler timer_handler = null;  
    private TextView current_time_text = null;
    private String current_time_str = null;
    /**************************���ݿ�**********************************/
	private database mydb = new database(this);
	private Cursor realtime_dataCur = null;
	private ListView RealTimeList = null;

	/****************************����************************************/
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;
    private static String ADDRESS = null;
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = null;
    public static final String TOAST = "toast";
    private TextView status_text = null;
    				/***���������߳�**/
    private static final String RECEIVETHREAD_NAME = "ReceiveThread";
    private ReceiveThread receiveThread;
    private Handler receiveThread_Handler = null;
    //private Runnable Reading_Data_Runnable = null;
	
	
    
    @Override
   public void onCreate(Bundle savedInstanceState) 
   {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_realtime);
       
       /***********************��ʾϵͳʱ���*******************************/
       int task_delay_sec = 0;
       current_time_text = (TextView)this.findViewById(R.id.current_time);       
       current_time = new Time();
       current_time.setToNow(); // ȡ��ϵͳʱ�䡣
       int second = current_time.second;
       task_delay_sec = 60-second;
       int hour = current_time.hour;
	   int minute = current_time.minute;
	   if(minute<10&&hour<10)
	   {
		   current_time_str= String.valueOf(hour)+":0"+String.valueOf(minute);
	   }
	   else if(minute<10&&hour>=10)
	   {
		   current_time_str= String.valueOf(hour)+":0"+String.valueOf(minute);
	   }
	   else if(minute>=10&&hour<10)
	   {
		   current_time_str= '0'+String.valueOf(hour)+":"+String.valueOf(minute);
	   }
	   else
	   {
		   current_time_str= String.valueOf(hour)+':'+String.valueOf(minute);
	   }	
	   current_time_text.setText(current_time_str);
       update_time_timer = new Timer(true);
       update_time_task = new TimerTask(){  
    	      public void run() 
    	      {  
    	    	  current_time.setToNow();
    	    	  int hour = current_time.hour;
	    	      int minute = current_time.minute;
	    	      if(minute<10&&hour<10)
	    		   {
	    			   current_time_str= String.valueOf(hour)+":0"+String.valueOf(minute);
	    		   }
	    		   else if(minute<10&&hour>=10)
	    		   {
	    			   current_time_str= String.valueOf(hour)+":0"+String.valueOf(minute);
	    		   }
	    		   else if(minute>=10&&hour<10)
	    		   {
	    			   current_time_str= '0'+String.valueOf(hour)+":"+String.valueOf(minute);
	    		   }
	    		   else
	    		   {
	    			   current_time_str= String.valueOf(hour)+':'+String.valueOf(minute);
	    		   }	
	    	      Message msg =new Message();
	    	      msg.what= 1;
	    	      timer_handler.sendMessage(msg);
    	      }
       };      
       timer_handler = new Handler(){
    	   @Override
   			public void handleMessage(Message msg)
	   		{
	   			switch(msg.what)
	   			{
	   			case 1:
	   				current_time_text.setText(current_time_str);
	   				break;
	   			}
	   			super.handleMessage(msg);
	   		}
   		};
   		update_time_timer.schedule(update_time_task,task_delay_sec*1000, 60000); //��ʱһ��ʱ���ִ�У�60sִ��һ��
   		
   		/**********************************************************************/
       
   		/**********************���ݼ�¼���ݿ�д���*****************************/
   		//Create database
        mydb.CreateDatabase(this, databaseHelper.DATABASE_NAME);
        mydb.CreateTable();
        mydb.CreateTempTable();
        mydb.OpenDb();
        Init_RealTime_List();
        //RecordInRealTime("00","55", "��");
   		
   		/**********************************************************************/       
        
        /******************************����ͨ�ſ�*******************************/
        status_text = (TextView)this.findViewById(R.id.motion_status); 
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) 
        {
        	Toast.makeText(this, "����������", Toast.LENGTH_LONG).show();
        	finish();
        	return;
        }
        if ( !bluetoothAdapter.isEnabled() )
        { 
        	Intent myIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        	myIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30);
        	startActivityForResult(myIntent,1);       	
        }
        else
		{
				final Intent i2 = new Intent(this, BluetoothDeviceList.class);
				startActivityForResult(i2,2);
		}
        
        /**********************************************************************/
    }
    
    
   
   
  //�������ӽ��
  	public void onActivityResult(int requestCode, int resultCode, Intent data) 
  	{
  		switch(requestCode)
  		{
  		case 1: //�������ܽ��
  			if ( resultCode != Activity.RESULT_CANCELED )
			{
  				final Intent i2 = new Intent(this, BluetoothDeviceList.class);
  				startActivityForResult(i2,2);
			}
  			else
  			{
  				Toast.makeText(this, "����δ��", Toast.LENGTH_LONG).show();
  	        	finish();
  			}
  			break;
  		case 2:	//�豸��Ѱ���
  			if ( resultCode != Activity.RESULT_CANCELED )
  			{
  	  			ADDRESS = data.getExtras().getString("target_address");	  			
  	  			BluetoothClient();
  	  		}
  			else
  			{
  				Toast.makeText(this, "��Ѱ�豸ʧ��", Toast.LENGTH_LONG).show();
  			}
  			break;
  		}		
  	}
  	
  	
  	
  	
  	
    public void onClick(View v)
    {
    	
    }

    
    @Override
    protected void onDestroy() 
    {
       super.onDestroy();
       Record2Database("�����ж�");
       update_time_timer.cancel();
       mydb.CloseDb();
       mydb.DeleteTable();
       //if (bluetoothAdapter.isEnabled() )
       //   	bluetoothAdapter.disable();
       try 
       {
    	   bluetoothSocket.close();
       } 
       catch (IOException e) 
       {
    	   e.printStackTrace();
       }
       
    }
   
   
    
   /*****************************�Զ��庯��*********************************/
    	/************************��ʼ��List****************************/
    private void Init_RealTime_List()
    {
    	
    	RealTimeList = (ListView) findViewById(R.id.realtime_list); 
    	ArrayList<HashMap<String,Object>> realtime_datalist=new ArrayList<HashMap<String,Object>>();
    	SimpleAdapter adapter = new SimpleAdapter(this,realtime_datalist,
                R.layout.realtime_list_item,      
                new String[] {"realtime_item_time","realtime_item_status"},   
                new int[] {R.id.realtime_item_time,R.id.realtime_item_status}  
            ); 
    	realtime_datalist.clear();
    	HashMap<String, Object> mMap = new HashMap<String, Object>();
    	mMap.put("realtime_item_time",  "ʱ��");
 	   	mMap.put("realtime_item_status",  "�˶�״̬" );
 	   	realtime_datalist.add(mMap);
 	   	RealTimeList.setAdapter(adapter);//��arrayadapter��listview��
    }
    
    	/**********************��¼��MM_temp������List**********************/
    private void update_list(String strHour, String strMinute, String strStatus)
   	{
    	mydb.InsertData2Temp(strHour, strMinute, strStatus);
    	RealTimeList = (ListView) findViewById(R.id.realtime_list);
    	ArrayList<HashMap<String,Object>> realtime_datalist=new ArrayList<HashMap<String,Object>>();
    	SimpleAdapter adapter = new SimpleAdapter(this,realtime_datalist,
                R.layout.realtime_list_item,      
                new String[] {"realtime_item_time","realtime_item_status"},   
                new int[] {R.id.realtime_item_time,R.id.realtime_item_status}  
            ); 
    	realtime_datalist.clear();
    	HashMap<String, Object> temp_mMap = new HashMap<String, Object>();
    	temp_mMap.put("realtime_item_time", "ʱ��"); 
        temp_mMap.put("realtime_item_status", "�˶�״̬"); 
        realtime_datalist.add(temp_mMap);
    	realtime_dataCur = mydb.PickFromTempTable();
        for ( realtime_dataCur.moveToFirst();!realtime_dataCur.isAfterLast();realtime_dataCur.moveToNext())
        {    	
        	HashMap<String, Object> mMap1 = new HashMap<String, Object>();
        	String data_time =
        			realtime_dataCur.getString(realtime_dataCur.getColumnIndex(database.DATA_HOUR))
        			+ ':'
        			+ realtime_dataCur.getString(realtime_dataCur.getColumnIndex(database.DATA_MINUTE));
        	mMap1.put("realtime_item_time",  data_time );
        	String data_status = realtime_dataCur.getString(realtime_dataCur.getColumnIndex(database.DATA_STATUS));
        	mMap1.put("realtime_item_status",  data_status );
        	realtime_datalist.add(mMap1);
        } 
	    RealTimeList.setAdapter(adapter);//��arrayadapter��listview��
	    RealTimeList.setSelection(RealTimeList.getCount()-1);
   	}
     	/*************************��¼��MM_history************************/
   private void Record2Database(String temp_status)
   {
	   mydb.OpenDb();
	   current_time.setToNow();
	   int year = current_time.year;
	   int month = current_time.month + 1; //0Ϊ1��
	   int day = current_time.monthDay;
	   int hour = current_time.hour;
	   int minute = current_time.minute;
	   mydb.InsertData(year, month, day, hour, minute, temp_status);
       mydb.CloseDb();   
   }
   
     	/*************************��¼��Database*****************************/
   private void RecordInRealTime(String temp_hour, String temp_minute, String temp_status)
   {
	   update_list(temp_hour, temp_minute, temp_status);
	   Record2Database(temp_status);   
   }
   		/*************************�����ͻ��˹���************************/
   @SuppressLint("NewApi")
private void BluetoothClient()
   {
	   BluetoothDevice temp_Device = bluetoothAdapter.getRemoteDevice(ADDRESS);	
	   if ( temp_Device!=null )
	   {
			try 
			{
		        // MY_UUID is the app's UUID string, also used by the server code
				bluetoothSocket = temp_Device.createRfcommSocketToServiceRecord(MY_UUID);
			} 
			catch (IOException e) { }
			try 
			{
			    // Connect the device through the socket. This will block
			    // until it succeeds or throws an exception
			    bluetoothSocket.connect();
			} 
			catch (IOException connectException) 
			{
			    // Unable to connect; close the socket and get out
			    try 
			    {
			        bluetoothSocket.close();
			        Toast.makeText(this, "���ӳ�������Է��豸�Ƿ���", Toast.LENGTH_LONG).show();
			        return;
			    } 
			    catch (IOException closeException) { }
			}
			if ( bluetoothSocket.isConnected() )
			{
				Toast.makeText(this, "���ӳɹ�", Toast.LENGTH_LONG).show();
				receiveThread_Handler = new Handler(){
					@Override
		   			public void handleMessage(Message msg)
			   		{
			   			int hour = current_time.hour;
			   			int minute = current_time.minute;
						switch(msg.what)
			   			{
			   			case 1:
			   				status_text.setText("վ��");
			   				current_time.setToNow();	   				
			   				RecordInRealTime( String.valueOf(hour), String.valueOf(minute), "վ��" );
			   				break;
			   			case 2:
			   				status_text.setText("ƽ��");
			   				current_time.setToNow();
			   				RecordInRealTime( String.valueOf(hour), String.valueOf(minute), "ƽ��");
			   				break;
			   			case 3:
			   				status_text.setText("�п�");
			   				current_time.setToNow();
			   				RecordInRealTime( String.valueOf(hour), String.valueOf(minute), "�п�");
			   				break;
			   			case 4:
			   				status_text.setText("����");
			   				current_time.setToNow();
			   				RecordInRealTime( String.valueOf(hour), String.valueOf(minute), "����");
			   				break;
			   			case 5:
			   				status_text.setText("�ܲ�");			   				
			   				current_time.setToNow();
			   				RecordInRealTime( String.valueOf(hour), String.valueOf(minute), "�ܲ�");
			   				break;
			   			case 8:
			   				status_text.setText("ˤ��");			   				
			   				current_time.setToNow();
			   				RecordInRealTime( String.valueOf(hour), String.valueOf(minute), "ˤ��");
			   				break;
			   			case 100:
			   				status_text.setText("�����ж�");
			   				current_time.setToNow();
			   				RecordInRealTime( String.valueOf(hour), String.valueOf(minute), "�����ж�");
			   				break;
			   			case 101:
			   				status_text.setText("���ӳɹ�");
			   				current_time.setToNow();
			   				RecordInRealTime( String.valueOf(hour), String.valueOf(minute), "���ӳɹ�");
			   				break;
			   			}
			   			super.handleMessage(msg);
			   		}
				};
				receiveThread = new ReceiveThread(receiveThread_Handler, bluetoothSocket);
  	  			receiveThread.setName(RECEIVETHREAD_NAME);
				receiveThread.start();
				Message msg =new Message();
	    	    msg.what= 101;			//���ӳɹ�
	    	    receiveThread_Handler.sendMessage(msg);
			}	
	   }
	   else
	   {
		   Toast.makeText(this, "�Է��豸���ʧ��", Toast.LENGTH_LONG).show();
	   }
		   
   }
			  
   
	
   

   
   

}
