package kuchinke.com.svgparser;

import android.graphics.Point;
import android.graphics.PointF;



/**
 * Created by Julius on 19.01.2017.
 */
public class SpeedSet extends Instruction{
    int speed=0;
    public SpeedSet(int speed){
        this.speed=speed;
    }


    @Override
    public String buildInstruction(Mode mode) {
        String command=null;
        switch (mode){
            case GPGL:{
                command="! "+speed;

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

    }

    @Override
    public void offsetBy(Point offset) {

    }
}
