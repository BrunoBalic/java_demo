package com.bb;

import java.io.File;

public class MqttPublisher {

    public static void main(String[] args) {

        // naziv konfiguracijske .json datoteke
        // * nalazi se u projektu, otvorite je da vidite kako izgleda
        // ako zelite promijeniti neke podatke onda ih u toj datoteci izmjenite
        // npr. "topic" : "t1"
        // "topic" je key, "t1" je value, mijenjate value "t1" u npr. "topic13"
        String configFilename = "WaterFlowMeter_config_1.0.json";

        // stvaranje WaterFlowMeter objekta static metodom createFromJSON
        WaterFlowMeter meter1 = WaterFlowMeter.createFromJSON(
                // * System.getProperty("user.dir") - vraca path trenutnog direktorija
                new File(System.getProperty("user.dir"), configFilename));

        // provjera jer stvoren objekt
        if (meter1 == null) {
            System.out.println("Error loading Water Flow Meter from confing file!");
            return;
        }

        // ako zelite kreirat json file WaterFlowMeter objekta
        //meter1.saveAsJsonFile(System.getProperty("user.dir"), "moj_test_file.json");
        //   * System.getProperty("user.dir") - vraca path trenutnog direktorija
        //   * tako da mi stvara file u trenutnom dir-u gdje je i projekt

        // ili mozete rucno napraviti json konfig file tako da koristite ovu
        // printPrettyJSON() metodu koja printa u komandnu liniju pa onda to rucno kopirate
        // i zaljepite u novi file i spremite ga
        meter1.printPrettyJSON();


        // * da ovo radi treba imati pokrenut mosquitto
        // * a da bi poruke negdje stigle, u drugom terminalu treba pokrenuti mosquitto_sub
        // * mosquitto_sub -h 127.0.0.1 -t t1
        //      * -t je topic definiran u json config datoteci u mom slucaju to je t1

        meter1.publishAllValuesContinuous(5000);

    }

}
