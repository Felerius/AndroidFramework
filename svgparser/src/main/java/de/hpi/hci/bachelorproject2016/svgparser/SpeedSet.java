package de.hpi.hci.bachelorproject2016.svgparser;

import android.graphics.Point;
import android.graphics.PointF;



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
