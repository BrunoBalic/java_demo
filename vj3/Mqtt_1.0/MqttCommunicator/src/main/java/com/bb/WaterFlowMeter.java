package com.bb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WaterFlowMeter {
    private String serverURL;
    private String topic;
    private List<Sensor> sensorList = new ArrayList<>();

    // Kad se koristi ovaj Jackson za de/serijalizaciju treba oznaciti
    // konstruktor sa @JsonCreator i ove @JsonProperty
    // * isto tako je i u klasi Senzor
    @JsonCreator
    public WaterFlowMeter(@JsonProperty("serverURL") String url, @JsonProperty("topic") String topic) {
        this.serverURL = url;
        this.topic = topic;
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
    public List<Sensor> getSensorList() {
        return sensorList;
    }
    public void setSensorList(List<Sensor> sensorList) {
        this.sensorList = sensorList;
    }

    // static metoda za kreiranje objekta
    // metoda prima objekt tipa File
    // file je .json konfiguracijska datoteka
    // u toj datoteci je serijaliziran jedan WaterFlowMeter objekt
    // * koristi se Jackson biblioteku za de/serijalizaciju - ObjectMapper
    public static WaterFlowMeter createFromJSON(File f) {
        try {
            // kreiranje objekta iz .json-a
            ObjectMapper mapper = new ObjectMapper();
            // mapper.readValue(f, WaterFlowMeter.class)
            // kreira objekt prosljedene klase iz prosljedenog file-a
            return mapper.readValue(f, WaterFlowMeter.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("error in parse, static method, returned null");
        return null;
    }

    // metoda koja u beskonacnoj petlji publisha sve vrijednosti odredenim intervalom
    // zaprva samo u petlji vrti ovu metodu sta jedanput publisha
    public void publishAllValuesContinuous(long intervalMilisec) {
        while (true) {
            this.publishAllValues();
            try {
                Thread.sleep(intervalMilisec);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // metoda koja jedan put publisha vrijednost svakog senzora
    public void publishAllValues() {
        try {
            // kreiranje MqttClient-a
            MqttClient client = new MqttClient(this.serverURL, MqttClient.generateClientId());
            client.connect();
            // MqttMessage objekt koristimo kod slanja poruke
            MqttMessage mqttMessage = new MqttMessage();

            // u ovom se dijelu za svaki senzor formira i salje poruka
            for (Sensor s: this.sensorList) {
                String strMsg = "";
                // poruka treba biti u json obliku pa preko ObjectMapper-a kreiramo json string
                try {
                    s.readCurrentValue(); // prije nego sto serijaliziramo senzor objekt treba ocitati novu vrijednost
                    // ovo je opet ista prica sa Jacksom ObjectMapper-om
                    ObjectMapper mapper = new ObjectMapper();
                    // ovo lijepo formatira json da bude u redovima inace je sve u jednom redu pa nije citljivo
                    mapper.enable(SerializationFeature.INDENT_OUTPUT);
                    // mapper.writeValueAsString(s) vraca json String objekta koji mu posaljemo
                    strMsg = mapper.writeValueAsString(s);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // ova .publish() metoda ne prima String vec MqttMessage
                // pa onda pretvaramo String u MqttMessage
                mqttMessage.setPayload(strMsg.getBytes());
                client.publish(this.topic, mqttMessage);
            }
            // na kraju se treba disconnect
            client.disconnect();
        } catch (MqttException e1) {
            e1.printStackTrace();
        }
    }

    // ovo mi je metoda samo onako da mogu vidjet kako mi izgleda json ovog objekta
    // ovo se isprint u konzoli
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

    // metoda koja iz trenutnog objekta kreira json datoteku
    public void saveAsJsonFile(String dirPath, String fileName) {
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
}
