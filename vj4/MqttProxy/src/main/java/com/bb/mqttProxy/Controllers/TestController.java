package com.bb.mqttProxy.Controllers;

import com.bb.mqttProxy.Models.MqttMsgDTO;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/test")
public class TestController {

    @GetMapping("/text1")
    public String text1() {
        return "moja poruka preko /text";
    }

    @GetMapping("/auth/text2")
    public String test2(@RequestHeader String authorization) {
        return "moja poruka preko /text koja je zasticena";
    }

    @GetMapping("/msg1")
    public MqttMsgDTO test3() {
        MqttMsgDTO m1 = new MqttMsgDTO();
        m1.setServerURL("url");
        m1.setTopic("topic");
        m1.setMessage("{ovo je novi message}");
        return m1;
    }

    @PostMapping("/publish1")
    public String test4(@RequestBody MqttMsgDTO message) {
        try {
            MqttClient mqttClient = new MqttClient(message.getServerURL(), MqttClient.generateClientId());
            mqttClient.connect();
            //mqttClient.publish(message.getTopic(), new MqttMessage().setPayload(message.getMessage().getBytes()););
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(message.getMessage().getBytes());
            mqttClient.publish(message.getTopic(), mqttMessage);
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.getMessage();
        }
        //System.out.println("@postMapping na  /publish1  OK");
        //return "request OK";
        return message.getServerURL() + message.getTopic() + message.getMessage();
    }

    @PostMapping("/publish2")
    public MqttMsgDTO test5(@RequestBody MqttMsgDTO message) {
        try {
            MqttClient mqttClient = new MqttClient(message.getServerURL(), MqttClient.generateClientId());
            mqttClient.connect();
            //mqttClient.publish(message.getTopic(), new MqttMessage().setPayload(message.getMessage().getBytes()););
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(message.getMessage().getBytes());
            mqttClient.publish(message.getTopic(), mqttMessage);
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.getMessage();
        }
        System.out.println("@postMapping na  /publish2  OK");
        message.setTopic("topic updated over POST");
        return message;
    }

}
