package com.polar.polarsdkecghrdemo;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYPlot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polar.polarsdkedghrdemo.mqtt.EcgMqttClient;

import org.reactivestreams.Publisher;

import java.util.Set;
import java.util.UUID;
import java.util.TimeZone;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import polar.com.sdk.api.PolarBleApi;
import polar.com.sdk.api.PolarBleApiCallback;
import polar.com.sdk.api.PolarBleApiDefaultImpl;
import polar.com.sdk.api.errors.PolarInvalidArgument;
import polar.com.sdk.api.model.PolarDeviceInfo;
import polar.com.sdk.api.model.PolarEcgData;
import polar.com.sdk.api.model.PolarHrData;
import polar.com.sdk.api.model.PolarSensorSetting;
import java.util.Calendar;
import java.util.Date;
import java.time.*; // Este paquete contiene LocalDate, LocalTime y LocalDateTime.
import java.text.SimpleDateFormat;


public class ECGActivity extends AppCompatActivity implements PlotterListener {
    private static final String TAG = "ECGActivity";

    private PolarBleApi api;
    private TextView textViewHR;
    private TextView textViewFW;
    private XYPlot plot;
    private Plotter plotter;

    private Disposable ecgDisposable = null;
    private final Context classContext = this;
    private String deviceId;
    private EcgMqttClient mqttClient;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg);
        deviceId = getIntent().getStringExtra("id");
        textViewHR = findViewById(R.id.info);
        textViewFW = findViewById(R.id.fw);

        plot = findViewById(R.id.plot);

        api = PolarBleApiDefaultImpl.defaultImplementation(this,
                PolarBleApi.FEATURE_POLAR_SENSOR_STREAMING |
                        PolarBleApi.FEATURE_BATTERY_INFO |
                        PolarBleApi.FEATURE_DEVICE_INFO |
                        PolarBleApi.FEATURE_HR);
        api.setApiCallback(new PolarBleApiCallback() {
            @Override
            public void blePowerStateChanged(boolean b) {
                Log.d(TAG, "BluetoothStateChanged " + b);
            }

            @Override
            public void deviceConnected(@NonNull PolarDeviceInfo s) {
                Log.d(TAG, "Device connected " + s.deviceId);
                Toast.makeText(classContext, R.string.connected, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void deviceConnecting(@NonNull PolarDeviceInfo polarDeviceInfo) {

            }

            @Override
            public void deviceDisconnected(@NonNull PolarDeviceInfo s) {
                Log.d(TAG, "Device disconnected " + s);
            }

            @Override
            public void streamingFeaturesReady(@NonNull final String identifier,
                                               @NonNull final Set<PolarBleApi.DeviceStreamingFeature> features) {

                for (PolarBleApi.DeviceStreamingFeature feature : features) {
                    Log.d(TAG, "Streaming feature is ready: " + feature);
                    switch (feature) {
                        case ECG:
                            streamECG();
                            break;
                        case ACC:
                        case MAGNETOMETER:
                        case GYRO:
                        case PPI:
                        case PPG:
                            break;
                    }
                }
            }

            @Override
            public void hrFeatureReady(@NonNull String s) {
                Log.d(TAG, "HR Feature ready " + s);
            }

            @Override
            public void disInformationReceived(@NonNull String s, @NonNull UUID u, @NonNull String s1) {
                if (u.equals(UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb"))) {
                    String msg = "Firmware: " + s1.trim();
                    Log.d(TAG, "Firmware: " + s + " " + s1.trim());
                    textViewFW.append(msg + "\n");
                }
            }

            @Override
            public void batteryLevelReceived(@NonNull String s, int i) {
                String msg = "ID: " + s + "\nBattery level: " + i;
                Log.d(TAG, "Battery level " + s + " " + i);
//                Toast.makeText(classContext, msg, Toast.LENGTH_LONG).show();
                textViewFW.append(msg + "\n");
            }

            @Override
            public void hrNotificationReceived(@NonNull String s, @NonNull PolarHrData polarHrData) {
                Date  dateString = new Date();
                //Log.d(TAG, tiempo);
                String strDate = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss.SS").format(dateString);
                Log.d(TAG, "HR " + dateString+ ","+polarHrData.hr);
                textViewHR.setText(String.valueOf(polarHrData.hr));
            }

            @Override
            public void polarFtpFeatureReady(@NonNull String s) {
                Log.d(TAG, "Polar FTP ready " + s);
            }
        });
        try {
            api.connectToDevice(deviceId);
        } catch (PolarInvalidArgument a) {
            a.printStackTrace();
        }

        plotter = new Plotter("ECG");
        plotter.setListener(this);

        plot.addSeries(plotter.getSeries(), plotter.getFormatter());
        plot.setRangeBoundaries(-3.3, 3.3, BoundaryMode.FIXED);
        plot.setRangeStep(StepMode.INCREMENT_BY_FIT, 0.55);
        plot.setDomainBoundaries(0, 500, BoundaryMode.GROW);
        plot.setLinesPerRangeLabel(2);
    }

    @Override
    public void onDestroy() {

        mqttClient.disconnectFromMqttServer();
        super.onDestroy();
        api.shutDown();

    }

    public void streamECG() {
        if (ecgDisposable == null) {
            ecgDisposable =




                    api.requestStreamSettings(deviceId, PolarBleApi.DeviceStreamingFeature.ECG)
                            .toFlowable()
                            .flatMap((Function<PolarSensorSetting, Publisher<PolarEcgData>>) sensorSetting -> api.startEcgStreaming(deviceId, sensorSetting.maxSettings()))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    polarEcgData -> {
                                        long timeStamp = polarEcgData.timeStamp;
                                      Date  dateString = new Date();
                                        String tiempo = convertTimeWithTimeZome(timeStamp);
                                         //Log.d(TAG, tiempo);
                                        String strDate = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss.SS").format(dateString);

                                        String logString = strDate+","+ polarEcgData.samples;


                                       // Log.d(TAG, logString);


                                        for (Integer data : polarEcgData.samples) {
                                            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss.SS");
                                            Date date =  new Date();
                                           // System.out.println("Time in milliseconds using Date class: " + timeMilli);
                                           // System.out.println("polarEcgData.samples " + polarEcgData.samples);
                                            String log =strDate + "," + data;
                                            Log.d(null, log);

                                            HeartData heartData = new HeartData();
                                            heartData.setEcg((float) ((float) data / 500));
                                            heartData.setHeartRate(new Float(67));

                                            if (mqttClient == null) {
                                                 mqttClient = new EcgMqttClient();
                                                mqttClient.connectToMqttServer();
                                            }

                                            ObjectMapper Obj = new ObjectMapper();

                                            String jsonStr = Obj.writeValueAsString(heartData);

                                            mqttClient.publishData(jsonStr);

                                           plotter.sendSingleSample((float) ((float) data / 500));
                                        }



                                    },
                                    throwable -> {
                                        Log.e(TAG,
                                                "" + throwable.getLocalizedMessage());
                                        ecgDisposable = null;
                                    },
                                    () -> Log.d(TAG, "complete")
                            );
        } else {
            // NOTE stops streaming if it is "running"
            ecgDisposable.dispose();
            ecgDisposable = null;
        }
    }

    @Override
    public void update() {
        runOnUiThread(() -> plot.redraw());
    }

    public String convertTimeWithTimeZome(long time){
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(time);
        return (cal.get(Calendar.YEAR) + " " + (cal.get(Calendar.MONTH) + 1) + " "
                + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR_OF_DAY) + ":"
                + cal.get(Calendar.MINUTE));

    }
}
