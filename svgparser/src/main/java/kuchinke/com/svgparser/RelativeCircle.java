package kuchinke.com.svgparser;

import android.graphics.Point;
import android.graphics.PointF;


/**
 * Created by Julius on 19.01.2017.
 */
public class RelativeCircle extends Instruction {
    int radius;
    int startAngle=0;
    int endAngle=3600;

    public RelativeCircle(int radius){
        this.radius=radius;

    }
    public RelativeCircle(int radius,int startAngle, int endAngle){
        this.radius=radius;
        this.startAngle=startAngle;
        this.endAngle=endAngle;


    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public String buildInstruction(Mode mode) {
        String command=null;
        switch (mode){
            case GPGL:{

                command="] "+radius+","+radius+","+startAngle+","+endAngle;

                break;
            }
            case  HPGL:{

                break;
            }
        }
        return command;
    }

    @Override
    public void scaleBy(PointF scale) {
        radius*=scale.x;
    }

    @Override
    public void offsetBy(Point offset) {

    }
}
