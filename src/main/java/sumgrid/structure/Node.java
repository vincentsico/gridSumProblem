package sumgrid.structure;

/**
 *
 * @author sicovin
 * @param <E>
 */
public class Node<E> {
    
    private E content;
    
    public Node() {
        this.content = null;
    }
    
    public Node(E content) {
        this.content = content;
    }
    
    public E get() {
        return content;
    }
    
    public void set(E content) {
        this.content = content;
    }
}
