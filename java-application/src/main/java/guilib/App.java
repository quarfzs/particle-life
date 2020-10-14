package guilib;

public interface App<T extends AppState> {

    void init(GraphicalInterface g, T state);

    T createAppState();

    default String getTitle() {
        return "App";
    }

    default String getIconPath() {
        return null;
    }
}
