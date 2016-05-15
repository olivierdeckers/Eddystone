package be.olivierdeckers.eddystone;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BeaconManager beaconManager;
    private Region region;
    private Map<Integer, Position> beaconLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView textView = (TextView) findViewById(R.id.textField);

        beaconLocations = new HashMap<Integer, Position>();
        beaconLocations.put(56946, new Position(0, 0));
        beaconLocations.put(29204, new Position(1, 0));
        beaconLocations.put(3481, new Position(0, 1));

        beaconManager = new BeaconManager(this);
        region = new Region("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (list.size() < 3) {
                    return;
                }

                Position result = getPositionEstimoteDemo(list);
                Position result2 = getPosition(list);
                Log.d("ESTIMOTE", "position: " + result);
                textView.setText(result.toString() + " " + result2.toString());
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private Position getPosition(List<Beacon> list) {
        Position result = new Position();
        double totalWeight = 0;
        for (Beacon beacon : list) {
            int major = beacon.getMajor();
            Position pos = beaconLocations.get(major);

            int rssi = beacon.getRssi();
            double weight = Math.pow(10f, rssi/20f);
            totalWeight += weight;
            result = result.add(pos.scale((float) weight));
        }
        return result.scale((float) (1 / totalWeight));
    }

    private Position getPositionEstimoteDemo(List<Beacon> list) {
        Position result = new Position();
        double totalWeight = 0;
        for (Beacon beacon : list) {
            int major = beacon.getMajor();
            Position pos = beaconLocations.get(major);

            double distance = computeAccuracy(beacon.getRssi(), beacon.getMeasuredPower());
            double weight = 1.0 / distance;
            totalWeight += weight;
            result = result.add(pos.scale((float) weight));
        }
        return result.scale((float) (1 / totalWeight));
    }

    private static double computeAccuracy(int rssi, int measuredPower) {
        if(rssi == 0) {
            return -1.0D;
        } else {
            double ratio = (double)rssi / (double)measuredPower;
            double rssiCorrection = 0.96D + Math.pow((double)Math.abs(rssi), 3.0D) % 10.0D / 150.0D;
            return ratio <= 1.0D?Math.pow(ratio, 9.98D) * rssiCorrection:(0.103D + 0.89978D * Math.pow(ratio, 7.71D)) * rssiCorrection;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        beaconManager.stopRanging(region);
    }
}
