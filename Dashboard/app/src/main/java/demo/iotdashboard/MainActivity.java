package demo.iotdashboard;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {
    MQTTHelper mqttHelper;
    TextView txtTemp, txtHumi, txtStatus;
    ToggleButton btnLED, btnPUMP;
    AlertDialog.Builder dialogBuilder;
    AlertDialog dialog;
    Button setting_save, setting_cancel;
    EditText temperatureLimit, humidityLimit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        startMQTT();

        txtStatus = findViewById(R.id.txtStatus);
        txtTemp = findViewById(R.id.txtTemperature);
        txtHumi = findViewById(R.id.txtHumidity);
        btnLED = findViewById(R.id.btnLED);
        btnPUMP = findViewById(R.id.btnPUMP);
        btnLED.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b == true) {
                    sendDataMQTT("3Bros/feeds/actuator1", "1");
                } else {
                    sendDataMQTT("3Bros/feeds/actuator1", "0");
                }
            }
        });

        btnPUMP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b == true) {
                    sendDataMQTT("3Bros/feeds/actuator2", "1");
                } else {
                    sendDataMQTT("3Bros/feeds/actuator2", "0");
                }
            }
        });
    }

    public void startMQTT(){
        mqttHelper = new MQTTHelper(this);
        // Lambda instruction or Asynchronous instruction
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("TEST", topic + " *** " + message.toString());
                if(topic.contains("sensor1")) {
                    txtTemp.setText(message.toString());
                } else if(topic.contains("sensor2")) {
                    txtHumi.setText(message.toString() + "%");
                } else if(topic.contains("visiondetection")){
                    txtStatus.setText(message.toString());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    public void sendDataMQTT(String topic, String value){
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(false);

        byte[] b = value.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        try {
            mqttHelper.mqttAndroidClient.publish(topic, msg);
        }catch (MqttException e){

        }

    }

    public void changeLayout(View view){
        dialogBuilder = new AlertDialog.Builder(this);
        final View settingPopupView = getLayoutInflater().inflate(R.layout.setting_layout, null);


        dialogBuilder.setView(settingPopupView);
        dialog = dialogBuilder.create();
        dialog.show();
    }
}