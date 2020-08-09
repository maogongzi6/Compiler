
import java.util.*;

public class DoubleWayMap<K,V>{
    //@Override
    public int size() {
        return map.size();
    }

    //@Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    //@Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    /// @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    // @Override
    public V get(Object key) {
        return map.get(key);
    }

    // @Override
    public V put(K key, V value) {
        if (map.containsKey(key)||traverseMap.containsKey(value))
            return null;
        traverseMap.put(value,key);
        return map.put(key,value);
    }

    // @Override
    public V remove(K key) {
        traverseMap.remove(map.get(key));
        return map.remove(key);
    }

    public K traverseRemove(V value) {
        map.remove(traverseMap.get(value));
        return traverseMap.remove(value);
    }

    //  @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new RuntimeException();
    }

    //  @Override
    public void clear() {
        map.clear();
        traverseMap.clear();
    }

    //  @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    // @Override
    public Collection<V> values() {
        return map.values();
    }

    // @Override
    /*public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }*/

    HashMap<V,K> traverse() {
        return traverseMap;
    }
    HashMap<K,V> map() {return map;}

    HashMap<K,V> map = new HashMap<>();
    HashMap<V,K> traverseMap = new HashMap<>();
}
