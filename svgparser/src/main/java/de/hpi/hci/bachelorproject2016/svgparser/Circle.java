package de.hpi.hci.bachelorproject2016.svgparser;

import android.graphics.Point;
import android.graphics.PointF;

public class Circle extends Instruction{
    int radius, cX,cY;
    int startAngle=0;
    int endAngle=3600;
    public Circle(int r, int cX, int cY){
        this.radius=r;
        this.cX=cX;
        this.cY=cY;
    }
    public Circle(int r, int cX, int cY, int startAngle, int endAngle){
        this.radius=r;
        this.cX=cX;
        this.cY=cY;
        this.startAngle=startAngle;
        this.endAngle=endAngle;
    }



    @Override
    public String buildInstruction(Mode mode) {
        String command=null;
        switch (mode){
            case GPGL:{
                command="W "+cY+","+cX+","+radius+","+radius+","+startAngle+","+endAngle;

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
        cX*=scale.x;
        cY*=scale.y;
    }

    @Override
    public void offsetBy(Point offset) {
        cX+=offset.x;
        cY+=offset.y;
    }
}
