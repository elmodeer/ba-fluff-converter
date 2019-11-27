package fluffUtil;

/**
 * object to hold two objects (e.g. for sending them via rest)
 * @author dierkes
 * @since 13.07.2017
 */
public class Pair<T1, T2> {

    private T1 entry1;
    private T2 entry2;
    
    public Pair() {
    }

    public Pair(T1 entry1, T2 entry2) {
        this.entry1 = entry1;
        this.entry2 = entry2;
    }

    public T1 getFirst() {
        return entry1;
    }

    public void setFirst(T1 entry1) {
        this.entry1 = entry1;
    }

    public T2 getSecond() {
        return entry2;
    }

    public void setSecond(T2 entry2) {
        this.entry2 = entry2;
    }
    
}
