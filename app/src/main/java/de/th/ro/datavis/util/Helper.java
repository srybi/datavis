package de.th.ro.datavis.util;



import android.util.Pair;

import java.util.LinkedList;
import java.util.List;

public class Helper {
    public static <A, B> List<Pair<A, B>> zip(List<A> listA, List<B> listB) {
        if (listA.size() != listB.size()) {
            throw new IllegalArgumentException("Lists must have same size");
        }

        List<Pair<A, B>> pairList = new LinkedList<>();

        for (int index = 0; index < listA.size(); index++) {
            pairList.add(Pair.create(listA.get(index), listB.get(index)));
        }
        return pairList;
    }
}
