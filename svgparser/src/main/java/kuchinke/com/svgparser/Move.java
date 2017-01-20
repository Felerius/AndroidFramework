package kuchinke.com.svgparser;

import android.graphics.Point;
import android.graphics.PointF;


/**
 * Created by Julius on 19.01.2017.
 */
public class Move extends Instruction {

    int x,y;

    public Move(int x, int y){
        this.x=x;
        this.y=y;
    }

    @Override
    public String buildInstruction(Mode mode) {
        String command=null;
        switch (mode){
            case GPGL:{
                command="M "+y+","+x+",";

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
        x*=scale.x;
        y*=scale.y;
    }

    @Override
    public void offsetBy(Point offset) {
        this.x+=offset.x;
        this.y+=offset.y;
    }
}
