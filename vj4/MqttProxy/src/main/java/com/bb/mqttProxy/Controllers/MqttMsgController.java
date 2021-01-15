package com.bb.mqttProxy.Controllers;

import com.bb.mqttProxy.Models.MqttMsgDTO;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.web.bind.annotation.*;

@RestController
public class MqttMsgController {

    @PostMapping("/publish") // glavni kontroler za zadatak
    public void publish(@RequestBody MqttMsgDTO message) {
        try {
            MqttClient mqttClient = new MqttClient(message.getServerURL(), MqttClient.generateClientId());
            mqttClient.connect();
            mqttClient.publish(message.getTopic(), new MqttMessage().setPayload(message.getMessage().getBytes()););
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.getMessage();
        }
    }

}
