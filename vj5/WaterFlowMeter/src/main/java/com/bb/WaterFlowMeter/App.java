package com.bb.WaterFlowMeter;

import com.bb.WaterFlowMeter.BusinessLogic.WaterFlowMeter;
import com.bb.WaterFlowMeter.WebClient.WebClientImplementation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;

public class App {

    public static void main(String[] args) {
        
        String configFilename = "WaterFlowMeter_config_1.0.json";

        WaterFlowMeter meter1 = WaterFlowMeter.createFromJSON(
                new File(System.getProperty("user.dir"), configFilename)
        );

        if (meter1 == null) {
            System.out.println("Error loading Water Flow Meter from confing file!");
            return;
        }

        SecurityCredentials credentials = new SecurityCredentials(
                "MqttProxy_vj04",
                "44e358fc-1a54-4e98-afa7-8c93e985c945",
                "bruno",
                "tmppassword",
                "http://localhost:8080/auth/realms/realm_vjezba_04/protocol/openid-connect/token"
        );
        long intervalSec = 50;

        // ovo je metoda za vj3
        //meter1.postValuesToWebUrlContinuous(5000);

        // ovo je metoda za vj5
        meter1.postValuesToSecuredWebUrlContinuous(meter1.getWebURL(), credentials, intervalSec);

    }

}
