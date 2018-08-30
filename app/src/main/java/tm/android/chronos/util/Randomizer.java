package tm.android.chronos.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Iterate randomly over a list : each element is obtained only once.
 * When all items have been obtained, the list is shuffled again and the iteration restart
 * @param <T>
 */
public class Randomizer<T> {
    private List<T> values;
    int index;


    public Randomizer(){
        values = new ArrayList<>(1000);
    }

    public void load(List<T> lst) {
        values.clear();
        values.addAll(lst);
        shuffle();
    }

    private void shuffle() {
        Collections.shuffle(values);
        index = 0;
    }


    public T getNext() {
        if (index >= values.size())
            shuffle();
        return values.get(index++);
    }


}
