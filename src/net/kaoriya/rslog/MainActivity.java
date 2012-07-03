package net.kaoriya.rslog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

public class MainActivity extends Activity
{
    public static final String TAG = "rslog";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TelephonyManager manager = (TelephonyManager)getSystemService(
                Context.TELEPHONY_SERVICE);
        manager.listen(new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength s)
            {
                log(s);
            }
        }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    private void log(SignalStrength signalStrength)
    {
        Log.d(TAG, "Strength(3G)=" + signalStrength.toString());
    }

}
