package kuchinke.com.svgparser;

import android.graphics.Point;
import android.graphics.PointF;



public abstract class Instruction {
    public enum Mode{
        GPGL,
        HPGL
    };
    public abstract String buildInstruction(Mode mode);
    public abstract void scaleBy(PointF scale);
    public abstract void offsetBy(Point offset);
    /*
    String command=null;
    switch (mode){
            case GPGL:{


                break;
            }
            case  HPGL:{

                break;
            }
        }
        return command;
     */
}




