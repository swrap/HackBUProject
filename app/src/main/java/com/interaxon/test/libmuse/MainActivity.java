/**
 * Example of using libmuse library on android.
 * Interaxon, Inc. 2015
 */

package com.interaxon.test.libmuse;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.SyncStateContract;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.interaxon.libmuse.Accelerometer;
import com.interaxon.libmuse.AnnotationData;
import com.interaxon.libmuse.Battery;
import com.interaxon.libmuse.ConnectionState;
import com.interaxon.libmuse.Eeg;
import com.interaxon.libmuse.LibMuseVersion;
import com.interaxon.libmuse.MessageType;
import com.interaxon.libmuse.Muse;
import com.interaxon.libmuse.MuseArtifactPacket;
import com.interaxon.libmuse.MuseConfiguration;
import com.interaxon.libmuse.MuseConnectionListener;
import com.interaxon.libmuse.MuseConnectionPacket;
import com.interaxon.libmuse.MuseDataListener;
import com.interaxon.libmuse.MuseDataPacket;
import com.interaxon.libmuse.MuseDataPacketType;
import com.interaxon.libmuse.MuseFileFactory;
import com.interaxon.libmuse.MuseFileReader;
import com.interaxon.libmuse.MuseFileWriter;
import com.interaxon.libmuse.MuseManager;
import com.interaxon.libmuse.MusePreset;
import com.interaxon.libmuse.MuseVersion;
import com.interaxon.test.libmuse.R;

import org.w3c.dom.Text;


/**
 * In this simple example MainActivity implements 2 MuseHeadband listeners
 * and updates UI when data from Muse is received. Similarly you can implement
 * listers for other data or register same listener to listen for different type
 * of data.
 * For simplicity we create Listeners as inner classes of MainActivity. We pass
 * reference to MainActivity as we want listeners to update UI thread in this
 * example app.
 * You can also connect multiple muses to the same phone and register same
 * listener to listen for data from different muses. In this case you will
 * have to provide synchronization for data members you are using inside
 * your listener.
 *
 * Usage instructions:
 * 1. Enable bluetooth on your device
 * 2. Pair your device with muse
 * 3. Run this project
 * 4. Press Refresh. It should display all paired Muses in Spinner
 * 5. Make sure Muse headband is waiting for connection and press connect.
 * It may take up to 10 sec in some cases.
 * 6. You should see EEG and accelerometer data as well as connection status,
 * Version information and MuseElements (alpha, beta, theta, delta, gamma waves)
 * on the screen.
 */
public class MainActivity extends Activity implements OnClickListener {
    public static int y_axis = 0;
    public static int x_axis = 0;
    public static boolean eyesOpen = true;
    /**
     * Connection listener updates UI with new connection status and logs it.
     */
    class ConnectionListener extends MuseConnectionListener {

        final WeakReference<Activity> activityRef;

        ConnectionListener(final WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseConnectionPacket(MuseConnectionPacket p) {
            final ConnectionState current = p.getCurrentConnectionState();
            Log.i("Muse Headband", "Attempting to change connection. Current state: "
            + current.toString());
            Activity activity = activityRef.get();
            // UI thread is used here only because we need to update
            // TextView values. You don't have to use another thread, unless
            // you want to run disconnect() or connect() from connection packet
            // handler. In this case creating another thread is required.
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(current == ConnectionState.DISCONNECTED)
                        {
                            TextView connectT = (TextView)findViewById(R.id.connectionStatus);
                            connectT.setText("Attempting to Connect...");
                            ProgressBar progressConnection = (ProgressBar)findViewById(R.id.progressConnection);
                            progressConnection.setVisibility(View.VISIBLE);
                            final List<Muse> pairedMuses = MuseManager.getPairedMuses();
                            Log.i("Muse", "Size: " + pairedMuses.size());
                            if(pairedMuses.size() > 0)
                            {
                                muse = pairedMuses.get(0);
                                runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        muse.runAsynchronously();
                                    } catch (Exception e) {
                                        Log.e("Muse Headband", e.toString());
                                    }
                                }
                            });
                            }
                            else
                            {
                                connectT.setText("No bluetooth Muse devices paired...");
                            }
                        }
                        else if(current == ConnectionState.CONNECTING)
                        {
                            TextView connectT = (TextView)findViewById(R.id.connectionStatus);
                            connectT.setText("Almost Connected!...");
                            ProgressBar progressConnection = (ProgressBar)findViewById(R.id.progressConnection);
                            progressConnection.setVisibility(View.VISIBLE);
                        }
                        else if(current == ConnectionState.CONNECTED)
                        {
                            TextView connectT = (TextView)findViewById(R.id.connectionStatus);
                            connectT.setText("You are connected to device: " + muse.getName().toString());
                            ProgressBar progressConnection = (ProgressBar)findViewById(R.id.progressConnection);
                            progressConnection.setVisibility(View.INVISIBLE);

                            Button easyB = (Button) findViewById(R.id.easyButton);
                            easyB.setEnabled(true);
                            Button mediumB = (Button) findViewById(R.id.mediumButton);
                            mediumB.setEnabled(true);
                            Button hardB = (Button) findViewById(R.id.hardButton);
                            hardB.setEnabled(true);
                        }
                        else if(current == ConnectionState.UNKNOWN)
                        {
                            TextView connectT = (TextView)findViewById(R.id.connectionStatus);
                            connectT.setText("Device Unknown? This is no bueno!");
                            ProgressBar progressConnection = (ProgressBar)findViewById(R.id.progressConnection);
                            progressConnection.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }
    }

    /**
     * Data listener will be registered to listen for: Accelerometer,
     * Eeg and Relative Alpha bandpower packets. In all cases we will
     * update UI with new values.
     * We also will log message if Artifact packets contains "blink" flag.
     * DataListener methods will be called from execution thread. If you are
     * implementing "serious" processing algorithms inside those listeners,
     * consider to create another thread.
     */
    class DataListener extends MuseDataListener {

        final WeakReference<Activity> activityRef;
        private MuseFileWriter fileWriter;

        DataListener(final WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseDataPacket(MuseDataPacket p) {
            Log.i("Muse","Data packet");
            switch (p.getPacketType()) {
                case EEG:
//                    updateEeg(p.getValues());
                    break;
                case ACCELEROMETER:
//                    updateAccelerometer(p.getValues());
                    break;
                case ALPHA_RELATIVE:
//                    updateAlphaRelative(p.getValues());
                    break;
                case BATTERY:
//                    updateBattery(p.getValues());
                    fileWriter.addDataPacket(1, p);
                    // It's library client responsibility to flush the buffer,
                    // otherwise you may get memory overflow.
                    if (fileWriter.getBufferedMessagesSize() > 8096)
                        fileWriter.flush();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void receiveMuseArtifactPacket(MuseArtifactPacket p) {
            if (p.getHeadbandOn() && p.getBlink()) {
                updateArtifacts(p);
            }
        }

        protected int boolToInt(boolean b){
        	if(b==true)
        		return 1;
        	return 0;
        }

        private void updateArtifacts(final MuseArtifactPacket p) {
            Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    	if(eyesOpen == true){
                    		y_axis = boolToInt(p.getBlink());
                    		eyesOpen = false;
                    	} else{
                    		eyesOpen = true;
                    	}
//                    	TextView blinkText = (TextView) findViewById(R.id.blinkText);
//                        blinkText.setText(String.format(
//                            "%s%d", blinkText.getText().toString(), boolToInt(p.getBlink())));
                    }
                });
            }
        }

        private void updateAccelerometer(final ArrayList<Double> data) {
            Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    	x-axis = data.get(Accelerometer.LEFT_RIGHT.ordinal());
//                        TextView acc_x = (TextView) findViewById(R.id.acc_x);
//                        TextView acc_y = (TextView) findViewById(R.id.acc_y);
//                        TextView acc_z = (TextView) findViewById(R.id.acc_z);
//                        acc_x.setText(String.format(
//                                "%6.2f", data.get(Accelerometer.FORWARD_BACKWARD.ordinal())));
//                        acc_y.setText(String.format(
//                                "%6.2f", data.get(Accelerometer.UP_DOWN.ordinal())));
//                        acc_z.setText(String.format(
//                                "%6.2f", data.get(Accelerometer.LEFT_RIGHT.ordinal())));
                    }
                });
            }
        }

//        private void updateEeg(final ArrayList<Double> data) {
//            Activity activity = activityRef.get();
//            if (activity != null) {
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                         TextView tp9 = (TextView) findViewById(R.id.eeg_tp9);
//                         TextView fp1 = (TextView) findViewById(R.id.eeg_fp1);
//                         TextView fp2 = (TextView) findViewById(R.id.eeg_fp2);
//                         TextView tp10 = (TextView) findViewById(R.id.eeg_tp10);
//                         tp9.setText(String.format(
//                            "%6.2f", data.get(Eeg.TP9.ordinal())));
//                         fp1.setText(String.format(
//                            "%6.2f", data.get(Eeg.FP1.ordinal())));
//                         fp2.setText(String.format(
//                            "%6.2f", data.get(Eeg.FP2.ordinal())));
//                         tp10.setText(String.format(
//                            "%6.2f", data.get(Eeg.TP10.ordinal())));
//                    }
//                });
//            }
//        }

//        private void updateAlphaRelative(final ArrayList<Double> data) {
//            Activity activity = activityRef.get();
//            if (activity != null) {
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        TextView elem1 = (TextView) findViewById(R.id.elem1);
//                        TextView elem2 = (TextView) findViewById(R.id.elem2);
//                        TextView elem3 = (TextView) findViewById(R.id.elem3);
//                        TextView elem4 = (TextView) findViewById(R.id.elem4);
//                        elem1.setText(String.format(
//                                "%6.2f", data.get(Eeg.TP9.ordinal())));
//                        elem2.setText(String.format(
//                                "%6.2f", data.get(Eeg.FP1.ordinal())));
//                        elem3.setText(String.format(
//                                "%6.2f", data.get(Eeg.FP2.ordinal())));
//                        elem4.setText(String.format(
//                                "%6.2f", data.get(Eeg.TP10.ordinal())));
//                    }
//                });
//            }
//        }

        private void updateBattery(final ArrayList<Double> data)
        {
        }

        public void setFileWriter(MuseFileWriter fileWriter) {
            this.fileWriter  = fileWriter;
        }
    }

    private Muse muse = null;
    private ConnectionListener connectionListener = null;
    private DataListener dataListener = null;
    private boolean dataTransmission = true;
    private MuseFileWriter fileWriter = null;
    private boolean calibrated = false;

    public MainActivity() {
        // Create listeners and pass reference to activity to them
        WeakReference<Activity> weakActivity =
                                new WeakReference<Activity>(this);

        connectionListener = new ConnectionListener(weakActivity);
        dataListener = new DataListener(weakActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Button easyB = (Button) findViewById(R.id.easyButton);
        easyB.setOnClickListener(this);
        Button mediumB = (Button) findViewById(R.id.mediumButton);
        mediumB.setOnClickListener(this);
        Button hardB = (Button) findViewById(R.id.hardButton);
        hardB.setOnClickListener(this);

        File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        fileWriter = MuseFileFactory.getMuseFileWriter(
                     new File(dir, "new_muse_file.muse"));
        Log.i("Muse Headband", "libmuse version=" + LibMuseVersion.SDK_VERSION);
        fileWriter.addAnnotationString(1, "MainActivity onCreate");
        dataListener.setFileWriter(fileWriter);

        //attempt to connect -jes
        MuseManager.refreshPairedMuses();
        final List<Muse> pairedMuses = MuseManager.getPairedMuses();
        Log.i("Muse", "Size: " + pairedMuses.size());
        if(pairedMuses.size() > 0)
        {
            muse = pairedMuses.get(0);
            configureLibrary();
        }
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.easyButton)
        {
            startActivity(new Intent(getApplicationContext(),game.class));
        }
        else if(v.getId() == R.id.mediumButton)
        {
            startActivity(new Intent(getApplicationContext(),game.class));
        }
        else if(v.getId() == R.id.hardButton)
        {
            startActivity(new Intent(getApplicationContext(),game.class));
        }
    }

    private void configureLibrary() {
        muse.registerConnectionListener(connectionListener);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.ACCELEROMETER);
        muse.registerDataListener(dataListener,
                                  MuseDataPacketType.EEG);
        muse.registerDataListener(dataListener,
                                  MuseDataPacketType.ALPHA_RELATIVE);
        muse.registerDataListener(dataListener,
                                  MuseDataPacketType.ARTIFACTS);
        muse.registerDataListener(dataListener,
                                  MuseDataPacketType.BATTERY);
        muse.setPreset(MusePreset.PRESET_14);
        muse.enableDataTransmission(dataTransmission);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    muse.runAsynchronously();
                } catch (Exception e) {
                    Log.e("Muse Headband", e.toString());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
