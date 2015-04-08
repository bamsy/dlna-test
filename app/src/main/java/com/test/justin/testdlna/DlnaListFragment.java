package com.test.justin.testdlna;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Browser;
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
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DescMeta;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

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
            super.remoteDeviceDiscoveryFailed(registry, device, ex);
        }

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            super.remoteDeviceAdded(registry, device);
        }

        @Override
        public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
            super.remoteDeviceUpdated(registry, device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            super.remoteDeviceRemoved(registry, device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            super.localDeviceAdded(registry, device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            super.localDeviceRemoved(registry, device);
        }

        @Override
        public void deviceAdded(Registry registry, Device device) {
            Log.i(TAG,device.getDetails().getFriendlyName() + " device added");
            if(device.getType().getType().equals("MediaServer")){
                Intent intent = new Intent("FUCK");
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                Service service = device.findService(new UDAServiceId("ContentDirectory"));
                if(service != null) {
                    upnpService.getControlPoint().execute(new Browse(service,"2", BrowseFlag.DIRECT_CHILDREN){
                        @Override
                        public void received(ActionInvocation actionInvocation, DIDLContent didl) {
                            Log.i(TAG,"received "+actionInvocation.getAction().getService().getDevice().getDetails().getFriendlyName());
                            Log.i(TAG,didl.getCount()+"");
                            for(Container c : didl.getContainers()){
                                Log.i(TAG,c.getTitle());
                            }
                        }

                        @Override
                        public void updateStatus(Status status) {

                        }

                        @Override
                        public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {

                        }
                    });
                }
            }
            super.deviceAdded(registry, device);
        }

        @Override
        public void deviceRemoved(Registry registry, Device device) {
            super.deviceRemoved(registry, device);
        }

        @Override
        public void beforeShutdown(Registry registry) {
            super.beforeShutdown(registry);
        }

        @Override
        public void afterShutdown() {
            super.afterShutdown();
        }
    }

}
