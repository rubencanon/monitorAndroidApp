package com.polar.polarsdkedghrdemo.mqtt;


import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class EcgMqttClient {

    String topic = "monitor/heart";
    int qos = 2;
    String broker = "ws://broker.emqx.io:8083";
    String clientId = "Androind_RECC";
    MemoryPersistence persistence = new MemoryPersistence();
    MqttClient sampleClient;

    public void publishData(String messsage) {


        try {

            System.out.println("Publishing message: " + messsage);
            MqttMessage message = new MqttMessage(messsage.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            System.out.println("Message published");

        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }

    }

    public boolean connectToMqttServer() {


        try {

            sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: " + broker);
            sampleClient.connect(connOpts);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            return true;

        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());

            return false;
        }
    }

    public boolean disconnectFromMqttServer() {

        try {
            sampleClient.disconnect();
            System.out.println("Disconnected");
            System.exit(0);

            return true;
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            return false;
        }

    }
}
