package net.kaoriya.rslog;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;

public class MainActivity extends Activity
{
    public static final String TAG = "rslog";
    public static final String ACTION_LOG_STRENGTH = "LogStrength";

    private PhoneStateListener phoneStateListener = null;
    private PendingIntent pendingIntent = null;
    private BroadcastReceiver broadcastReceiver = null;
    private int lastRssi = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TelephonyManager manager = (TelephonyManager)getSystemService(
                Context.TELEPHONY_SERVICE);
        this.phoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength s)
            {
                log(s);
            }
        };
        manager.listen(this.phoneStateListener,
                PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_LOG_STRENGTH.equals(intent.getAction())) {
                    logWifiStrength();
                }
            }
        };
        registerReceiver(this.broadcastReceiver,
                new IntentFilter(ACTION_LOG_STRENGTH));

        AlarmManager alarm = (AlarmManager)getSystemService(
                Context.ALARM_SERVICE);
        this.pendingIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(ACTION_LOG_STRENGTH), 0);
        alarm.setRepeating(AlarmManager.RTC, 1000, 3000, this.pendingIntent);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (this.pendingIntent != null) {
            AlarmManager alarm = (AlarmManager)getSystemService(
                    Context.ALARM_SERVICE);
            alarm.cancel(this.pendingIntent);
            this.pendingIntent = null;
        }
        if (this.broadcastReceiver != null) {
            unregisterReceiver(this.broadcastReceiver);
            this.broadcastReceiver = null;
        }
        if (this.phoneStateListener != null) {
            TelephonyManager manager = (TelephonyManager)getSystemService(
                    Context.TELEPHONY_SERVICE);
            manager.listen(this.phoneStateListener,
                    PhoneStateListener.LISTEN_NONE);
            this.phoneStateListener = null;
        }
    }

    private void log(SignalStrength signalStrength)
    {
        String rat = signalStrength.isGsm() ? "gsm" : "cdma";
        if (rat == "gsm") {
            Log.d(TAG, "Strength(" + rat + "):"
                    + " rssi=" + String.valueOf(signalStrength.getGsmSignalStrength())
                    + ", bit error rate=" + String.valueOf(signalStrength.getGsmBitErrorRate()));
        } else {
            Log.d(TAG, "Strength(" + rat + "):"
                    + " cdma rssi=" + String.valueOf(signalStrength.getCdmaDbm())
                    + ", cdma ecio=" + String.valueOf(signalStrength.getCdmaEcio() / 10)
                    + ", evdo rssi=" + String.valueOf(signalStrength.getEvdoDbm())
                    + ", evdo ecio=" + String.valueOf(signalStrength.getEvdoEcio() / 10)
                    + ", evdo snr=" + String.valueOf(signalStrength.getEvdoSnr()));
        }
    }

    private void logWifiStrength()
    {
        WifiManager manager = (WifiManager)getSystemService(
                Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        int curr = info.getRssi();
        if (curr != this.lastRssi) {
            Log.d(TAG, "Strength(WiFi)=" + curr);
            this.lastRssi = curr;
        }
    }

}
