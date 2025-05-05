package interfaces;

import java.util.List;

public interface IServiceparcelle<T> {

    void add(T t);
    void update(T t);
    void delete(T t);
    List<T> getAll();
}
