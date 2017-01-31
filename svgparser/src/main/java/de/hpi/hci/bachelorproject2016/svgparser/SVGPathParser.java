package de.hpi.hci.bachelorproject2016.svgparser;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class SVGPathParser {
    private Point subpathstart=null;
    private static final String TAG = "SVGPathParser" ;
    private String pathData;
    private Point position2=null;
    private float scaleMultiplier;
    private int offsetx=0;
    private int offsety=500;
    private Point offset= new Point(offsetx,offsety);
    private List<Instruction> instructionList= new ArrayList<>();
    SVGPathParser(String pathData){
        this.pathData=pathData;
        this.scaleMultiplier=1;
    }

    void parseCommands(){
        String [] elements=pathData.split("(?=([\\p{Lower}\\p{Upper}]))");
        for(String e: elements){
            if(e.equals(""))continue;
            //Log.d(TAG, "parseCommands: -------------------------NEW COMMAND---------------------");
            //Log.d(TAG, "parseCommands: "+e);
            String []parts= splitCommand(e);
            for(String p: parts){
                //Log.d(TAG, "parseCommands: "+p);
            }
            String command=parts[0];

            switch(command){
                case "M": {
                    parseMove(e);
                    break;
                }
                case "m": {
                    parseMove(e);
                    break;
                }
                case "C": {
                    parseCubicBezier(e);
                    break;
                }
                case "c": {
                    parseCubicBezier(e);
                    break;
                }
                case "Q": {
                    parseQuadraticBezier(e);
                    break;
                }
                case "q": {
                    parseQuadraticBezier(e);
                    break;
                }
                case "L": {
                    parseLine(e);
                    break;
                }
                case "l": {
                    parseLine(e);
                    break;
                }
                case "z":{
                    closePath();
                    break;
                }
                case "Z":{
                    closePath();
                    break;
                }
                default:
                    ////Log.d(TAG, "parseCommands:  not understood");
            }

        }
    }

    void parseQuadraticBezier(String command){
        updateSubpathstart();
        Point pos=new Point(0,0);
        if(position2!=null)pos=position2;
        String []parts=splitCommand(command);
        if(parts.length%4!=1){
            Log.d(TAG, "parseQuadraticBezier: wrong parameter count");
            return;
        }
        if(parts[0].equals("Q")){
            Point start= pos;

            for (int i = 0; i < parts.length-1; i+=4) {
                Point control= parsePoint(parts[i+1],parts[i+2]);
                Point end= parsePoint(parts[i+3],parts[i+4]);

                Point con1=pAdd(start, pMult(pAdd(control,pMult(start,-1)),2.0f/3.0f));
                Point con2=pAdd(end, pMult(pAdd(control,pMult(end,-1)),2.0f/3.0f));
                instructionList.add(new CubicBezierCurve(start,con1,con2,end));
                position2=new Point(end);
                start=new Point(end);
            }




        }
        else
        if(parts[0].equals("q")){
            Point start= pos;

            for (int i = 0; i < parts.length-1; i+=4) {
                Point control= pAdd(pos,parsePoint(parts[i+1],parts[i+2]));
                Point end=pAdd(pos, parsePoint(parts[i+3],parts[i+4]));

                Point con1=pAdd(start, pMult(pAdd(control,pMult(start,-1)),2.0f/3.0f));
                Point con2=pAdd(end, pMult(pAdd(control,pMult(end,-1)),2.0f/3.0f));
                instructionList.add(new CubicBezierCurve(start,con1,con2,end));
                position2=new Point(end);
                start=new Point(end);
            }

        }

    }

    void parseCubicBezier(String command){
        updateSubpathstart();
        Point pos=new Point(0,0);
        if(position2!=null)pos=new Point(position2);
        Point start,control1,control2,end;
        String []parts= splitCommand(command);
        if(parts.length%6!=1){
            Log.d(TAG, "parseCubicBezier: wrong parameter count");
            return;
        }
        if(parts[0].equals("C")){
            start=new Point(pos);
            for(int i=0;i<parts.length-1;i+=6){
                control1=parsePoint(parts[i+1],parts[i+2]);
                control2=parsePoint(parts[i+3],parts[i+4]);
                end=parsePoint(parts[i+5],parts[i+6]);
                instructionList.add(new CubicBezierCurve(start,control1,control2,end));
                start=new Point(end);
                position2=new Point(end);

            }

        }

        if(parts[0].equals("c")){
            start=new Point(pos);
            for(int i=0;i<parts.length-1;i+=6){
                control1=pAdd(start,parsePoint(parts[i+1],parts[i+2]));
                control2=pAdd(start,parsePoint(parts[i+3],parts[i+4]));
                end=pAdd(start,parsePoint(parts[i+5],parts[i+6]));
                instructionList.add(new CubicBezierCurve(start,control1,control2,end));
                start=new Point(end);
                position2=new Point(end);

            }

        }
        //Log.d(TAG, "parseCubicBezier: "+instructionList.get(instructionList.size()-1).buildInstruction(GPGL));


    }

    void parseMove(String command){
        String [] parts= splitCommand(command);
        if(parts[0].equals("M")){
            position2=parsePoint(parts[1],parts[2]);
            instructionList.add(new Move(position2.y,position2.x));
        }
        else if(parts[0].equals("m")){
            Point nextPos;
            if(position2==null){
                position2=parsePoint(parts[1],parts[2]);
                nextPos=position2;

            }
            else{
                nextPos=parsePoint(parts[1],parts[2]);

                position2.x+=nextPos.x;
                position2.y+=nextPos.y;


            }
            instructionList.add(new RelativeMove(nextPos.x,nextPos.y));
        }
        updateSubpathstart();

    }
    void closePath(){
        instructionList.add(new Line(position2.x,position2.y,subpathstart.x,subpathstart.y));
        position2=new Point(subpathstart);
        subpathstart=null;
    }



    void parseLine(String command){
        updateSubpathstart();
        Point start= new Point(position2);
        Point end=new Point(0,0);
        String [] parts= splitCommand( command);
        if(parts.length%2!=1){
            Log.d(TAG, "parseLine: wrong parameter counter");
            return;
        }
        if(parts[0].equals("L")){

            for(int i=0;i<parts.length-1;i+=2){
                end=parsePoint(parts[i+1], parts[i+2]);
                instructionList.add(new Line(start.x,start.y,end.x ,end.y));
                start=new Point(end);
            }


        }
        else{
            for(int i=0;i<parts.length-1;i+=2){
                end=pAdd(start,parsePoint(parts[i+1], parts[i+2]));
                instructionList.add(new Line(start.x,start.y,end.x ,end.y));
                start=new Point(end);
            }


        }
        position2=new Point(end);
        //Log.d(TAG, "parseLine: " +instructionList.get(instructionList.size()-1).buildInstruction(GPGL));


    }
    String [] splitCommand(String command){
        String [] parts=command.split("(,\\s*)|\\s+|(?<=[a-zA-Z])");
        for(String part: parts){
            part.replaceAll(",?\\s+", " ");
        }
        return parts;

    }

    Point parsePoint(String x, String y){

        int fx= (int)Float.valueOf(x).floatValue();
        int fy= (int)Float.valueOf(y).floatValue();
        return pMult(new Point(fx,fy),1);//,(int)scaleMultiplier);
    }
    PointF parsePointF(String x, String y){

        float fx= Float.valueOf(x).floatValue();
        float fy= Float.valueOf(y).floatValue();
        return pMult(new PointF(fx,fy),scaleMultiplier);
    }

    Point pAdd(Point a, Point b){
        Point p= new Point();
        p.x=a.x+b.x;
        p.y=a.y+b.y;
        return p;
    }

    PointF pAdd(PointF a, PointF b){
        PointF p= new PointF();
        p.x=a.x+b.x;
        p.y=a.y+b.y;
        return p;
    }
    Point pMult(Point a, int m){
        Point p= new Point();
        p.x=a.x*m;
        p.y=a.y*m;
        return p;
    }
    Point pMult(Point a, float m){
        Point p= new Point();
        p.x=(int)((float)a.x*m);
        p.y=(int)((float)a.y*m);
        return p;
    }
    PointF pMult(PointF a, float m){
        PointF p= new PointF();
        p.x=a.x*m;
        p.y=a.y*m;
        return p;
    }

    public List<Instruction> getInstructionList() {
        return instructionList;
    }

    public void setInstructionList(List<Instruction> instructionList) {
        this.instructionList = instructionList;
    }

    void updateSubpathstart(){

        if(subpathstart==null)subpathstart=new Point(position2);
    }
}
