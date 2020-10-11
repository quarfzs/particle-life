package requests;

import logic.Matrix;

public final class RequestMatrix extends Request {

    public final Matrix matrix;

    public RequestMatrix(Matrix matrix) {
        this.matrix = matrix;
    }
}
