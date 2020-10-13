package gui;

import engine.Renderer;
import guilib.AppState;

import java.util.Arrays;
import java.util.List;

public class MyAppState implements AppState {

    public Renderer renderer;

    public MyAppState(Renderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public MyAppState loadFromString(String src) {
        double[] args = Arrays.stream(src.split(",")).mapToDouble(Float::parseFloat).toArray();
        return new MyAppState(new Renderer((float) args[0], (float) args[1]));
    }

    @Override
    public String storeToString() {
        return String.join(",", List.of(
                Float.toString(renderer.getWidth()),
                Float.toString(renderer.getHeight())
        ));
    }
}
