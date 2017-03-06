package kuchinke.com.airbar_tracking_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SurfaceTestActivity extends AppCompatActivity {
    TrackingSurface trackingSurface;
    private static final String TAG = "SurfaceTestActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trackingSurface= new TrackingSurface(this);
        setContentView(trackingSurface);

        trackingSurface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(TAG, "onTouch: ");
                trackingSurface.drawTrackingCircle((int)motionEvent.getX(),(int)motionEvent.getY(),-1,-1);
                return true;
            }
        });

    }
}
