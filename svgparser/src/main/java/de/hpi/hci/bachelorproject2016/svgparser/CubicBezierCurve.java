package de.hpi.hci.bachelorproject2016.svgparser;

import android.graphics.Point;
import android.graphics.PointF;

public class CubicBezierCurve extends Instruction{

    Point start,c1,c2,end;
    public CubicBezierCurve(Point start, Point c1, Point c2, Point end){
        this.start=start;
        this.c1=c1;
        this.c2=c2;
        this.end=end;


    }

    @Override
    public String buildInstruction(Mode mode) {
        String command=null;
        switch (mode){
            case GPGL:{
                command="BZ 0,"+start.y+","+start.x+","+c1.y+","+c1.x+","+c2.y+","+c2.x+","+end.y+","+end.x;

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
        start.x*=scale.x;
        start.y*=scale.y;
        c1.x*=scale.x;
        c1.y*=scale.y;
        c2.x*=scale.x;
        c2.y*=scale.y;
        end.x*=scale.x;
        end.y*=scale.y;


    }

    @Override
    public void offsetBy(Point offset) {
        start.x+=offset.x;
        start.y+=offset.y;
        c1.x+=offset.x;
        c1.y+=offset.y;
        c2.x+=offset.x;
        c2.y+=offset.y;
        end.x+=offset.x;
        end.y+=offset.y;
    }


}
