/*
 * PrimitiveKeper
 *
 * Copyright (c) 2014 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * unfortunately due to autoboxing and immutability of primitive type it's impossible to passe an int as parameter and modify it in a procedure.
 * This is the reason of this class or wait .... what ? java 9 or 10 to have everything as object even 1 or + like 1.+.50.*50, 10.do(lambda)..........
 * So below a generic and mutable wrapper around primitive types.
 */
public class Pwrapper<T extends Number> {

    private T value;


    public Pwrapper(T value) {
        this.value = value;
        if (value instanceof BigInteger || value instanceof BigDecimal || value instanceof AtomicInteger)
            throw new UnsupportedOperationException("BigInteger, BigDecimal and AtomicInteger are not primitive so they are not supported as parameter");
    }


    public T value() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }


    public Pwrapper<T> add(T value) {
        double d = this.value.doubleValue() + value.doubleValue();
        setValue(createFromDouble(d));
        return this;
    }

    public Pwrapper<T> sub(T value) {
        double d = this.value.doubleValue() - value.doubleValue();
        setValue(createFromDouble(d));
        return this;
    }

    @SuppressWarnings("unchecked")
    private T createFromDouble(double d) {
        if (this.value instanceof Integer) {
            int n = (int) d;
            return ((T) (Number) n);
        }
        if (this.value instanceof Double) return ((T) (Number) d);

        if (this.value instanceof Long) {
            long n = (long) d;
            return ((T) (Number) n);
        }
        if (this.value instanceof Short) {
            short n = (short) d;
            return ((T) (Number) n);
        }
        if (this.value instanceof Byte) {
            byte n = (byte) d;
            return ((T) (Number) n);
        }
        if (this.value instanceof Float) {
            float n = (float) d;
            return ((T) (Number) n);
        }
        return null;
    }

    // first approach, but TODO more options such as 2 dec no remove trailing 0, but 3 if third non zero; rounding or not, tech tab analyse automat style.
    public String format(int ndecimal, boolean removeZerroDec) {
        String s = value.toString();

        int sep = s.indexOf(".");
        if (sep == -1) sep = s.indexOf(",");
        if (sep == -1) return value.toString();

        String dec = s.substring(sep + 1);
        if (ndecimal < dec.length())
            dec = dec.substring(0, ndecimal);
        if (removeZerroDec) {
            int last = dec.length();
            while (last > 0) {
                if (!(dec.substring(last - 1, last)).equals("0"))
                    break;
                last--;
            }
            if (last == 0)
                dec = "";
            else
                dec = dec.substring(0, last);


        } else {
            if (dec.length() > ndecimal)
                dec = dec.substring(0, ndecimal);
            else if (dec.length() < ndecimal) {
                int n = ndecimal - dec.length();
                for (int k = 0; k < n; k++)
                    dec = dec + "0";
            }
        }
        if (dec.equals(""))
            return s.substring(0, sep);
        return s.substring(0, sep + 1) + dec;

    }

    @Override
    public String toString() {
        return value.toString();
    }
}
