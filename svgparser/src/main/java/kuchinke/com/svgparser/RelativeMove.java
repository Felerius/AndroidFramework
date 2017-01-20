package kuchinke.com.svgparser;

import android.graphics.Point;
import android.graphics.PointF;



public class RelativeMove extends Instruction{
    int dX, dY;
    public RelativeMove(int dX, int dY){
        this.dX=dX;
        this.dY=dY;
    }
    @Override
    public String buildInstruction(Mode mode) {
        String command=null;
        switch (mode){
            case GPGL:{
                command="O "+dY+","+dX+",";

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




