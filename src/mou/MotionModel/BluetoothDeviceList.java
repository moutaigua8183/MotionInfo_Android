package mou.MotionModel;

import java.lang.reflect.Method;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public class BluetoothDeviceList extends Activity {
    // Debugging
    private static final String TAG = "DeviceList"; 
    private static final String PIN = "1234";
    private static final boolean D = true;
    // Member fields
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;


    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.search_device_list);

        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
                mNewDevicesArrayAdapter.clear();
            }
        });
        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.devicename);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.devicename);

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mPairedDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mNewDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        // Register for broadcasts when discovery has finished
        filter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
        
        
        
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        // Turn on sub-title for new devices
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
   
    }
    
    
    //SetPin
    static public boolean setPin(Class<?> btClass, BluetoothDevice btDevice,String strPin) throws Exception
    {
    	try
    	{
    		Method setPinMethod = btClass.getClass().getDeclaredMethod("setPin",byte[].class);
			setPinMethod.invoke(btDevice, strPin.getBytes());
	        Log.d(TAG, "Success to add the PIN.");
	        try 
	        {
	              btDevice.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(btDevice, true);
	              Log.d(TAG, "Success to setPairingConfirmation.");
	        } 
	        catch (Exception e) 
	        {
	        	// TODO Auto-generated catch block
	            Log.e(TAG, e.getMessage());
	            e.printStackTrace();
	        } 
    	}
    	catch (Exception e) 
    	{
	        Log.e(TAG, e.getMessage());
	        e.printStackTrace();
    	}
		return true;
    }
    //取消用户输入
    static public boolean cancelPairingUserInput(Class<?> btClass, BluetoothDevice device) throws Exception
    {
    	Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
    	//cancelBondProcess();
    	Boolean returnValue = (Boolean) createBondMethod.invoke(device);
    	return returnValue.booleanValue();
    }

    //CreateBond
    static public boolean createBond(Class<?> btClass,BluetoothDevice btDevice) throws Exception 
    {  
        Method createBondMethod = btClass.getMethod("createBond");  
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);  
        return returnValue.booleanValue();  
    } 
    // The on-click listener for New devices in the ListViews
    private OnItemClickListener mNewDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();           
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            
            BluetoothDevice temp_btDevice = mBtAdapter.getRemoteDevice(address);      
            try 
            {  
            	setPin(temp_btDevice.getClass(), temp_btDevice, PIN);
            	createBond(temp_btDevice.getClass(), temp_btDevice);
            	mPairedDevicesArrayAdapter.add(temp_btDevice.getName() + "\n" + temp_btDevice.getAddress());
            } catch (Exception e) {  
                // TODO Auto-generated catch block  
                e.printStackTrace();  
            }
        }
    };
    // The on-click listener for paired devices in the ListViews
    private OnItemClickListener mPairedDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);            
            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra("target_address", address);
            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };
    
    

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            //蓝牙模块发送回来的信息
            if (action.equals("android.bluetooth.device.action.PAIRING_REQUEST"))
            {
            	BluetoothDevice temp_btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            	
            	try
            	{
                	setPin(temp_btDevice.getClass(), temp_btDevice, PIN); // 手机和蓝牙采集器配对
            		createBond(temp_btDevice.getClass(), temp_btDevice);
            		//cancelPairingUserInput(temp_btDevice.getClass(), temp_btDevice);
            	}
            	catch (Exception e)
            	{
            		// TODO Auto-generated catch block
            		e.printStackTrace();
            	}
            }
    
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            // When discovery is finished, change the Activity title
            } 
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) 
            {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) 
                {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
                Button scanButton = (Button) findViewById(R.id.button_scan);
                scanButton.setVisibility(View.VISIBLE);
            }
        }
    };

}