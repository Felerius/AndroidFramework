package kuchinke.com.airbar_tracking_app.annotations;

/**
 * Created by Julius on 07.03.2017.
 */
 public abstract class Annotation {
        String text="";

        public Annotation(String text){
            this.text = text;
        }

        public abstract boolean checkItemIntersection(int x, int y);

    public String getText(){
        return text;
    }
 }

