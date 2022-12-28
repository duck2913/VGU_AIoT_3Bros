package demo.iotdashboard;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    MQTTHelper mqttHelper;
    TextView txtTemp, txtHumi, txtStatus;
    ToggleButton btnLED, btnPUMP;
    AlertDialog.Builder dialogBuilder;
    AlertDialog dialog;
    Button saveButton, cancelButton;
    EditText maxTemperature, minTemperature, maxHumidity, minHumidity;
    int maxTemp = -1, minTemp = -1, maxHumid = -1, minHumid = -1;

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
                if(topic.contains("sensor1")) {
                    txtTemp.setText(message.toString() + "℃");
                    if(maxTemp == -1 || minTemp == -1) return;
                    int currentTemp = Integer.parseInt(message.toString());
                    if(currentTemp > minTemp && currentTemp < maxTemp) {
                        sendDataMQTT("3Bros/feeds/actuator1", "0");
                        sendDataMQTT("3Bros/feeds/heating_system", "0");
                        return;
                    };
                    createNotification(currentTemp);
                    if(currentTemp > maxTemp ) {
                        sendDataMQTT("3Bros/feeds/actuator1", "1");
                        sendDataMQTT("3Bros/feeds/heating_system", "0");
                    }
                    if(currentTemp < minTemp ) {
                        sendDataMQTT("3Bros/feeds/heating_system", "1");
                        sendDataMQTT("3Bros/feeds/actuator1", "0");
                    }
                }
                else if(topic.contains("sensor2")) {
                    txtHumi.setText(message.toString() + "%");
                }
                else if(topic.contains("visiondetection")){
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

        minTemperature = (EditText) settingPopupView.findViewById(R.id.minTemperature);
        maxTemperature = (EditText) settingPopupView.findViewById(R.id.maxTemperature);
        minHumidity = (EditText) settingPopupView.findViewById(R.id.minHumidity);
        maxHumidity = (EditText) settingPopupView.findViewById(R.id.maxHumidity);

        saveButton = (Button) settingPopupView.findViewById(R.id.saveButton);
        cancelButton = (Button) settingPopupView.findViewById(R.id.cancelButton);

        dialogBuilder.setView(settingPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        maxTemperature.setFocusable(true);
        maxTemperature.setFocusableInTouchMode(true);
        maxTemperature.requestFocus();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // define save button function here
                int minTempInput =  Integer.parseInt(minTemperature.getText().toString());
                int maxTempInput = Integer.parseInt(maxTemperature.getText().toString());
                int minHumidInput = Integer.parseInt(minHumidity.getText().toString());
                int maxHumidInput = Integer.parseInt(maxHumidity.getText().toString());
                maxTemp = maxTempInput;
                minTemp = minTempInput;
                maxHumid = maxHumidInput;
                minHumid = minHumidInput;
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());

        // setting width to 90% of display
        layoutParams.width = (int) (displayMetrics.widthPixels * 0.9f);

        // setting height to 90% of display
        layoutParams.height = (int) (displayMetrics.heightPixels * 0.7f);
        dialog.getWindow().setAttributes(layoutParams);
    }

    public void createNotification(int currentTemp){
        String id = "my_channel_id_01";
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel =manager.getNotificationChannel(id);
            if(channel ==null)
            {
                channel = new NotificationChannel(id,"Channel Title", NotificationManager.IMPORTANCE_HIGH);
                //config nofication channel
                channel.setDescription("[Channel description]");
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100,1000,200,340});
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                manager.createNotificationChannel(channel);
            }
        }
        Intent notificationIntent = new Intent(this,MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,id)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("The temperature is not okay")
                .setContentText(currentTemp > maxTemp ?
                        "Current temperature is higher than " + maxTemp + "!" :"Current temperature is lower than " + minTemp + "!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{100,1000,200,340})
                .setAutoCancel(false)//true touch on notificaiton menu dismissed, but swipe to dismiss
                .setTicker("Nofiication");
        builder.setContentIntent(contentIntent);
        NotificationManagerCompat m = NotificationManagerCompat.from(getApplicationContext());
        //id to generate new notification in list notifications menu
        m.notify(new Random().nextInt(),builder.build());
    }
}