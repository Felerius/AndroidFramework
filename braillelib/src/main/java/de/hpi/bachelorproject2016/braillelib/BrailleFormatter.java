package de.hpi.bachelorproject2016.braillelib;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adrian on 03.02.2017.
 */

public class BrailleFormatter {
    private static final String TAG = "BrailleConverter";
    private String brailleText;
    private List<BrailleChar> chars=new ArrayList<>();
    public static class BrailleChar{
        boolean [][] dots=new boolean[2][4];
        char thechar;
        public BrailleChar(char thechar) {
            if(thechar<0x2800)return;
            this.thechar=thechar;

            thechar-=0x2800;
            Log.d(TAG, "BrailleChar: "+Integer.toBinaryString(thechar));
            for(int x=0;x<2;x++){
                for(int y=0;y<3;y++){
                    Log.d(TAG, "BrailleChar: "+Integer.toBinaryString(1<<(x*4+y)));
                    if((thechar&(1<<(x*3+y)))!=0){

                        Log.d(TAG, "BrailleChar: dot at: "+x+" "+y);
                        dots[x][y]=true;
                    }
                }
            }
            if((thechar&1<<6)>0)dots[0][3]=true;
            if((thechar&1<<7)>0)dots[1][3]=true;
            }

        @Override
        public String toString() {
            String res=""+thechar;
            for(int y=0;y<4;y++){
                String line="\n";
                for (int x=0;x<2;x++){
                    line+=dots[x][y]? "o" : "_";
                }
                res+=line+"\n";
            }
            return res;
        }
    }


    public BrailleFormatter() {
    }

      public void setBrailleText(String brailleText){
        this.brailleText=brailleText;
        char [] chars=new char[brailleText.length()];
        brailleText.getChars(0,brailleText.length(),chars,0);
          Log.d(TAG, "setBrailleText: brailletext: "+brailleText);
        for(char c:chars){
            //Log.d(TAG, "setBrailleText: "+c);
           BrailleChar brailleChar=new BrailleChar(c);
            this.chars.add(brailleChar);
           Log.d(TAG, "setBrailleText: "+brailleChar.toString());
        }

    }


    public List<BrailleChar> getChars(){
        return chars;
    }


}
