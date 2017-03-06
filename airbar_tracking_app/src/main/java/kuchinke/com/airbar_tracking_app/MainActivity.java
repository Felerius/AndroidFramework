package kuchinke.com.airbar_tracking_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {
    private RecyclerView bondedView;
    private RecyclerView foundView;
    private DeviceAdapter bondedAdapter;
    private DeviceAdapter foundAdapter;
    private List<BluetoothDevice> bonded_list=new ArrayList<>();
    private List<BluetoothDevice> found_list=new ArrayList<>();
    private BluetoothAdapter adapter;
    private String TAG="MAINACTIVITY";
    private ProgressBar progressCircle;
    private ImageButton refreshButton;

    private BroadcastReceiver deviceReceiver= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)){
                Log.d(TAG, "onReceive: Found device");
                BluetoothDevice d= intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                found_list.add(d);
                foundAdapter.notifyItemInserted(foundAdapter.getItemCount());
                return;
            }
            if(intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
                Log.d(TAG, "onReceive: Discovery started");
               // Animation refreshAnim= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.refresh_rotation);
                //refreshButton.startAnimation(refreshAnim);
                progressCircle.setIndeterminate(true);
                return;
            }
            if(intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                Log.d(TAG, "onReceive: discovery finished");
               // refreshButton.clearAnimation();
                progressCircle.setIndeterminate(false);
                return;
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refreshButton=(ImageButton)findViewById(R.id.refreshbutton);
        progressCircle= (ProgressBar) findViewById(R.id.progressBar);
        bondedView= (RecyclerView)findViewById(R.id.bonded_list);
        foundView= (RecyclerView)findViewById(R.id.found_list);
        adapter= BluetoothAdapter.getDefaultAdapter();
        bondedAdapter= new DeviceAdapter(this,bonded_list);
        foundAdapter= new DeviceAdapter(this, found_list);
        bondedView.setLayoutManager(new LinearLayoutManager(this));
        foundView.setLayoutManager(new LinearLayoutManager(this));
        bondedView.setAdapter(bondedAdapter);
        foundView.setAdapter(foundAdapter);

        IntentFilter filter= new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(deviceReceiver,filter);

        bondedAdapter.setCallback(new DeviceAdapter.ItemClickListener() {
            @Override
            public void onItemClick(BluetoothDevice d) {
                Intent i= new Intent(getApplicationContext(), DeviceCommunicationActivity.class);
                i.putExtra("DEVICE", d);
                adapter.cancelDiscovery();
                startActivity(i);
                //finish();
            }
        });
        foundAdapter.setCallback(new DeviceAdapter.ItemClickListener() {
            @Override
            public void onItemClick(BluetoothDevice d) {
                Intent i= new Intent(getApplicationContext(), DeviceCommunicationActivity.class);
                i.putExtra("DEVICE", d);
                adapter.cancelDiscovery();
                startActivity(i);
                //finish();
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                found_list.clear();
                foundAdapter.notifyDataSetChanged();
                checkPermissions();
            }
        });






        if(adapter!=null && adapter.isEnabled()){
            for(BluetoothDevice d: adapter.getBondedDevices()){
                Log.d(TAG, "onCreate: "+d.getAddress());
                bonded_list.add(d);
                bondedAdapter.notifyDataSetChanged();
            }
            Log.d(TAG, "onCreate: bonded listed, checking permissions");
            checkPermissions();
            Log.d(TAG, "onCreate: checked permission");
        }

        else{
            /*
                if (adapter.enable()){
                    for(BluetoothDevice d: adapter.getBondedDevices()){
                        Log.d(TAG, "onCreate: "+d.getAddress());
                        bonded_list.add(d);
                        bondedAdapter.notifyDataSetChanged();
                    }
                    adapter.cancelDiscovery();
                    adapter.startDiscovery();
                }
                else{*/
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1000);
               // }


        }
        Log.d(TAG, "onCreate: --------------INSTRUCTIONS------------------");





    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(deviceReceiver);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1000 && resultCode==RESULT_OK){
            for(BluetoothDevice d: adapter.getBondedDevices()){
                Log.d(TAG, "onCreate: "+d.getAddress());
                bonded_list.add(d);
                bondedAdapter.notifyItemInserted(bonded_list.size()-1);
            }
            adapter.cancelDiscovery();
            adapter.startDiscovery();
        }

    }

    void checkPermissions(){
        int result= PermissionChecker.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION");
        if(result== PermissionChecker.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,new String[]{"android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCTION"},1000);
        }
        else{
            adapter.cancelDiscovery();
            adapter.startDiscovery();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1000 && grantResults[0]== PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){
            adapter.cancelDiscovery();
            adapter.startDiscovery();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


}
