package com.bb.WaterFlowMeter.BusinessLogic;

import com.bb.WaterFlowMeter.Models.KeycloakAccessToken;
import com.bb.WaterFlowMeter.Models.MqttMsgDTO;
import com.bb.WaterFlowMeter.SecurityCredentials;
import com.bb.WaterFlowMeter.WebClient.WebClientImplementation;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WaterFlowMeter {
    private String serverURL; // mqtt broker url
    private String webURL; // web server url
    private String topic;
    private List<Sensor> sensorList = new ArrayList<>();
    private String subtopicLate;

    @JsonIgnore
    private SecurityCredentials credentials;
    @JsonIgnore
    private KeycloakAccessToken token;

    @JsonCreator
    public WaterFlowMeter(@JsonProperty("serverURL") String url,
                          @JsonProperty("topic") String topic,
                          @JsonProperty("subtopicLate") String subtopicLate,
                          @JsonProperty("webURL") String webURL) {
        this.serverURL = url;
        this.webURL = webURL;
        this.topic = topic;
        this.subtopicLate = subtopicLate;
    }

    public String getServerURL() {
        return serverURL;
    }
    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }
    public String getWebURL() {
        return webURL;
    }
    public void setWebURL(String webURL) {
        this.webURL = webURL;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public List<Sensor> getSensorList() {
        return sensorList;
    }
    public void setSensorList(List<Sensor> sensorList) {
        this.sensorList = sensorList;
    }
    public String getSubtopicLate() {
        return subtopicLate;
    }
    public void setSubtopicLate(String subtopicLate) {
        this.subtopicLate = subtopicLate;
    }

    public static WaterFlowMeter createFromJSON(File f) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            //mapper.enable(SerializationFeature.INDENT_OUTPUT);
            WaterFlowMeter wfm;
            wfm = mapper.readValue(f, WaterFlowMeter.class);
            return wfm;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Error in createFromJSON static method, returned null");
        return null;
    }

    // vj5
    public void postValuesToSecuredWebUrlContinuous(String webURL, SecurityCredentials credentials, long intervalSec) {
        this.credentials = credentials;
        WebClientImplementation webClient = new WebClientImplementation();
        this.token = webClient.getNewAccessToken(
                this.credentials.getToken_url(),
                this.credentials.getClient_id(),
                this.credentials.getClient_secret(),
                this.credentials.getUsername(),
                this.credentials.getPassword()
        );
        if (this.token == null) {
            System.out.println("Greska u dobavljanju inicijalnog access tokena");
            return;
        }
        while (true) {
            this.postAllValuesToSecuredWebUrl(webClient);
            try {
                Thread.sleep(intervalSec * 1000); // *1000 for Miliseconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // vj5
    private void postAllValuesToSecuredWebUrl(WebClientImplementation webClient) {
        MqttMsgDTO mqttMsgDTO = new MqttMsgDTO(this.serverURL, this.topic);
        for (Sensor s: this.sensorList) {
            String jsonStr = "";
            s.readCurrentValue();
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                jsonStr = jsonStr.concat(mapper.writeValueAsString(s));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mqttMsgDTO.setMessage(jsonStr);

            MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
            header.add("Authorization", "Bearer " + this.token.getAccess_token());

            // u Map.Entry<> je HttpStatus i objekt kojeg dobijem u response-u
            Map.Entry<HttpStatus, MqttMsgDTO> resultPair;

            resultPair = webClient.makePOSTRequest(this.webURL, header, mqttMsgDTO, MqttMsgDTO.class);
            if (resultPair == null) {
                System.out.println("Neuspijelo slanje POST requesta - vjerojatno razlog Host connection refused!");
                // odmah prekinem metodu, bez obzira ima li jos senzora u petlji...
                return;
            }


            if (resultPair.getKey().is2xxSuccessful()) {
                System.out.println("POST request uspjesno obavljen");
            }
            else if (resultPair.getKey().value() == 401) { // 401 Unauthorized
                this.token = webClient.getNewAccessTokenByRefreshToken(
                        this.credentials.getToken_url(),
                        this.credentials.getClient_id(),
                        this.credentials.getClient_secret(),
                        this.token.getRefresh_token()
                );
                if (this.token == null) {
                    System.out.println("Greska u dobavljanju access tokena preko refresh tokena");
                    return;
                }

                header.set("Authorization", "Bearer " + this.token.getAccess_token());

                resultPair = webClient.makePOSTRequest(this.webURL, header, mqttMsgDTO, MqttMsgDTO.class);
                if (resultPair == null) {
                    System.out.println("Neuspijelo slanje POST requesta - vjerojatno razlog Host connection refused!");
                    return;
                }

                if (resultPair.getKey().value() == 401) {
                    this.token = webClient.getNewAccessToken(
                            this.credentials.getToken_url(),
                            this.credentials.getClient_id(),
                            this.credentials.getClient_secret(),
                            this.credentials.getUsername(),
                            this.credentials.getPassword()
                    );
                    if (this.token == null) {
                        System.out.println("Greska u dobavljanju novog access tokena");
                        return;
                    }

                    header.set("Authorization", "Bearer " + this.token.getAccess_token());

                    resultPair = webClient.makePOSTRequest(this.webURL, header, mqttMsgDTO, MqttMsgDTO.class);
                    if (resultPair == null) {
                        System.out.println("Neuspijelo slanje POST requesta - vjerojatno razlog Host connection refused!");
                        return;
                    }

                    if (resultPair.getKey().value() != 200) {
                        System.out.println("POST request se nije obavi ni nakon dohvata novog access tokena");
                        System.out.println(resultPair.getKey().getReasonPhrase());
                    }
                }
            }
        }
    }

    public void postValuesToWebUrlContinuous(long intervalMilisec) {
        WebClientImplementation webClient = new WebClientImplementation();
        while (true) {
            this.postAllValuesToWebUrl(webClient);
            try {
                Thread.sleep(intervalMilisec);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void postAllValuesToWebUrl(WebClientImplementation webClient) {
        MqttMsgDTO mqttMsgDTO = new MqttMsgDTO(serverURL, topic);
        for (Sensor s: sensorList) {
            String jsonStr = "";
            s.readCurrentValue();
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                jsonStr = jsonStr.concat(mapper.writeValueAsString(s));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mqttMsgDTO.setMessage(jsonStr);
            webClient.makePOSTRequest(this.webURL, new LinkedMultiValueMap<>(), mqttMsgDTO, Void.class);
        }
    }

    public void publishAllValuesContinuous(long interval, boolean deseriazlibleMessageFormat, boolean publishToSubtopics) {
        while (true) {
            this.publishAllValues(deseriazlibleMessageFormat, publishToSubtopics);
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void publishAllValues(boolean deseriazlibleMessageFormat, boolean publishToSubtopics) {
        int sensorIndex = 0;
        float tmpValue = 0f;
        boolean valueRead = false;
        try {
            publishUnpublishedValuesIfExists(this.subtopicLate);

            valueRead = false;

            // mozda sam trebao staviti da klijent bude clan klase ali da ga ne serijaliziram ??
            MqttClient client = new MqttClient(serverURL, MqttClient.generateClientId());
            client.connect();
            MqttMessage mqttMessage = new MqttMessage();
            String strMsg;

            for (Sensor s: sensorList) {
                strMsg = "";
                tmpValue = s.readCurrentValue();
                valueRead = true;
                if (deseriazlibleMessageFormat) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.enable(SerializationFeature.INDENT_OUTPUT);
                        strMsg = strMsg.concat(mapper.writeValueAsString(s));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    strMsg = strMsg.concat(dtf.format(now) + " " + s.getName() + ": " + tmpValue + " " + s.getUnit());
                }
                String topic_final = topic;
                if (publishToSubtopics)
                    topic_final = topic + "/" + s.getName().toLowerCase();
                mqttMessage.setPayload(strMsg.getBytes());
                client.publish(topic_final, mqttMessage);
                sensorIndex += 1;
            }
            client.disconnect();
        } catch (MqttException e1) {
            // 32103 REASON_CODE_SERVER_CONNECT_ERROR == Unable to connect to server
            if (e1.getReasonCode() == 32103) {
                // once error occurs save all values from remaining Sensors
                // only 1 value can be read and NOT published, this if handles that
                if (valueRead) {
                    sensorList.get(sensorIndex).addToNotPublished(tmpValue);
                    sensorIndex += 1;
                }
                while (sensorIndex < sensorList.size()) {
                    Sensor tmp = sensorList.get(sensorIndex);
                    tmp.addToNotPublished(tmp.readCurrentValue());
                    sensorIndex += 1;
                }
            }
        }
    }

    private void publishUnpublishedValuesIfExists(String subtopic) {
        // trenutno je napravljeno samo da salje jednostavn poruku, datum, ime senzor i value
        try {
            MqttClient client = new MqttClient(serverURL, MqttClient.generateClientId());
            client.connect();
            MqttMessage mqttMessage = new MqttMessage();
            String strMsg;

            for (Sensor s: sensorList) {
                strMsg = "";
                // za sada cu napraviti da se sve vrijednosti salje u jednoj poruci
                for (Map.Entry<String, Float> entry: s.getMap().entrySet()) {
                    strMsg = strMsg.concat(entry.getKey() + " " + s.getName() + ": " + entry.getValue() + " " + s.getUnit() + "\n");
                }
                String finalTopic = topic + "/" + subtopic;
                mqttMessage.setPayload(strMsg.getBytes());
                client.publish(finalTopic, mqttMessage);
                s.getMap().clear();
            }
            client.disconnect();
        } catch (MqttException e) {
            //e.getMessage();
            //System.out.println("Neuspjelo publishanje zaostalih poruka!");
        }
    }

    public void printUnpublished() {
        for (Sensor s: sensorList) {
            String strMsg = "";
            for (Map.Entry<String, Float> entry: s.getMap().entrySet()) {
                strMsg = strMsg.concat(entry.getKey() + " " + s.getName() + ": " + entry.getValue() + " " + s.getUnit() + "\n");
            }
            System.out.println(strMsg);
        }
    }

    public void printPrettyJSON() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            System.out.println(mapper.writeValueAsString(this));
            //System.out.println(mapper.writeValueAsString(this).replace("\"", "\\\""));
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Error printing pretty JSON");
    }

    public String jsonAsString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            //mapper.enable(SerializationFeature.INDENT_OUTPUT);
            //System.out.println(mapper.writeValueAsString(this));
            //System.out.println(mapper.writeValueAsString(this).replace("\"", "\\\""));
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Error printing pretty JSON");
        return null;
    }

    public void saveAsJson(String dirPath, String fileName) {
        File f = new File(dirPath, fileName);
        try {
            if (f.createNewFile() == false) {
                System.out.println("file with specified name already exists");
                return;
            }
            System.out.println("file created");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("file NOT created");
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(f, this);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error writing json file!");
        }
    }

    public void saveAsJson2(String dirPath, String fileName, boolean overwrite) {
        try {
            if (overwrite) {
                Writer fWriter = new FileWriter(dirPath + "/" + fileName, false);
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.enable(SerializationFeature.INDENT_OUTPUT);
                    mapper.writeValue(fWriter, this);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error writing json file!");
                }
                fWriter.close();
            }
            else {
                System.out.println("File with specified name already exists");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
