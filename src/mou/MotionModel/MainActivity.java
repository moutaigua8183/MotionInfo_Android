package mou.MotionModel;

import android.support.v7.app.ActionBarActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends ActionBarActivity implements OnClickListener{

	private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    
    @Override
   public void onCreate(Bundle savedInstanceState) 
   {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
       
      
       
       View btButton = this.findViewById(R.id.bt_realtime);
       btButton.setOnClickListener(this);
       View msgButton = this.findViewById(R.id.bt_history);
       msgButton.setOnClickListener(this);
       View aboutButton = this.findViewById(R.id.bt_about);
       aboutButton.setOnClickListener(this);
       View exitButton = this.findViewById(R.id.bt_exit);
       exitButton.setOnClickListener(this);       
    }
   
    public void onClick(View v){
		final Intent i;
   	switch (v.getId()){
		case R.id.bt_realtime:
			i = new Intent(this, RealTime.class);
			startActivity(i);
			break;
		case R.id.bt_history:
			i = new Intent(this, History.class);
			startActivity(i);
			break;
		case R.id.bt_about:
			i = new Intent(this, About.class);
			startActivity(i);
			break;
		case R.id.bt_exit:
			finish();
			break;
		}
   }

   @Override
   protected void onDestroy() {
       super.onDestroy();
   	if (bluetoothAdapter.isEnabled() )
       	bluetoothAdapter.disable();
   }
}
