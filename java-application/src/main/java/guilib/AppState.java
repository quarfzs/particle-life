package guilib;

public interface AppState {

    void loadFromString(String src);

    String storeToString();
}
