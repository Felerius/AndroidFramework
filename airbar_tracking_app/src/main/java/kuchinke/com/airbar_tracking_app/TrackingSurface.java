package kuchinke.com.airbar_tracking_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Adrian on 01.03.2017.
 */

public class TrackingSurface extends SurfaceView {
    int MAX_X=3452;
    int MAX_Y=2000;
    private static final String TAG = "TrackingSurface";
    private SurfaceHolder holder;
    private Canvas canvas;
    public TrackingSurface(final Context context) {
        super(context);
        init(context);


    }

    public TrackingSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TrackingSurface(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public TrackingSurface(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void drawTrackingCircle(int x1, int y1, int x2, int y2){
        int w=getWidth();
        int h=getHeight();

        int resX1=w*x1/MAX_Y;
        int resY1=h-h*y1/MAX_X;

        int resX2=w*x2/MAX_Y;
        int resY2=h-h*y2/MAX_X;

        Log.d(TAG, "drawTrackingCircle: locking "+x1 + " "+y1);
        canvas=holder.lockCanvas();

        canvas.drawColor(Color.WHITE);
        Paint paint =new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);
        canvas.drawCircle(resX1,resY1,10,paint);
        if(x2>-1 && y2 >-1)canvas.drawCircle(resX2,resY2,10,paint);

        holder.unlockCanvasAndPost(canvas);

    }

    void init(Context context){
        holder=getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            }
        });
    }
}
