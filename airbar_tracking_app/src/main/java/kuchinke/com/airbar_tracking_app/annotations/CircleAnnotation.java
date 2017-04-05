package kuchinke.com.airbar_tracking_app.annotations;

/**
 * Created by Julius on 07.03.2017.
 */

public class CircleAnnotation extends Annotation {
    int cx;
    int cy;
    int r;
    public CircleAnnotation(int cx, int cy, int r, String text){
        super(text);
        this.cx = cx;
        this.cy = cy;
        this.r = r;

    }
    @Override
    public boolean checkItemIntersection(int x, int y) {
        return (((x - cx) * (x - cx) + (y - cy)*(y - cy)) < r*r);
    }
}
