package cc.lovezhy.streamE;

import java.util.Map;

public class StreamContext {

    private static ThreadLocal<AbstractPipeline> THREAD_LOCAL = new ThreadLocal<>();

    public static <T, V> V of(String tag, T t) {
        AbstractPipeline abstractPipeline = THREAD_LOCAL.get();
        Map prepareDataMap = abstractPipeline.getPrepareDataMap(tag);
        return (V) prepareDataMap.get(t);
    }

    public static void reset(AbstractPipeline abstractPipeline) {
        THREAD_LOCAL.remove();
        THREAD_LOCAL.set(abstractPipeline);
    }
}
