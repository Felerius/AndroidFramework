package kuchinke.com.airbar_tracking_app;

import android.media.ToneGenerator;

import java.util.HashMap;

/**
 * Created by Julius on 08.03.2017.
 */

public class Tones {
    public static int getTone(String note){
        int tone = -1;
        switch (note){
            case "c":
                tone = ToneGenerator.TONE_DTMF_C;
                break;
            case "d":
                tone = ToneGenerator.TONE_DTMF_D;
                break;
            case "e":
                tone = ToneGenerator.TONE_DTMF_0;
                break;
            case "f":
                tone = ToneGenerator.TONE_DTMF_1;
                break;
            case "g":
                tone = ToneGenerator.TONE_DTMF_2;
                break;
            case "cis":
                tone = ToneGenerator.TONE_DTMF_3;
                break;
            case "dis":
                tone = ToneGenerator.TONE_DTMF_4;
                break;

        }
        return tone;
    }
}
