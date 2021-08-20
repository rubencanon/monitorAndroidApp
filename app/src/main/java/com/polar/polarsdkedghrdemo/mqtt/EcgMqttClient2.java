package com.polar.polarsdkedghrdemo.mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class EcgMqttClient2 {
    final private String PRODUCTKEY = "a11xsrW****";
    final private String DEVICENAME = "paho_android";
    final private String DEVICESECRET = "tLMT9QWD36U2SArglGqcHCDK9rK9****";

    private String clientId;
    private String userName;
    private String passWord;
    MqttAndroidClient mqttAndroidClient;
    private String  host = "ws://" + "broker.emqx.io" + ":" + "8083";

    public void connetMqttServer(Context context){


        /* Obtain the MQTT connection parameters clientId, username, and password. */
        AiotMqttOption aiotMqttOption = new AiotMqttOption().getMqttOption(PRODUCTKEY, DEVICENAME, DEVICESECRET);
        if (aiotMqttOption == null) {
            Log.e("", "device info error");
        } else {
            clientId = aiotMqttOption.getClientId();
            userName = aiotMqttOption.getUsername();
            passWord = aiotMqttOption.getPassword();
        }

        /* Create an MqttConnectOptions object and configure the username and password. */
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName(userName);
        mqttConnectOptions.setPassword(passWord.toCharArray());
        /* Create an MqttAndroidClient object and configure the callback. */
        mqttAndroidClient = new MqttAndroidClient(context, host, "clientId");
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i("TAG", "connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i("TAG", "topic: " + topic + ", msg: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i("", "msg delivered");
            }
        });

        /* Establish a connection to IoT Platform by using MQTT. */
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i("", "connect succeed");

                    subscribeTopic("casa/nevera/");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i("", "connect failed");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

    public void subscribeTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i("", "subscribed succeed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i("", "subscribed failed");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


}
