package tm.android.chronos.localisation;

import tm.android.chronos.core.Units;
import tm.android.chronos.util.Couple;

import java.util.*;

/**
 * Speed parameters by category
 */
public class SpeedCategory  {
    public final static int WALK=0;
    public final static int RUNNING=1;
    public final static int BICYCLE =2;
    public final static int CAR=3;
    private final static int baseFactor = 40; //
    private final static List<Integer> cats = new ArrayList<>(4);
    private final  static Hashtable<Integer,Float> speedParser = new Hashtable<>(4); //speeds in meter by second
    private final  static Hashtable<Integer,Float> minSpeed = new Hashtable<>(4); // min speed toke into acount for calculate a mean speed
    private final  static List<Couple<Integer,String>> speedCategories;
    static {
        Collections.addAll(cats,WALK,RUNNING, BICYCLE,CAR);
        speedParser.put(WALK,1.5f);
        speedParser.put(RUNNING,3.0f);
        speedParser.put(BICYCLE,7.0f);
        speedParser.put(CAR,30.0f);
//
        minSpeed.put(WALK,0.8f);
        minSpeed.put(RUNNING,1.2f);
        minSpeed.put(BICYCLE,2.0f);
        minSpeed.put(CAR,3.0f);
        List<Couple<Integer,String>> lst = Arrays.asList(new Couple<>(WALK,Units.getLocalizedText("WALK")),
                new Couple<>(RUNNING,Units.getLocalizedText("RUNNING")),
                new Couple<>(BICYCLE,Units.getLocalizedText("BICYCLE")),
                new Couple<>(CAR,Units.getLocalizedText("CAR")));
        speedCategories = Collections.unmodifiableList(lst);
    }
    public static float getCloseDistance(int category){
        if (cats.contains(category))
            return speedParser.get(category)*baseFactor;
        else return -1.0f;
    }

    /**
     * Return the base speed for a category
     */
   public static float getBaseSpeed(int category) {
        if (cats.contains(category))
            return speedParser.get(category);
        else return -1;
   }


   public static List<Couple<Integer,String>> getSpeedCategories(){
        return speedCategories;
   }

    /**
     * Return the minimum speed for the category to consoder it valide for calculate a mean speed
     * @param category the speedCategory
     * @return float, min speed for the category
     */
   public static float getMinSpeed(int category){
        if (cats.contains(category))
            return minSpeed.get(category);
        else return 0.5f;
   }



}
