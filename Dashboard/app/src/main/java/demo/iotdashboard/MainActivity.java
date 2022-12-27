package demo.iotdashboard;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
    Button saveButton, cancelButton;
    EditText maxTemperature, minTemperature, maxHumidity, minHumidity;

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
                    txtTemp.setText(message.toString() + "℃");
                    if(Integer.parseInt(message.toString()) > 30){
                        txtTemp.setText("over");
                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        int notifyId = 1;
                        String channelId = "some_channel_id";

                        Notification notification = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            notification = new Notification.Builder(MainActivity.this)
                                    .setContentTitle("IoT Dashboard")
                                    .setContentText("Overheat!")
                                    .setSmallIcon(R.drawable.icon)
                                    .setChannelId(channelId)
                                    .build();
                        }

                        notificationManager.notify(notifyId, notification);

                    }
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

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "some_channel_id";
        CharSequence channelName = "Some Channel";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(notificationChannel);
        }

    }
}