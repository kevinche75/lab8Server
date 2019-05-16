
import java.io.Serializable;

/**
 *
 * @author kevin
 */
public class Three <K,V,E> implements Serializable{
   static final long serialVersionUID = 20;
    
   private K key;
   private V value;
   private E element;

    public Three(K key, V value, E element) {
        this.key = key;
        this.value = value;
        this.element = element;
    }

    public E getElement() {
        return element;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}
