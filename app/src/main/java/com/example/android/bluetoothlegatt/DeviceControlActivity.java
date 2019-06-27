/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity implements View.OnClickListener{
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;//通用数据接收
    private TextView DPS310DataField;//310数据接收
    private TextView BatterDataField;//电池电量接收
    private TextView WorkModeDataField;//当前工作状态接收
    private TextView SmokePowerDataField;//当前功率 显示
    private TextView SmokeEnergyDataField;//本次使用能量
    private TextView USEDEnergyDataField;//累积使用能量
    private TextView GetSetPowerDataField;//累积使用能量

    private EditText readSendData;//edittext要发送的数据
    private EditText setSmokePower;//edittext要设定的抽烟最大功率
    private Button send_btn;//发送按钮
    private Button send_locking_btn;//锁定按钮
    private Button send_unlock_btn;//解锁按钮
    private Button btn_mode_btn;//显示模式选择
    private Button list_mode_start_btn;//显示模式选择
    private Button plus_power_btn;//解锁按钮
    private Button inc_power_btn;//显示模式选择

    public static float setsmokepower = 6.5f;

    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private List<BluetoothGattService> mnotyGattService_list;
    private ArrayList<BluetoothGattCharacteristic> temp_GattCharacteristics = new ArrayList<BluetoothGattCharacteristic>();
    private BluetoothGattCharacteristic Data_send_characteristic;//发送数据 功能
    private BluetoothGattCharacteristic Lock_send_characteristic;//锁定功能
    private BluetoothGattCharacteristic Batter_recv_characteristic;//电量信息
    private BluetoothGattCharacteristic DPS310_recv_characteristic;//dps310传感器信息
    private BluetoothGattCharacteristic Universally_recv_characteristic;//通用信息接收
    private BluetoothGattCharacteristic WorkMode_recv_characteristic;//当前工作状态接收
    private BluetoothGattCharacteristic SmokePower_recv_characteristic;//抽烟功率接收
    private BluetoothGattCharacteristic SmokeEnergy_recv_characteristic;//本次消耗能量
    private BluetoothGattCharacteristic GetSetPower_sendrecv_characteristic;//当前设定的最大的功率值

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))
            {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action))
            {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
                mnotyGattService_list = mBluetoothLeService.getSupportedGattServices();

                for (BluetoothGattService gattService_temp : mnotyGattService_list)
                {
                    if (gattService_temp.getUuid().equals(UUID.fromString(SampleGattAttributes.Data_service)))//通用数据服务
                    {
                        Data_send_characteristic =
                                gattService_temp.getCharacteristic(UUID.fromString(SampleGattAttributes.Data_send_chara));

                        Lock_send_characteristic =
                                gattService_temp.getCharacteristic(UUID.fromString(SampleGattAttributes.Locking_send_chara));

                        Universally_recv_characteristic =
                                gattService_temp.getCharacteristic(UUID.fromString(SampleGattAttributes.Data_recv_chara));
                        temp_GattCharacteristics.add(Universally_recv_characteristic);

                        DPS310_recv_characteristic =
                                gattService_temp.getCharacteristic(UUID.fromString(SampleGattAttributes.DPS310_recv_chara));
                        temp_GattCharacteristics.add(DPS310_recv_characteristic);

                        WorkMode_recv_characteristic =
                                gattService_temp.getCharacteristic(UUID.fromString(SampleGattAttributes.workmode_recv_chara));
                        temp_GattCharacteristics.add(WorkMode_recv_characteristic);

                        SmokePower_recv_characteristic =
                                gattService_temp.getCharacteristic(UUID.fromString(SampleGattAttributes.power_recv_chara));
                        temp_GattCharacteristics.add(SmokePower_recv_characteristic);

                        SmokeEnergy_recv_characteristic =
                                gattService_temp.getCharacteristic(UUID.fromString(SampleGattAttributes.Energy_recv_chara));
                        temp_GattCharacteristics.add(SmokeEnergy_recv_characteristic);

                        GetSetPower_sendrecv_characteristic =
                            gattService_temp.getCharacteristic(UUID.fromString(SampleGattAttributes.powerset_recvsend_chara));
                    }

                    if (gattService_temp.getUuid().equals(UUID.fromString(SampleGattAttributes.Batter_service)))
                    {
                        Batter_recv_characteristic =
                                gattService_temp.getCharacteristic(UUID.fromString(SampleGattAttributes.Batter_chara));
                        temp_GattCharacteristics.add(Batter_recv_characteristic);
                    }
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            for(BluetoothGattCharacteristic chara_temp : temp_GattCharacteristics)
                            {
                                final int charaProp = chara_temp.getProperties();

                                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)
                                {
                                    mBluetoothLeService.setCharacteristicNotification(chara_temp, true);
                                    //Log.d(TAG, "enable the  NOTIFY");
                                    System.out.println("enable the  NOTIFY");
                                }

                                Thread.sleep(300);
                            }

                            mBluetoothLeService.readCharacteristic(GetSetPower_sendrecv_characteristic);

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();

                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))
            {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                display_batterlevelData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA_batter));
                display_dps310Data(intent.getStringExtra(BluetoothLeService.EXTRA_DATA_dps310));
                display_WorkModeData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA_workmode));
                display_smokepowerData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA_smokepower));
                display_smokeenergylData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA_smokeenergy));
                display_usedenergylData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA_usedenergy));
                display_GetSetPowerlData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA_getsetpower));
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner = new ExpandableListView.OnChildClickListener()
    {
         @Override
         public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,int childPosition, long id)
         {
             if (mGattCharacteristics != null)
             {
                 final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);
                 final int charaProp = characteristic.getProperties();

                 if (characteristic.getUuid().toString().equals(SampleGattAttributes.Data_send_chara))  //自己增加的数据
                 {
                     String data = readSendData.getText().toString();
                     characteristic.setValue(data.getBytes());
                     mBluetoothLeService.wirteCharacteristic(characteristic);
                     readSendData.setText("");
                 }

                 if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0)
                 {
                     // If there is an active notification on a characteristic, clear
                     // it first so it doesn't update the data field on the user interface.
                     if (mNotifyCharacteristic != null)
                     {
                         mBluetoothLeService.setCharacteristicNotification( mNotifyCharacteristic, true);
                         mNotifyCharacteristic = null;
                     }
                     mBluetoothLeService.readCharacteristic(characteristic);
                 }

                 if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)
                 {
                     mNotifyCharacteristic = characteristic;
                     mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                 }
                 return true;
             }
             return false;
         }
    };

    private void clearUI()
    {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            String tempData = savedInstanceState.getString("usedSmokeEnergy");
            Log.d(TAG, tempData);
            display_usedenergylData(tempData);
        }

        getWindow().setSoftInputMode
                (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);

        mDataField = (TextView) findViewById(R.id.data_value);
        DPS310DataField = (TextView) findViewById(R.id.dps310_data_value);
        BatterDataField = (TextView) findViewById(R.id.batterlevel_data_value);
        WorkModeDataField = (TextView) findViewById(R.id.workmode_value);
        SmokePowerDataField = (TextView) findViewById(R.id.smokepower_value);
        SmokeEnergyDataField = (TextView) findViewById(R.id.smokeenergy_data_value);
        USEDEnergyDataField = (TextView)findViewById(R.id.UsedEnergy_data_value);
        GetSetPowerDataField = (TextView)findViewById(R.id.powerget_data_value);

        readSendData = (EditText)findViewById(R.id.ready_send_data);
        setSmokePower = (EditText)findViewById(R.id.setpower_data);
        send_btn = (Button) findViewById(R.id.send_btn);
        send_locking_btn = (Button) findViewById(R.id.send_locking_btn);
        send_unlock_btn = (Button) findViewById(R.id.send_unlock_btn);
        plus_power_btn = (Button) findViewById(R.id.plus_power_btn);
        inc_power_btn = (Button) findViewById(R.id.inc_power_btn);

        btn_mode_btn = (Button) findViewById(R.id.btn_mode_btn);
        list_mode_start_btn = (Button) findViewById(R.id.list_mode_start_btn);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        send_btn.setOnClickListener(this);
        send_locking_btn.setOnClickListener(this);
        send_unlock_btn.setOnClickListener(this);
        btn_mode_btn.setOnClickListener(this);
        list_mode_start_btn.setOnClickListener(this);
        plus_power_btn.setOnClickListener(this);
        inc_power_btn.setOnClickListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String UsedSmokeEnergy = USEDEnergyDataField.getText().toString();
        Log.d(TAG, UsedSmokeEnergy);
        outState.putString("usedSmokeEnergy", UsedSmokeEnergy);
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.send_btn:
            {
                if(mConnected == true)
                {
                    String data = readSendData.getText().toString();
                    Data_send_characteristic.setValue(data.getBytes());
                    mBluetoothLeService.wirteCharacteristic(Data_send_characteristic);
                }
                else
                    Toast.makeText(DeviceControlActivity.this, "请先开启蓝牙", Toast.LENGTH_SHORT).show();
            }break;

            case R.id.send_locking_btn:
            {
                if(mConnected == true)
                {
                    String data = "lock";
                    Lock_send_characteristic.setValue(data.getBytes());
                    mBluetoothLeService.wirteCharacteristic(Lock_send_characteristic);
                }
                else
                    Toast.makeText(DeviceControlActivity.this, "请先开启蓝牙", Toast.LENGTH_SHORT).show();

            }break;

            case R.id.send_unlock_btn:
            {
                if(mConnected == true)
                {
                    String data = "unlock";
                    Lock_send_characteristic.setValue(data.getBytes());
                    mBluetoothLeService.wirteCharacteristic(Lock_send_characteristic);
                }
                else
                    Toast.makeText(DeviceControlActivity.this, "请先开启蓝牙", Toast.LENGTH_SHORT).show();

            }break;

            case R.id.btn_mode_btn:
            {
                Toast.makeText(DeviceControlActivity.this, "下版本支持", Toast.LENGTH_SHORT).show();
            }break;

            case R.id.list_mode_start_btn:
            {
                Toast.makeText(DeviceControlActivity.this, "下版本支持", Toast.LENGTH_SHORT).show();
            }break;

            case R.id.plus_power_btn:
            {
                setsmokepower += 0.1f;
                if(setsmokepower > 8.0f)
                    setsmokepower = 8.0f;

                setSmokePower.setText(Float.toString(setsmokepower));

                String data = readSendData.getText().toString();
                GetSetPower_sendrecv_characteristic.setValue(data.getBytes());

                for(int i=0;i<data.length();i++)
                    System.out.println(data);

                mBluetoothLeService.wirteCharacteristic(GetSetPower_sendrecv_characteristic);
                mBluetoothLeService.readCharacteristic(GetSetPower_sendrecv_characteristic);

            };break;

            case R.id.inc_power_btn:
            {
                setsmokepower -= 0.1f;
                if(setsmokepower < 4.0f)
                    setsmokepower = 4.0f;

                DecimalFormat df = new DecimalFormat("####.#");

                setSmokePower.setText(Float.toString(setsmokepower));

                String data = setSmokePower.getText().toString();
                GetSetPower_sendrecv_characteristic.setValue(data.getBytes());

                for(int i=0;i<data.length();i++)
                        System.out.println(data);

                mBluetoothLeService.wirteCharacteristic(GetSetPower_sendrecv_characteristic);
                mBluetoothLeService.readCharacteristic(GetSetPower_sendrecv_characteristic);

            };break;

            default:break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        Log.d(TAG, "onDestroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }

    private void display_dps310Data(String data) {
        if (data != null) {
            DPS310DataField.setText(data);
        }
    }

    private void display_batterlevelData(String data) {
        if (data != null) {
            BatterDataField.setText(data +"%");
        }
    }

    private void display_smokepowerData(String data) {
        if (data != null) {
            SmokePowerDataField.setText(data);
        }
    }

    private void display_smokeenergylData(String data) {
        if (data != null) {
            SmokeEnergyDataField.setText(data);
        }
    }

    private void display_usedenergylData(String data) {
        if (data != null) {
            USEDEnergyDataField.setText(data);
        }
    }

    private void display_GetSetPowerlData(String data) {
        if (data != null) {
            GetSetPowerDataField.setText(data+"W");
        }
    }

    private void display_WorkModeData(String data) {
        if (data != null) {

            String display =new String();
            switch (data)
            {
                case "0": display="空闲状态";   break;
                case "1": display="查询状态";   break;
                case "2": display="抽烟状态";   break;
                case "3": display="充电状态";   break;
                case "4": display="待机状态";   break;
                case "5": display="充满状态";   break;
                case "6": display="低电量状态"; break;
                case "7": display="断开充电状态";break;
                case "8": display="短路";       break;
                case "9": display="开路";       break;
                case "10":display="干烧";       break;
                case "11":display="电池温度异常";break;
                case "12": display="电池电压低于安全充电值"; break;
                default:WorkModeDataField.setText("未知状态" );break;
            }

            if(data !="2")
                SmokePowerDataField.setText("0");

            WorkModeDataField.setText(display );
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices)
    {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
