package requests;

public final class RequestMatrixValue extends Request {

    public final int i;
    public final int j;
    public final float value;

    public RequestMatrixValue(int i, int j, float value) {
        this.i = i;
        this.j = j;
        this.value = value;
    }
}
