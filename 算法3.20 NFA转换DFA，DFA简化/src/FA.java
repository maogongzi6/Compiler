import java.util.HashSet;

public interface FA {
    HashSet<Integer> findTarget(int begin, Integer path);
    HashSet<Integer> findPath(int begin, Integer target);
    boolean addPath(Integer beginId, Integer endId, Integer path);
}
