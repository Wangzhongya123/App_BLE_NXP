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

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static String Device_info_service =      "0000180a-0000-1000-8000-00805f9b34fb";

    public static String Device_Name_chara =        "00002a00-0000-1000-8000-00805f9b34fb";
    public static String Appearance_chara =         "00002a01-0000-1000-8000-00805f9b34fb";
    public static String PPCP_chara =               "00002a04-0000-1000-8000-00805f9b34fb";

    public static String Service_Changed_chara =    "00002a05-0000-1000-8000-00805f9b34fb";
    public static String CCCD_chara =               "00002902-0000-1000-8000-00805f9b34fb";


    public static String Data_service =             "0000fee9-0000-1000-8000-00805f9b34fb";
    public static String Data_send_chara =          "d44bc439-abfd-45a2-b575-925416129600";
    public static String Data_recv_chara =          "d44bc439-abfd-45a2-b575-925416129601";
    public static String DPS310_recv_chara =        "d44bc439-abfd-45a2-b575-925416129602";
    public static String Locking_send_chara =       "d44bc439-abfd-45a2-b575-925416129603";
    public static String Energy_recv_chara =        "d44bc439-abfd-45a2-b575-925416129604";
    public static String power_recv_chara =         "d44bc439-abfd-45a2-b575-925416129605";
    public static String workmode_recv_chara =      "d44bc439-abfd-45a2-b575-925416129606";
    public static String powerset_recvsend_chara =  "d44bc439-abfd-45a2-b575-925416129607";

    public static String ECig_service =             "00008888-0000-1000-8000-00805f9b34fb";
    public static String ECig_Data_recv_chara =     "d44bc439-abfd-45a2-b575-925416129611";
    public static String ECig_Commd_recv_chara =    "d44bc439-abfd-45a2-b575-925416129612";
    public static String ECig_Data_send_chara =     "d44bc439-abfd-45a2-b575-925416129613";
    public static String ECig_Commd_send_chara =    "d44bc439-abfd-45a2-b575-925416129614";

    public static String Batter_service =           "0000180f-0000-1000-8000-00805f9b34fb";
    public static String Batter_chara =             "00002a19-0000-1000-8000-00805f9b34fb";
    public static String Chara_Presentation_chara = "00002904-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        attributes.put("00001800-0000-1000-8000-00805f9b34fb",  "GenericAccess");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb",  "GenericAttribute");
        attributes.put(Batter_service,                          "电池电量");
        attributes.put(Device_info_service,                     "硬件信息");
        attributes.put(Data_service,                            "数据收发");
        attributes.put(ECig_service,                            "电子烟数据收发");

        // gENERIC servive Characteristics.
        attributes.put(Device_Name_chara,                       "设备名称");
        attributes.put(Appearance_chara,                        "Appearance");
        attributes.put(PPCP_chara,                              "Peripheral Preferred Connection Parameters");

        // gENERIC servive Characteristics.
        attributes.put(Service_Changed_chara,                   "Service Changed");
        attributes.put(CCCD_chara,                              "CCCD");

        //通用 Characteristics.
        attributes.put(Data_send_chara,                         "通用数据发送");
        attributes.put(Data_recv_chara,                         "通用数据接收");
        attributes.put(DPS310_recv_chara,                       "压力传感器数据");
        attributes.put(Locking_send_chara,                      "锁定控制发送");
        attributes.put(Energy_recv_chara,                       "能量数据接收");
        attributes.put(power_recv_chara,                        "抽烟功率接收");
        attributes.put(workmode_recv_chara,                     "工作状态接收");
        attributes.put(powerset_recvsend_chara,                 "抽烟功率读取与设定");

        // 电子烟 Characteristics.
        attributes.put(ECig_Data_recv_chara,                    "电子烟数据接收");
        attributes.put(ECig_Commd_recv_chara,                   "电子烟命令接收");
        attributes.put(ECig_Data_send_chara,                    "电子烟数据发送");
        attributes.put(ECig_Commd_send_chara,                   "电子烟命令发送");

        // batter servive Characteristics.
        attributes.put(Batter_chara,                            "电池电量");
        attributes.put(Chara_Presentation_chara,                "Charateristic Presentation");

        // Device information Characteristics.
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("00002a24-0000-1000-8000-00805f9b34fb", "Model Number String");
        attributes.put("00002a25-0000-1000-8000-00805f9b34fb", "Serial Number String");
        attributes.put("00002a27-0000-1000-8000-00805f9b34fb", "Hardware Revision String");
        attributes.put("00002a26-0000-1000-8000-00805f9b34fb", "Firmware Revision String");
        attributes.put("00002a28-0000-1000-8000-00805f9b34fb", "Software Revision String");
        attributes.put("00002a23-0000-1000-8000-00805f9b34fb", "System ID");
        attributes.put("00002a2a-0000-1000-8000-00805f9b34fb", "IEEE");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
