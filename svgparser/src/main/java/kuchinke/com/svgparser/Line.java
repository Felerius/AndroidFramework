package kuchinke.com.svgparser;

import android.graphics.Point;
import android.graphics.PointF;



public class Line extends Instruction {

    int startX,startY, endX, endY;
    public Line(int startX, int startY, int endX, int endY){
        this.startX=startX;
        this.startY=startY;
        this.endX=endX;
        this.endY=endY;
    }

    @Override
    public String toString() {
        return startX+ " "+startY + " "+ endX + " " + endY;
    }

    @Override
    public String buildInstruction(Mode mode) {
        String command=null;
        switch (mode){
            case GPGL:{
                command="M "+startY+","+startX+(char)3+"D "+endY+","+endX;

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
        startX*=scale.x;
        startY*=scale.y;
        endX*=scale.x;
        endY*=scale.y;
    }

    @Override
    public void offsetBy(Point offset) {
        startX+=offset.x;
        startY+=offset.y;
        endX+=offset.x;
        endY+=offset.y;
    }


}




