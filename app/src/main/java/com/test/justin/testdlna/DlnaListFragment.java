package com.test.justin.testdlna;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justin on 2015-03-31.
 */
public class DlnaListFragment extends Fragment {
    private final static String TAG = "DlnaListFragment";
    private RecyclerView mDeviceRecyclerView;
    private DeviceAdapter mAdapter;
    private AndroidUpnpService upnpService;
    private DefaultRegistryListener registryListener = new RegistryListenerTest();
    private BroadcastReceiver mReceiver;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TAG, "Service Connected");
            upnpService = (AndroidUpnpService) service;
            // Refresh the list with all known devices
            //listAdapter.clear();
            for (Device device : upnpService.getRegistry().getDevices()) {
                Log.i(TAG, device.getDetails().toString());
            }

            // Getting ready for future device advertisements
            upnpService.getRegistry().addListener(registryListener);

            // Search asynchronously for all devices
            upnpService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getApplicationContext()
                .bindService(new Intent(this.getActivity(), UpnpServiceTestImpl.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_dlna_list, container, false);

        //#TODO Clean up upnp service code in fragment

        mDeviceRecyclerView = (RecyclerView) view.findViewById(R.id.device_recycler_view);
        mDeviceRecyclerView.setAdapter(mAdapter);
        mDeviceRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUI();
            }
        };



        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
        getActivity().getApplicationContext().unbindService(serviceConnection);
    }


    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mReceiver,new IntentFilter("FUCK"));
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mReceiver);
    }

    private class DeviceHolder extends RecyclerView.ViewHolder{
        public TextView mTitleTextView;

        public DeviceHolder(View itemView) {
            super(itemView);
            mTitleTextView = (TextView) itemView;
        }
    }

    private class DeviceAdapter extends RecyclerView.Adapter<DeviceHolder> {

        //Hold the list of devices
        private List<Device> mDevices;

        public DeviceAdapter(List<Device> devices){
            mDevices = devices;
        }

        @Override
        public DeviceHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            TextView textView = new TextView(getActivity());
            return new DeviceHolder(textView);
        }

        @Override
        public void onBindViewHolder(DeviceHolder deviceHolder, int position) {
            Device device = mDevices.get(position);
            deviceHolder.mTitleTextView.setText(device.getDetails().getFriendlyName());
        }

        @Override
        public int getItemCount() {
            return mDevices.size();
        }

    }

    public void updateUI(){
        ArrayList<Device> devices = new ArrayList<>();
        if(upnpService != null) {
            for (Device d : upnpService.getRegistry().getDevices()) {
                devices.add(d);
            }
        }
        mAdapter = new DeviceAdapter(devices);
        mDeviceRecyclerView.setAdapter(mAdapter);
    }

    /**
     * RegistryListener
     */
    private class RegistryListenerTest extends DefaultRegistryListener{
        public RegistryListenerTest() {
            super();
        }

        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            Log.i(TAG, device.getDetails().getFriendlyName() + " remoteDeviceDiscoveryStarted");
            super.remoteDeviceDiscoveryStarted(registry, device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
            Log.i(TAG, device.getDetails().getFriendlyName() + " remoteDeviceDiscoveryFailed");
            super.remoteDeviceDiscoveryFailed(registry, device, ex);
        }

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            Log.i(TAG,device.getDetails().getFriendlyName() + " added");
            super.remoteDeviceAdded(registry, device);
        }

        @Override
        public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
            Log.i(TAG,device.getDetails().getFriendlyName() + " updated");
            super.remoteDeviceUpdated(registry, device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            Log.i(TAG,device.getDetails().getFriendlyName() + " removed");
            super.remoteDeviceRemoved(registry, device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            Log.i(TAG,device.getDetails().getFriendlyName() + " localadd");
            super.localDeviceAdded(registry, device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            Log.i(TAG,device.getDetails().getFriendlyName() + " localremove");
            super.localDeviceRemoved(registry, device);
        }

        @Override
        public void deviceAdded(Registry registry, Device device) {
            Log.i(TAG,device.getDetails().getFriendlyName() + " device added");
            Intent intent = new Intent("FUCK");
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            super.deviceAdded(registry, device);
        }

        @Override
        public void deviceRemoved(Registry registry, Device device) {
            Log.i(TAG,device.getDetails().getFriendlyName() + " device removed");
            super.deviceRemoved(registry, device);
        }

        @Override
        public void beforeShutdown(Registry registry) {
            Log.i(TAG,"before shutdown");
            super.beforeShutdown(registry);
        }

        @Override
        public void afterShutdown() {
            Log.i(TAG,"after shutdown");
            super.afterShutdown();
        }
    }

}
