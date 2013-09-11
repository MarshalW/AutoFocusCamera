package marshal.autofocus;

import android.app.Activity;
import android.hardware.*;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Timer;
import java.util.List;
import java.util.TimerTask;

public class MyActivity extends Activity implements SurfaceHolder.Callback, SensorEventListener {

    SurfaceView surfaceView;

    Camera camera;

    Timer timer;

    private SensorManager sensorManager;

    boolean needAutoFocus = true;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        this.surfaceView.getHolder().setKeepScreenOn(true);
        this.surfaceView.getHolder().addCallback(this);
        timer = new Timer();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        camera = Camera.open();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        camera.setDisplayOrientation(90);

        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        Camera.Parameters params = camera.getParameters();

        List<String> focusModes = params.getSupportedFocusModes();

        String CAF_PICTURE = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
                CAF_VIDEO = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
                supportedMode = focusModes
                        .contains(CAF_PICTURE) ? CAF_PICTURE : focusModes
                        .contains(CAF_VIDEO) ? CAF_VIDEO : "";

        if (!supportedMode.equals("")) {
            params.setFocusMode(supportedMode);
            camera.setParameters(params);
        }

//        camera.setAutoFocusMoveCallback(new Camera.AutoFocusMoveCallback() {
//            @Override
//            public void onAutoFocusMoving(boolean b, Camera camera) {
//                Log.d("autofocus","auto focus moving >>>>>>>>>>>>>>>>>>>>>>");
//            }
//        });

        camera.startPreview();

//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                if(needAutoFocus){
//                    camera.autoFocus(new Camera.AutoFocusCallback() {
//                        @Override
//                        public void onAutoFocus(boolean b, Camera camera) {
////                        Log.d("autofocus",">>>>call camera auto focus");
//                        }
//                    });
//                    needAutoFocus=false;
//                }
//            }
//        };
//        timer.schedule(task, 1000, 1000);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        timer.cancel();
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(sensorEvent);
        }
    }

    private void getAccelerometer(SensorEvent sensorEvent) {
        float[] values = sensorEvent.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);

        if (accelationSquareRoot >= 1.5) {
            Log.d("autofocus", ">>>>>>>>>>>>>sensor accelation");
            needAutoFocus = true;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
