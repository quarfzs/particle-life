package guilib;

public interface App<T extends AppState> {

    void init(GraphicalInterface g, T state);

    T createAppState();
}
