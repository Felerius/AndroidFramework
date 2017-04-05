package kuchinke.com.airbar_tracking_app.annotations;


import android.graphics.Rect;

/**
 * Created by Julius on 07.03.2017.
 */



public class RectangleAnnotation extends Annotation {
    Rect rect;
    public RectangleAnnotation(int x1, int y1, int x2, int y2, String text){
        super(text);
        this.rect = new Rect(x1,y1,x2,y2);
    }

    @Override
    public boolean checkItemIntersection(int x, int y) {
        return rect.contains(x,y);
    }

    public Rect getRect(){
        return rect;
    }
}
