package interfaces;

import java.util.List;

public interface UserCrud <T> {

    void add(T t);

    void delete(T t);

    void update(T t);

    List<T> getAll();

}
