package guilib;

public interface AppState {

    AppState loadFromString(String src);

    String storeToString();
}
