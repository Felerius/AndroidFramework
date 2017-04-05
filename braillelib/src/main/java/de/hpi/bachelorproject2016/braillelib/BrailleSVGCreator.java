package de.hpi.bachelorproject2016.braillelib;

import java.util.List;

/**
 * Created by Adrian on 03.02.2017.
 */

public class BrailleSVGCreator {
    static final int DOT_RADIUS=2;
    static final int WORD_SPACE=15;
    static final int DOT_SPACE=6;
    static final int LINE_SPACE=7;
    static final int CHAR_SPACE=10;
    private String svgString="";
    private int startX;
    private int startY;
    List<BrailleFormatter.BrailleChar> chars;


    public BrailleSVGCreator(String inString, int startX, int startY) {
        BrailleFormatter formatter=new BrailleFormatter();
        formatter.setBrailleText(inString);
        this.chars = formatter.getChars();
        this.startX=startX;
        this.startY=startY;
    }

    String getCircleCommand(int x, int y){
        return  "<circle cx=\""+x+"\" cy=\""+y+"\" r=\""+DOT_RADIUS+"\"/>";
    }

    public void buildSVG(){

        svgString+="<svg width=\"200\" height=\"200\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                " <!-- Created with Method Draw - http://github.com/duopixel/Method-Draw/ -->\n" +
                "\n" +
                " <g>";





        int currX=startX;
        int currY=startY;
        for(BrailleFormatter.BrailleChar brailleChar: chars){

            boolean[][]dots=brailleChar.dots;
            boolean isWhiteSpace=true;



            for(int x=0;x<2;x++){
                int localY=currY;
                for(int y=0;y<4;y++){
                    if(dots[x][y]){
                        svgString+=getCircleCommand(currX,localY);


                        isWhiteSpace=false;

                    }
                    localY+=DOT_SPACE;



                }

                if(x==0)currX+=DOT_SPACE;

            }
            if(isWhiteSpace){
                currX+=WORD_SPACE;
            }
            else{
                currX+=CHAR_SPACE;
            }




        }

        svgString+="</g>\n" +
                "</svg>";
    }


    public String getSvgString() {
        return svgString;
    }
}
