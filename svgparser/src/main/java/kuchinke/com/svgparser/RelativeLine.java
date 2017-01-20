package kuchinke.com.svgparser;

import android.graphics.Point;
import android.graphics.PointF;

/**
 * Created by Julius on 19.01.2017.
 */
public class RelativeLine extends Instruction {

    int dX,dY;
    public RelativeLine(int dX, int dY){
        this.dX=dX;
        this.dY=dY;
    }

    @Override
    public String toString() {
        return dX+ " "+dY;
    }

    @Override
    public String buildInstruction(Mode mode) {
        String command=null;
        switch (mode){
            case GPGL:{
                command="E "+dY+","+dX;

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
        dX*=scale.x;
        dY*=scale.y;
    }

    @Override
    public void offsetBy(Point offset) {

    }
}
