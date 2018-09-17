package sumgrid.linkedtotal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sicovin
 */
public class MultiChildLinkedTotal extends LinkedTotal {

    private final List<LinkedTotal> nextTotals = new ArrayList<>();
    private final Set<Integer> skipConditions = new HashSet<>();

    private boolean toSkip = false;

    public MultiChildLinkedTotal(int total, int coord) {
        super(total, coord);
        this.skipConditions.add(coord);
    }

    public MultiChildLinkedTotal(LinkedTotal toCopy) {
        super(toCopy);
        this.skipConditions.add(toCopy.id);
    }

    @Override
    public void addChild(LinkedTotal newChild) {
        nextTotals.add(newChild);
    }

    @Override
    public Set<Integer> compareToPath(List<Integer> path) {
        Set<Integer> returnVal = new HashSet<>(skipConditions);
        returnVal.retainAll(path);
        return returnVal;
    }

    @Override
    public void trimPath(List<Integer> path, Set<Integer> parentSkipConditions, int maxDepth) {
        if (maxDepth <= 0) {
            return;
        }
        
        List<Set<Integer>> childSkipConditions = new ArrayList<>();
        path.add(this.id);

        this.nextTotals.removeIf(LinkedTotal::isToSkip);

        for (Iterator<LinkedTotal> iterator = this.nextTotals.iterator(); iterator.hasNext();) {
            LinkedTotal currentTotal = iterator.next();
            Set<Integer> nodeSkipConditions = new HashSet<>();
            Set<Integer> comparison = currentTotal.compareToPath(path);
            childSkipConditions.add(nodeSkipConditions);
            if (comparison.isEmpty()) {
                currentTotal.trimPath(path, nodeSkipConditions, maxDepth - 1);
            } else {
                nodeSkipConditions.addAll(comparison);
            }
            if (nodeSkipConditions.contains(this.id) || currentTotal.isToSkip()) {
                iterator.remove();
            }
        }

        Set<Integer> newSkipConditions = new HashSet<>();
        if (!childSkipConditions.isEmpty()) {
            newSkipConditions.addAll(childSkipConditions.get(0));
            for (int i = 1; i < childSkipConditions.size(); i++) {
                newSkipConditions.retainAll(childSkipConditions.get(i));
            }
            this.skipConditions.addAll(newSkipConditions);
        }
        this.toSkip = newSkipConditions.contains(this.id) || this.nextTotals.isEmpty();

        parentSkipConditions.addAll(newSkipConditions);
        path.remove(path.size() - 1);
    }

    @Override
    public boolean processPath(List<Integer> path) {
        path.add(this.id);
        boolean success = false;

        for (LinkedTotal currentTotal : this.nextTotals) {
            if (!currentTotal.isToSkip() && !path.contains(currentTotal.id)) {
                success = currentTotal.processPath(path) || success;
            }
        }

        path.remove(path.size() - 1);
        return success;
    }

    @Override
    public boolean isToSkip() {
        return toSkip;
    }
}
