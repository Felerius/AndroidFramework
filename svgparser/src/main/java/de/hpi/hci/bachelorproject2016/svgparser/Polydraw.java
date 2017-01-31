package de.hpi.hci.bachelorproject2016.svgparser;

import android.graphics.Point;
import android.graphics.PointF;


import java.util.ArrayList;
import java.util.List;

public class Polydraw extends Instruction{
    private List<Point> points=new ArrayList<>();
    public Polydraw(List<Point> points){
        for(Point p: points){
            this.points.add(new Point(p));
        }
    }

    @Override
    public String buildInstruction(Mode mode) {
        String command=null;
        switch (mode){
            case GPGL:{
                command="M "+points.get(0).y+","+points.get(0).x+(char)3+"D ";
                for(int i=1;i<points.size();i++){
                    Point p= points.get(i);

                    if(i==points.size()-1){
                        command+=p.y+","+p.x;
                    }
                    else{
                        command+=p.y+","+p.x+",";
                    }
                }

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
        for(Point p: points){
            p.x*=scale.x;
            p.y*=scale.y;
        }
    }

    @Override
    public void offsetBy(Point offset) {
        for(Point p: points){
            p.x+=offset.x;
            p.y+=offset.y;
        }
    }
}
