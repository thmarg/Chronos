/*  Digit
 *
 *  Copyright (c) 2014 Thierry Margenstern under MIT license
 *  http://opensource.org/licenses/MIT
 */
package tm.android.chronos.core;

import tm.android.chronos.BuildConfig;
import tm.android.chronos.core.Units.DIGIT_FORMAT;
import static tm.android.chronos.core.Units.DIGIT_FORMAT.*;

/**
 * This class handle a living version of a time in the format<br>
 *     <ul>
 *         <li>dd number of days from startSelectedStopwatch</li>
 *         <li>hh hours</li>
 *         <li>mm minutes</li>
 *         <li>ss seconds</li>
 *         <li>milli seconds</li>
 *     </ul>
 */
public class Digit {

    private long days;
    private long hours;
    private long minutes;
    private long seconds;
    private long milliSeconds;
    private static DIGIT_FORMAT initilaDigitFormat;

    private boolean sflag;// flags to now if seconds, minutes, hours or days have changed.
    private boolean mflag;
    private boolean hflag;
    private boolean dflag;

    private boolean sepsmsFlag=false;
    private boolean sepmsFlag=false;
    private boolean sepmhFlag=false;
    private boolean sephdFlag=false;

    private static  boolean noMs = false;

    private long innerMilliseconds;// time in ms from 1J1970 as in System.currentTimeMillis()
    private StringBuilder stringRep; // string representation

    private Digit(long milliSeconds){
        if (initilaDigitFormat ==null)
            initilaDigitFormat = EXTRA_SHORT;
        stringRep = new StringBuilder(initilaDigitFormat.getFormat());
        addMillisSeconds(milliSeconds);

    }


    private void resetDigitFlags(){
        sflag=mflag=hflag=dflag=false;
    }

    private void resetSepFlags(){
        sephdFlag=sepmhFlag=sepmsFlag=sepsmsFlag=false;
    }

    public void subMilliseconds(long ms){
        if (BuildConfig.DEBUG && ms <0){ throw new AssertionError("Invalid Time "+ms+" must be positive");}
        if (BuildConfig.DEBUG && innerMilliseconds <ms){ throw new AssertionError("Invalid Time : innerMilliseconds "+innerMilliseconds+" >= "+ms+" failed !");}

        innerMilliseconds -=ms;
        milliSeconds-=ms;
        if (milliSeconds>=0)
            return;
        //

        subSeconds((milliSeconds / 1000) - (milliSeconds % 1000 == 0 ? 0 : 1));
        milliSeconds = (milliSeconds%1000==0 ? 0 : milliSeconds%1000+1000);
    }


    private void subSeconds(long ss){
        seconds+=ss;// ss < 0!
        if (seconds>=0)
            return;
        //

        subMinutes((seconds / 60) - (seconds%60==0?0:1));
        seconds = (seconds%60==0 ? 0 : seconds%60+60);
    }

    private void subMinutes(long mm){
        minutes+=mm;// mm < 0 !
        if (minutes>=0)
            return;
        //

        subHours((minutes / 60) - (minutes%60==0?0:1));
        minutes = (minutes%60==0 ? 0: minutes%60 +60);
    }

    private void subHours(long hh){
        hours+=hh; // hh < 0 !
        if (hours>=0)
            return;
        //

        subDays((hours / 24) -(hours%24==0?0: 1));
        hours = (hours%24==0 ? 0: hours%24+24);
    }

    private void subDays(long dd){
        days+=dd; // dd < 0 !
    }

    public Digit addMillisSeconds(long ms){
        resetDigitFlags();
       // assert (ms>=0);
        innerMilliseconds +=ms;
        milliSeconds+=ms;
        if (milliSeconds<1000)
            return this;


        addSeconds(milliSeconds / 1000);
        sflag=true;
        milliSeconds=milliSeconds%1000;
        return this;
    }

    private void addSeconds(long ss){
        seconds+=ss;
        if (seconds<60)
            return;


        addMinutes(seconds / 60);
        mflag=true;
        seconds=seconds%60;


    }

    private void addMinutes(long mm){
        minutes+=mm;
        if (minutes<60)
            return;

        addHours(minutes / 60);
        hflag=true;
        minutes=minutes%60;
    }

    private  void addHours(long hh){
        hours +=hh;
        if (hours <24)
            return;

        addDays(hours / 24);
        dflag=true;
        hours = hours %24;
    }
    private void addDays(long dd){
        days +=dd;
    }

    public static void setInitilaDigitFormat(DIGIT_FORMAT initilaDigitFormat) {
        Digit.initilaDigitFormat = initilaDigitFormat;
        noMs = initilaDigitFormat.getKey().startsWith("no_ms");
    }

    public long[] toArray(){
        return new long[]{days, hours,minutes,seconds,milliSeconds};
    }

    public static Digit split(long l){
        Digit d = new Digit(0);
        d.addMillisSeconds(l);
        return d;
    }
    public long getInternal(){
        return innerMilliseconds;
    }

    public void reset(){
        innerMilliseconds=0;
        days=0;
        hours=0;
        minutes=0;
        seconds=0;
        milliSeconds=0;
        stringRep.replace(0, stringRep.length(), initilaDigitFormat.getFormat());
        resetDigitFlags();
        resetSepFlags();
    }




    @Override
    public String toString() {
            stringRep.replace(13,16,(milliSeconds<10?"00"+milliSeconds:(milliSeconds<100?"0"+milliSeconds:""+milliSeconds)));
        if (sflag) {
                if (!sepsmsFlag) {
                    stringRep.replace(12, 13, ":");
                    sepsmsFlag = true;
                }
                stringRep.replace(10, 12, (seconds < 10?"0" + seconds:"" + seconds));
        }

        if (mflag) {
            if (!sepmsFlag){
                stringRep.replace(9,10,":");
                sepmsFlag=true;
            }
            stringRep.replace(7, 9, (minutes < 10 ? "0" + minutes : "" + minutes));
        }

        if (hflag) {
            if (!sepmhFlag){
                stringRep.replace(6,7,":");
                sepmhFlag=true;
            }
            stringRep.replace(4, 6, (hours < 10 ? "0" + hours : "" + hours));
        }

        if (dflag) {
            if (!sephdFlag){
                stringRep.replace(3,4,":");
                sephdFlag=true;
            }
            stringRep.replace(0, 3,(days < 10 ? "00" + days : (days < 100 ? "0" + days : "" + days)));
        }

        return noMs?stringRep.substring(0,12):stringRep.toString();

    }
}
