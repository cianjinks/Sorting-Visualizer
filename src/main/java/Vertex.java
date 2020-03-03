import org.lwjgl.opengl.GL30;

public class Vertex {
    public static final int type = GL30.GL_FLOAT;
    public static final int positionElementCount = 4;
    public static final int colorElementCount = 4;

    private float[] xyzw = new float[positionElementCount];
    private float[] rgba = new float[colorElementCount];

    public static final int elementSize = Float.BYTES;
    public static final int positionSize = elementSize * positionElementCount;
    public static final int colorSize = elementSize * colorElementCount;

    public static final int positionOffset = 0;
    public static final int colorOffset = positionSize;

    public static final int elementCount = positionElementCount + colorElementCount;
    public static final int stride = positionSize + colorSize;

    public void setXYZW(float x, float y, float z, float w) {
        this.xyzw = new float[] {x, y, z, w};
    }

    public void setRGBA(float r, float g, float b, float a) {
        this.rgba = new float[] {r, g, b, a};
    }

    public float[] getElements() {
        float[] out = new float[Vertex.elementCount];
        int i = 0;

        out[i++] = this.xyzw[0];
        out[i++] = this.xyzw[1];
        out[i++] = this.xyzw[2];
        out[i++] = this.xyzw[3];

        out[i++] = this.rgba[0];
        out[i++] = this.rgba[1];
        out[i++] = this.rgba[2];
        out[i]   = this.rgba[3];

        return out;
    }

    public float[] getXYZW() {
        return new float[] {this.xyzw[0], this.xyzw[1], this.xyzw[2], this.xyzw[3]};
    }

    public float[] getRGBA() {
        return new float[] {this.rgba[0], this.rgba[1], this.rgba[2], this.rgba[3]};
    }

    public float getX() {
        return this.xyzw[0];
    }

    public float getY() {
        return this.xyzw[1];
    }

    public float getZ() {
        return this.xyzw[2];
    }

    public float getW() {
        return this.xyzw[3];
    }

    public float getR() {
        return this.rgba[0];
    }

    public float getG() {
        return this.rgba[1];
    }

    public float getB() {
        return this.rgba[2];
    }

    public float getA() {
        return this.rgba[3];
    }
}
