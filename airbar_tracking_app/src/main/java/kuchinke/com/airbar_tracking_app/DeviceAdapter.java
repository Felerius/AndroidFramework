package kuchinke.com.airbar_tracking_app;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Adrian on 04.11.2016.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    private List<BluetoothDevice> devices;
    private Context context;
    private ItemClickListener callback;

    public void setCallback(ItemClickListener callback) {
        this.callback = callback;
    }

    interface ItemClickListener{
        void onItemClick(BluetoothDevice d);
    }
    public DeviceAdapter(Context c, List<BluetoothDevice> devices){
        this.devices=devices;
        this.context=c;
    }
    private Context getContext(){
        return context;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView name;
        public TextView address;
        public ViewHolder(View item){
            super(item);
            name=(TextView) item.findViewById(R.id.name);
            address= (TextView) item.findViewById(R.id.address);
        }


    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View deviceView = inflater.inflate(R.layout.deviceelement, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(deviceView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final BluetoothDevice d= devices.get(position);
        TextView name= holder.name;
        TextView address=holder.address;
        String dname= d.getName();
        String daddress=d.getAddress();
        if(dname!=null){
            name.setText(dname);
            address.setText(daddress);
        }
        else{
            name.setText(daddress);
            address.setText("");
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback!=null)callback.onItemClick(d);
            }
        });

    }


    @Override
    public int getItemCount() {
        return devices.size();
    }



}
