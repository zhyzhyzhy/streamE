package cc.lovezhy.streamE;

import java.util.Collection;

public class EStreams {
    public static <E> Stream<E> streamE(Collection<E> collection) {
        return StreamSupport.stream(collection.spliterator(), false);
    }

    public static <E> Stream<E> parallelStreamE(Collection<E> collection) {
        return StreamSupport.stream(collection.spliterator(), true);
    }
}
