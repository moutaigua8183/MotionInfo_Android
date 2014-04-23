package mou.MotionModel;
 
import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
 
public class ReceiveThread extends Thread {

	
	private BluetoothSocket bluetoothSocket = null;
    private InputStream MYINPUTSTREAM = null;
    private Handler receiveThread_Handler = null;
	private byte[] RECEIVED_BYTE = new byte[4];
 
	
	
	public ReceiveThread(Handler handler, BluetoothSocket socket) {
		this.receiveThread_Handler = handler;
		this.bluetoothSocket  = socket;
	}
 
	@SuppressLint("NewApi")
	public void run() 
	{
		InputStream tmpIn = null;
        // Get the input and output streams, using temp objects because
        // member streams are final
        try 
        {
            tmpIn = bluetoothSocket.getInputStream();
        } 
        catch (IOException e) { }
        MYINPUTSTREAM = tmpIn;	       
        // Keep listening to the InputStream until an exception occurs
        int byteReceived; // bytes returned from read()
        while (true) 
        {
            try 
            {
            	if ( bluetoothSocket.isConnected() == true )
            	{
	            	// Read from the InputStream
	            	byteReceived = MYINPUTSTREAM.read(RECEIVED_BYTE);
	            	if ( byteReceived>0 )
	            	{			            		
		            	Message msg = new Message();
			    	    msg.what= (int)RECEIVED_BYTE[0]-48;			//BYTE里存的是ACKII对应的十进制的数，'1'对应49
			    	    receiveThread_Handler.sendMessage(msg);
	            	}
            	}
            	else
            	{
            		Message msg =new Message();
		    	    msg.what = 100;
		    	    receiveThread_Handler.sendMessage(msg);
            		break;
            	}
            }
            catch (IOException e) 
            {
                break;
            }
        }
    }
}
