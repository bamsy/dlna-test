package com.test.justin.testdlna;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.Router;


public class DlnaActivity extends ActionBarActivity {
    private final static String TAG = "DlnaActivity";
    private AndroidUpnpService upnpService;
    private DefaultRegistryListener registryListener = new RegistryListenerTest();

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dlna);
        getApplicationContext().bindService(new Intent(this, UpnpServiceTestImpl.class),serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
        getApplicationContext().unbindService(serviceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dlna, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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
