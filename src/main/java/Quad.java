import org.joml.Vector4f;

public class Quad {

    public static int indicesCount = 0;
    public static int verticesPerQuad = 4;
    public static int indicesPerQuad = 6;

    private Vertex v0, v1, v2, v3;
    private float x, y, width, height;
    private float r, g, b, a;

    public Quad(float x, float y, float width, float height, float r, float g, float b, float a) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        v0 = new Vertex(); v0.setXYZW(x, y, 0f, 1.0f); v0.setRGBA(r, g, b, a);
        v1 = new Vertex(); v1.setXYZW(x, y + height, 0f, 1.0f); v1.setRGBA(r, g, b, a);
        v2 = new Vertex(); v2.setXYZW( x + width,y + height, 0f, 1.0f); v2.setRGBA(r, g, b, a);
        v3 = new Vertex(); v3.setXYZW( x + width, y, 0f, 1.0f); v3.setRGBA(r, g, b, a);
        indicesCount += indicesPerQuad;
    }

    public Quad(float x, float y, float width, float height, Vector4f color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.r = color.x;
        this.g = color.y;
        this.b = color.z;
        this.a = color.w;
        v0 = new Vertex(); v0.setXYZW(x, y, 0f, 1.0f); v0.setRGBA(r, g, b, a);
        v1 = new Vertex(); v1.setXYZW(x, y + height, 0f, 1.0f); v1.setRGBA(r, g, b, a);
        v2 = new Vertex(); v2.setXYZW( x + width,y + height, 0f, 1.0f); v2.setRGBA(r, g, b, a);
        v3 = new Vertex(); v3.setXYZW( x + width, y, 0f, 1.0f); v3.setRGBA(r, g, b, a);
        indicesCount += indicesPerQuad;
    }

    public void updatePosition() {
        v0.setXYZW(x, y, 0f, 1.0f);
        v1.setXYZW(x, y + height, 0f, 1.0f);
        v2.setXYZW( x + width,y + height, 0f, 1.0f);
        v3.setXYZW( x + width, y, 0f, 1.0f);
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        v0.setXYZW(x, y, 0f, 1.0f);
        v1.setXYZW(x, y + height, 0f, 1.0f);
        v2.setXYZW( x + width,y + height, 0f, 1.0f);
        v3.setXYZW( x + width, y, 0f, 1.0f);
    }

    public void setColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        v0.setRGBA(r, g, b, a);
        v1.setRGBA(r, g, b, a);
        v2.setRGBA(r, g, b, a);
        v3.setRGBA(r, g, b, a);
    }

    public void setColor(Vector4f color) {
        this.r = color.x;
        this.g = color.y;
        this.b = color.z;
        this.a = color.w;
        v0.setRGBA(r, g, b, a);
        v1.setRGBA(r, g, b, a);
        v2.setRGBA(r, g, b, a);
        v3.setRGBA(r, g, b, a);
    }

    public Vertex[] getVertices() {
        return new Vertex[] {v0, v1, v2, v3};
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public float getA() {
        return a;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
