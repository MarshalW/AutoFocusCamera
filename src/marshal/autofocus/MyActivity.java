package marshal.autofocus;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MyActivity extends Activity implements SurfaceHolder.Callback {

    SurfaceView surfaceView;

    Camera camera;

    Timer timer;

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

        camera.startPreview();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean b, Camera camera) {
                        Log.d("autofocus",">>>>call camera auto focus");
                    }
                });
            }
        };
        timer.schedule(task, 1000, 1000);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        timer.cancel();
        camera.stopPreview();
        camera.release();
        camera = null;
    }
}
