public class Quad {

    public static int indicesCount = 0;
    public static int verticesPerQuad = 4;
    public static int indicesPerQuad = 6;

    private Vertex v0, v1, v2, v3;
    private float x, y, width, height;

    public Quad(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        v0 = new Vertex(); v0.setXYZW(x, y, 0f, 1.0f); v0.setRGBA(0.0f, 1.0f, 0.0f, 1.0f);
        v1 = new Vertex(); v1.setXYZW(x, y + height, 0f, 1.0f); v1.setRGBA(0.0f, 1.0f, 0.0f, 1.0f);
        v2 = new Vertex(); v2.setXYZW( x + width,y + height, 0f, 1.0f); v2.setRGBA(0.0f, 1.0f, 0.0f, 1.0f);
        v3 = new Vertex(); v3.setXYZW( x + width, y, 0f, 1.0f); v3.setRGBA(0.0f, 1.0f, 0.0f, 1.0f);
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

    public Vertex[] getVertices() {
        return new Vertex[] {v0, v1, v2, v3};
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }
}
