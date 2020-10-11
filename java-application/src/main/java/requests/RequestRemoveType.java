package requests;

public final class RequestRemoveType extends Request {

    public final int index;
    public final boolean replaceRemovedPoints;
    public final boolean replaceRandom;
    public final int newType;  // only of interest if replaceRandom is false

    public RequestRemoveType(int index) {
        this.index = index;
        this.replaceRemovedPoints = false;
        this.replaceRandom = false;  // doesn't matter
        this.newType = 0;  // doesn't matter
    }

    public RequestRemoveType(int index, boolean replaceRandom) {
        this.index = index;
        this.replaceRemovedPoints = true;
        this.replaceRandom = replaceRandom;
        this.newType = 0;  // doesn't matter
    }

    public RequestRemoveType(int index, int newType) {
        this.index = index;
        this.replaceRemovedPoints = true;
        this.replaceRandom = false;
        this.newType = newType;
    }
}
