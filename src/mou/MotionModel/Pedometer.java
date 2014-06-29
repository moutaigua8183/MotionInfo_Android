package mou.MotionModel;

import java.io.IOException;
import java.io.OutputStream;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import mou.MotionModel.ReceiveThread;

@SuppressLint("HandlerLeak")
public class Pedometer extends ActionBarActivity {
	
	
	/**************************当前时间**********************************/
	private Time current_time;
	private Timer update_time_timer;
	private TimerTask update_time_task;
	private Handler timer_handler = null;  
    private TextView current_time_text = null;
    private String current_time_str = null;

	/****************************蓝牙************************************/
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
    private TextView number_text = null;
    private int WALK_STEPS=0;
    private int RUN_STEPS=0;
    				/***蓝牙接收线程**/
    private static final String RECEIVETHREAD_NAME = "ReceiveThread";
    private ReceiveThread receiveThread;
    private Handler receiveThread_Handler = null;
					/***蓝牙发送变量**/
	private static OutputStream MYOUTPUTSTREAM;
    //private Runnable Reading_Data_Runnable = null;
	
	
    
    @Override
   public void onCreate(Bundle savedInstanceState) 
   {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_pedometer);
       
       /***********************显示系统时间块*******************************/
       int task_delay_sec = 0;
       current_time_text = (TextView)this.findViewById(R.id.current_time);       
       current_time = new Time();
       current_time.setToNow(); // 取得系统时间。
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
   		update_time_timer.schedule(update_time_task,task_delay_sec*1000, 60000); //延时一定时间后执行，60s执行一次
   		
   		/**********************************************************************/
   		
        
        /******************************蓝牙通信块*******************************/
        status_text = (TextView)this.findViewById(R.id.pedometer_motion_status);
        number_text = (TextView)this.findViewById(R.id.step_number);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) 
        {
        	Toast.makeText(this, "蓝牙不可用", Toast.LENGTH_LONG).show();
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
    
    
   
   
  //蓝牙连接结果
  	public void onActivityResult(int requestCode, int resultCode, Intent data) 
  	{
  		switch(requestCode)
  		{
  		case 1: //蓝牙是能结果
  			if ( resultCode != Activity.RESULT_CANCELED )
			{
  				final Intent i2 = new Intent(this, BluetoothDeviceList.class);
  				startActivityForResult(i2,2);
			}
  			else
  			{
  				Toast.makeText(this, "蓝牙未打开", Toast.LENGTH_LONG).show();
  	        	finish();
  			}
  			break;
  		case 2:	//设备搜寻结果
  			if ( resultCode != Activity.RESULT_CANCELED )
  			{
  	  			ADDRESS = data.getExtras().getString("target_address");	  			
  	  			BluetoothClient();
  	  		}
  			else
  			{
  				Toast.makeText(this, "搜寻设备失败", Toast.LENGTH_LONG).show();
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
       update_time_timer.cancel();
       //if (bluetoothAdapter.isEnabled() )
       //   	bluetoothAdapter.disable();
       if( MYOUTPUTSTREAM!=null )
       {
	       try {
				MYOUTPUTSTREAM.write('9');//告诉单片机功能退出
			} catch (IOException e) 
			{ }	
       }
       try 
       {
    	   bluetoothSocket.close();
       } 
       catch (IOException e) 
       {
    	   e.printStackTrace();
       }
       
    }
   
   
    
   /*****************************自定义函数*********************************/
    	
   		/*************************蓝牙客户端构建************************/
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
			        Toast.makeText(this, "连接出错，请检查对方设备是否开启", Toast.LENGTH_LONG).show();
			        return;
			    } 
			    catch (IOException closeException) { }
			}
			if ( bluetoothSocket.isConnected() )
			{
				Toast.makeText(this, "连接成功", Toast.LENGTH_LONG).show();
				//输出准备
				OutputStream tmpOut = null;
				try {
					tmpOut = bluetoothSocket.getOutputStream();
				} catch (IOException e) { }
				MYOUTPUTSTREAM = tmpOut;
				try {
					MYOUTPUTSTREAM.write('3');//告诉单片机这是计步器
				} catch (IOException e) 
				{ }		
				receiveThread_Handler = new Handler(){
					@Override
		   			public void handleMessage(Message msg)
			   		{
						switch(msg.what)
			   			{
			   			case 4:
			   				WALK_STEPS=WALK_STEPS+1;				
			   				status_text.setText("步行");
		   					number_text.setText(String.valueOf(WALK_STEPS));
			   				break;
			   			case 5:
			   				RUN_STEPS=RUN_STEPS+1;		   				
			   				status_text.setText("跑步");
		   					number_text.setText(String.valueOf(RUN_STEPS));
			   				break;
			   			case 27:
			   				status_text.setText("初始化完成");	
			   			}
			   			super.handleMessage(msg);
			   		}
				};
				receiveThread = new ReceiveThread(receiveThread_Handler, bluetoothSocket);
  	  			receiveThread.setName(RECEIVETHREAD_NAME);
				receiveThread.start();
				Message msg =new Message();
	    	    msg.what= 101;			//连接成功
	    	    receiveThread_Handler.sendMessage(msg);
			}	
	   }
	   else
	   {
		   Toast.makeText(this, "对方设备获得失败", Toast.LENGTH_LONG).show();
	   }
		   
   }
			  
   
	
   

   
   

}
