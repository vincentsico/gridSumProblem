package sumgrid.linkedtotal;

import java.util.List;
import java.util.Set;
import sumgrid.exception.AddingChildException;

/**
 *
 * @author sicovin
 */
public abstract class LinkedTotal {

    protected final int total;
    protected final int id;

    public LinkedTotal(int total, int id) {
        this.total = total;
        this.id = id;
    }

    public LinkedTotal(LinkedTotal toCopy) {
        this.total = toCopy.total;
        this.id = toCopy.id;
    }

    public abstract Set<Integer> compareToPath(List<Integer> path);

    public abstract void trimPath(List<Integer> path, Set<Integer> parentSkipConditions, int maxDepth);

    public abstract boolean processPath(List<Integer> path);

    public abstract boolean isToSkip();

    public abstract void addChild(LinkedTotal newChild) throws AddingChildException;
}
