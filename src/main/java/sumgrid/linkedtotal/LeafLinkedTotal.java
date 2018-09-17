package sumgrid.linkedtotal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import sumgrid.exception.AddingChildException;

/**
 *
 * @author sicovin
 */
public class LeafLinkedTotal extends LinkedTotal {

    public LeafLinkedTotal(int total, int coord) {
        super(total, coord);
    }

    public LeafLinkedTotal(LinkedTotal toCopy) {
        super(toCopy);
    }

    @Override
    public Set<Integer> compareToPath(List<Integer> path) {
        Set<Integer> returnList = new HashSet<>();

        if (path.contains(id)) {
            returnList.add(id);
        }

        return returnList;
    }

    @Override
    public void trimPath(List<Integer> path, Set<Integer> parentSkipConditions, int maxDepth) {
        parentSkipConditions.addAll(this.compareToPath(path));
    }

    @Override
    public boolean processPath(List<Integer> path) {
        StringBuilder builder = new StringBuilder();
        path.stream().forEach(x -> builder.append("(").append(Math.floorDiv(x, 16)).append(",").append(x % 16).append(")"));
        builder.append("(").append(Math.floorDiv(id, 16)).append(",").append(id % 16).append(")");
        System.out.println(builder.toString());

        return true;
    }

    @Override
    public boolean isToSkip() {
        return false;
    }

    @Override
    public void addChild(LinkedTotal newChild) throws AddingChildException {
        throw new AddingChildException("Can't add a child to a leaf total.");
    }
}
