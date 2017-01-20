package kuchinke.com.svgparser;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;

/**
 * Created by Adrian on 29.11.2016.
 */

public class SVGParser {
    private Point dimensions;
    private XmlPullParser parser;
    private String data;
    private InputStream in;
    private List<Instruction> instructions= new ArrayList<>();
    private PointF scale=null;
    private Point translate=null;
    SVGParser(String filedata){
        try {
            parser = XmlPullParserFactory.newInstance().newPullParser();
            in = new StringBufferInputStream(filedata);
            InputStreamReader r= new InputStreamReader(in);
            parser.setInput(r);
        }catch (Exception e){
            Log.d(TAG, "SVGParser: couldnt create parser");
        }
    }

    void startParsing(){
        try{
            while(parser.getEventType()!=XmlPullParser.END_DOCUMENT){
                if(parser.getEventType()==XmlPullParser.START_TAG){
                    //Log.d(TAG, "startParsing: "+parser.getName());

                    switch (parser.getName()){
                        case "svg":{
                            break;
                        }
                        case "g":{
                            readGroup();
                            break;
                        }
                        case "circle":{
                            readCircle();
                            break;
                        }
                        case "path":{
                            readPath();
                            break;
                        }
                        case "line":{
                            readLine();
                            break;
                        }
                        case "rect":{
                            readRect();
                            break;
                        }



                    }
                }
            parser.next();
            }
            /*Log.d(TAG, "startParsing: ---------------------FINISHED PARSING------------------------");
            for(Instruction i: instructions){
                Log.d(TAG, "startParsing: "+i.buildInstruction(GPGL));
            }*/
        }catch (Exception e){
            Log.d(TAG, "startParsing: Error in parsing");
            e.printStackTrace();
        }
        if(scale!=null){
            scaleAll(scale);
        }
        if(translate!=null){
            translateAll(translate);
        }



    }

    private void readRect() {
        int x=0,y=0,w=0,h=0;
        int attrCount=parser.getAttributeCount();
        for(int i=0;i<attrCount;i++){
            String name=parser.getAttributeName(i);
            Log.d(TAG, "readRect: "+parser.getAttributeValue(i));

            switch (name){
                case "x":{
                    x=(int)Float.valueOf(parser.getAttributeValue(i)).floatValue();
                    break;
                }
                case "y":{
                    y=(int)Float.valueOf(parser.getAttributeValue(i)).floatValue();
                    break;
                }
                case "width":{
                    Log.d(TAG, "readRect: "+parser.getAttributeValue(i));
                    if(parser.getAttributeValue(i).contains("%"))return;
                    w=(int)Float.valueOf(parser.getAttributeValue(i)).floatValue();
                    break;

                }
                case "height":{
                    Log.d(TAG, "readRect: "+parser.getAttributeValue(i));
                    if(parser.getAttributeValue(i).contains("%"))return;
                    h=(int)Float.valueOf(parser.getAttributeValue(i)).floatValue();
                    break;

                }
                case "id":{
                    if(parser.getAttributeValue(i).equals("canvas_background"))return;
                    break;
                }

            }


        }
        List<Point> points= new ArrayList<>();
        Point tl= new Point(x,y);
        Point tr= new Point(x+w,y);
        Point bl= new Point(x,y+h);
        Point br = new Point(x+w,y+h);
        points.add(tl);
        points.add(tr);
        points.add(br);
        points.add(bl);
        points.add(new Point(tl));
        instructions.add(new Polydraw(points));

    }

    void readCircle(){
        float cx=0,cy=0,r=0;
        int attrCount=parser.getAttributeCount();
        for(int i=0;i<attrCount;i++){
            String name=parser.getAttributeName(i);
            switch (name){
                case "cx":{
                    cx=Float.valueOf(parser.getAttributeValue(i));
                    break;
                }
                case "cy":{
                    cy=Float.valueOf(parser.getAttributeValue(i));
                    break;
                }
                case "r":{
                    r=Float.valueOf(parser.getAttributeValue(i));
                    break;

                }
            }


        }
        if(r!=0 &&cx!=0 &&cy!=0){
            Circle c= new Circle((int)r,(int)cx,(int)cy);
            instructions.add(c);
        }

    }
    void readLine(){
        float x1=0,x2=0,y1=0,y2=0;
        int attrCount=parser.getAttributeCount();
        for(int i=0;i<attrCount;i++){
            String name=parser.getAttributeName(i);
            switch (name){
                case "x1":{
                    x1=Float.valueOf(parser.getAttributeValue(i));
                    break;
                }
                case "x2":{
                    x2=Float.valueOf(parser.getAttributeValue(i));
                    break;
                }
                case "y1":{
                    y1=Float.valueOf(parser.getAttributeValue(i));
                    break;

                }
                case "y2":{
                    y2=Float.valueOf(parser.getAttributeValue(i));
                    break;

                }
            }


        }
        Line l= new Line((int)x1,(int)y1,(int)x2,(int)y2);
        instructions.add(l);
    }

    void readPath(){
        String data= parser.getAttributeValue("", "d");
        //Log.d(TAG, "readPath: data: "+data);
        SVGPathParser pathParser= new SVGPathParser(data);
        pathParser.setInstructionList(instructions);
        pathParser.parseCommands();
    }

    void readGroup(){
        float scaleX=0,scaleY=0,translateX=0,translateY=0;
        int attrCount=parser.getAttributeCount();
        for(int i=0;i<attrCount;i++){
            String name=parser.getAttributeName(i);
            Log.d(TAG, "readGroup: name: "+name);
            switch (name){
                case "transform":{
                    String transformString=parser.getAttributeValue(i);
                    Pattern translatePattern=Pattern.compile("translate\\((-?[0-9]+\\.?[0-9]*),(-?[0-9]+\\.?[0-9]*)\\)");
                    Matcher translateMatcher=translatePattern.matcher(transformString);
                    while(translateMatcher.find()){
                        Log.d(TAG, "readGroup: translate found");
                        translateX=Float.valueOf(translateMatcher.group(1));
                        translateY=Float.valueOf(translateMatcher.group(2));
                    }
                    Pattern scalePattern=Pattern.compile("scale\\((-?[0-9]+\\.?[0-9]*),(-?[0-9]+\\.?[0-9]*)\\)");
                    Matcher scaleMatcher=scalePattern.matcher(transformString);
                    while(scaleMatcher.find()){
                        scaleX=Float.valueOf(scaleMatcher.group(1));
                        scaleY=Float.valueOf(scaleMatcher.group(2));
                    }
                    break;
                }

            }



        }
        if(scaleX!=0 && scaleY!=0){
            scale=new PointF(scaleX,scaleY);
        }
        if(translateX!=0 || translateY!=0){
            translate=new Point((int)translateX,(int)translateY);
        }
        Log.d(TAG, "readGroup: scaleX: "+scaleX);
        Log.d(TAG, "readGroup: scaleY: "+scaleY);
        Log.d(TAG, "readGroup: translateX: "+translateX);
        Log.d(TAG, "readGroup: translateY: "+translateY);
    }

    void readSvgParameter(){
        int width=0, height=0;
        int attrCount=parser.getAttributeCount();
        for(int i=0;i<attrCount;i++){
            String name=parser.getAttributeName(i);
            switch (name){
                case "width":{
                    width=Integer.valueOf(parser.getAttributeValue(i));
                    break;
                }
                case "height":{
                    height=Integer.valueOf(parser.getAttributeValue(i));
                    break;
                }
                default: break;
            }


        }
        if(width!=0 && height!=0)dimensions=new Point(width, height);

    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public void scaleAll(PointF s){
        for(Instruction i: instructions){
            i.scaleBy(s);
        }
    }

    public void translateAll(Point trans){
        for(Instruction i: instructions){
            i.offsetBy(trans);
        }
    }
}
