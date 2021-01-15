package com.bb.WaterFlowMeter.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class MqttMsgDTO {
    private String serverURL;
    private String topic;
    private String message;

    public MqttMsgDTO(String serverURL, String topic) {
        this.serverURL = serverURL;
        this.topic = topic;
    }

    @JsonCreator
    public MqttMsgDTO(@JsonProperty("serverURL") String serverURL,
                      @JsonProperty("topic") String topic,
                      @JsonProperty("message") String message) {
        this.serverURL = serverURL;
        this.topic = topic;
        this.setMessage(message);
    }

    public String getServerURL() {
        return serverURL;
    }
    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public String getMessage() {
        return this.message.replace("\\\"", "\"");
    }
    public void setMessage(String message) {
        this.message = message.replace("\"", "\\\"");
    }

    public String jsonAsString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error serializing MqttMsgDTO to json string in jsonAsString()");
            return null;
        }
    }
}
