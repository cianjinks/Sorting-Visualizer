import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Application {

    // The window handle
    private long window;
    public static int WINDOW_WIDTH = 1280;
    public static int WINDOW_HEIGHT = 720;
    public ArrayList<Quad> quads;
    public ArrayList<Integer> barHeights;
    public ArrayList<Integer> completed;
    public static String WINDOW_TITLE = "Batch Renderer";

    // Sorting
    private static Vector4f BASE_COLOR = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f);
    private static Vector4f SELECTION_COLOR = new Vector4f(1.0f, 1.0f, 0.0f, 1.0f);
    private static Vector4f COMPLETE_COLOR = new Vector4f(0.5f, 0.0f, 0.5f, 1.0f);
    private boolean sorting = false;
    private boolean bubble = false;
    private boolean selection = false;
    private int sorti = 0;
    private int sortj = 0;

    public void run() {

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_TITLE, NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ) {
                glfwSetWindowShouldClose(window, true);
            }
            if(key == GLFW_KEY_A && action == GLFW_RELEASE) {
                // TODO: Broken
                if(!sorting) {
                    resetBars();
                }
            }
            if(key == GLFW_KEY_B && action == GLFW_RELEASE) {
                if(!sorting) {
                    sorting = true;
                    bubble = true;
                }
            }
            if(key == GLFW_KEY_I && action == GLFW_RELEASE) {
                if(!sorting) {
                    sorting = true;
                    selection = true;
                }
            }
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void loop() {
        GL.createCapabilities();

        // Enables openGL debug messages
        GLUtil.setupDebugMessageCallback();

        // Set the clear color
        glClearColor(1.0f, 1.0f, 1.0f, 0.0f);

        Random random = new Random();
        // Generate between 5 - 100 bars of random height
        barHeights = new ArrayList<Integer>();
        int numBars = random.nextInt(96) + 5;
        for(int index = 0; index < numBars; index++) {
            barHeights.add(Math.round(random.nextFloat() * 500.0f));
        }

        // Setup Quads
        quads = new ArrayList<Quad>();
        setupQuads();

        // IBO (Index Buffer Object)
        // 768 for 16kb of vertex memory
        int[] indices = new int[quads.size() * Quad.indicesPerQuad];
        int offset = 0;
        for(int i = 0; i < indices.length; i += 6) {
            indices[i + 0] = 0 + offset;
            indices[i + 1] = 1 + offset;
            indices[i + 2] = 2 + offset;

            indices[i + 3] = 2 + offset;
            indices[i + 4] = 3 + offset;
            indices[i + 5] = 0 + offset;

            offset += 4;
        }
        IntBuffer iboBuffer = BufferUtils.createIntBuffer(indices.length);
        iboBuffer.put(indices);
        iboBuffer.flip();

        // VAO (Vertex Array Object)
        int vaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoID);

        // Bind Buffer Data
        int vboID = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);
        // Call is now dynamic and so we allocate memory (16kB) or 512 vertices (8 floats per vertex) (768 indices)
        FloatBuffer vboBuffer = MemoryUtil.memAllocFloat(2 * Quad.verticesPerQuad * 512);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vboBuffer.capacity() * Float.BYTES, GL30.GL_DYNAMIC_DRAW);
        GL30.glVertexAttribPointer(0, Vertex.positionElementCount, Vertex.type, false, Vertex.stride, Vertex.positionOffset);
        GL30.glVertexAttribPointer(1, Vertex.colorElementCount, Vertex.type, false, Vertex.stride, Vertex.colorOffset);

        GL30.glBindVertexArray(0);

        int iboID = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, iboID);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, iboBuffer, GL30.GL_STATIC_DRAW);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, 0);

        Shader shaderHandler = new Shader();
        shaderHandler.addShader("/shaders/vert.shader", GL30.GL_VERTEX_SHADER);
        shaderHandler.addShader("/shaders/frag.shader", GL30.GL_FRAGMENT_SHADER);

        Matrix4f mvp = new Matrix4f().ortho(0.0f, 1280.0f, 0.0f, 720.0f, -1.0f, 1.0f);

        completed = new ArrayList<Integer>();
        int minelement = 0;
        boolean selectionfor = false;

        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            // Sorting Algorithms
            if(sorting) {
                // Bubble Sort
                if(bubble) {
                    if(sorti < barHeights.size() - 1) {
                        if(sortj < barHeights.size() - sorti - 1) {
                            if(barHeights.get(sortj) > barHeights.get(sortj + 1)) {
                                Collections.swap(barHeights, sortj, sortj+1);
                            }
                            sortj++;
                        } else {
                            sortj = 0;
                            sorti++;
                        }
                        setupQuads();
                        // Color the selected bars
                        for(int index = 0; index < barHeights.size(); index++) {
                            if(index == sortj || index == sortj+1) {
                                quads.get(index).setColor(SELECTION_COLOR.x, SELECTION_COLOR.y, SELECTION_COLOR.z, SELECTION_COLOR.w);
                            }
                        }
                    } else {
                        sorting = false;
                        bubble = false;
                        quads.get(0).setColor(COMPLETE_COLOR.x, COMPLETE_COLOR.y, COMPLETE_COLOR.z, COMPLETE_COLOR.w);
                        System.out.println("Bubble Sort Complete");
                    }
                    // Color the completed bars
                    for(int index = quads.size() - 1; index > quads.size() - sorti - 1; index--) {
                        quads.get(index).setColor(COMPLETE_COLOR.x, COMPLETE_COLOR.y, COMPLETE_COLOR.z, COMPLETE_COLOR.w);
                    }
                }
                // Selection Sort
                else if(selection) {
                    if(sorti < barHeights.size() - 1 && !selectionfor) {
                        minelement = sorti;
                        sortj = sorti + 1;
                        selectionfor = true;
                    } else if(selectionfor) {
                        if(sortj < barHeights.size()) {
                            if(barHeights.get(sortj) < barHeights.get(minelement)) {
                                minelement = sortj;
                            }
                            sortj++;
                        } else {
                            selectionfor = false;
                            Collections.swap(barHeights, minelement, sorti);
                            setupQuads();
                            // Color the selected bars
                            for(int index = 0; index < barHeights.size(); index++) {
                                if(index == minelement || index == sorti) {
                                    // TODO
                                    quads.get(index).setColor(SELECTION_COLOR.x, SELECTION_COLOR.y, SELECTION_COLOR.z, SELECTION_COLOR.w);
                                }
                            }
                            sorti++;
                        }
                    } else {
                        sorting = false;
                        selection = false;
                        quads.get(quads.size() - 1).setColor(COMPLETE_COLOR.x, COMPLETE_COLOR.y, COMPLETE_COLOR.z, COMPLETE_COLOR.w);
                        quads.get(quads.size() - 2).setColor(COMPLETE_COLOR.x, COMPLETE_COLOR.y, COMPLETE_COLOR.z, COMPLETE_COLOR.w);
                        System.out.println("Selection Sort Complete");
                    }
                    // System.out.println("I: " + sorti + " J: " + sortj + " BarSize: " + barHeights.size() + " MinElem: " + minelement);
                    // Color the completed bars
                    for(int index = 0; index < sorti - 1; index++) {
                        quads.get(index).setColor(COMPLETE_COLOR.x, COMPLETE_COLOR.y, COMPLETE_COLOR.z, COMPLETE_COLOR.w);
                    }
                }
            }

            // VBO (Vertex Buffer Object)
            Vertex[] vertices = null;
            for(int quad = 0; quad < quads.size(); quad++) {
                quads.get(quad).updatePosition();
                vertices = quads.get(quad).getVertices();
                for(int vertex = 0; vertex < vertices.length; vertex++) {
                    vboBuffer.put(vertices[vertex].getXYZW());
                    vboBuffer.put(vertices[vertex].getRGBA());
                }
            }
            vboBuffer.flip();

            shaderHandler.bindProgram();
            shaderHandler.setUniMat4f("u_MVP", mvp);

            // Bind VBO and dynamically fill it with data
            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);
            GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, vboBuffer);

            // Bind VAO
            GL30.glBindVertexArray(vaoID);
            GL30.glEnableVertexAttribArray(0);
            GL30.glEnableVertexAttribArray(1);
            GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, iboID);

            // Draw the vertices
            GL30.glDrawElements(GL30.GL_TRIANGLES, Quad.indicesCount, GL_UNSIGNED_INT, 0);

            // Unbind VAO
            GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, 0);
            GL30.glDisableVertexAttribArray(0);
            GL30.glDisableVertexAttribArray(1);
            GL30.glBindVertexArray(0);

            // Clear the VBO
            vboBuffer.clear();

            shaderHandler.unBindProgram();

            glfwSwapBuffers(window); // swap the color buffers
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Application().run();
    }

    public void setupQuads() {
        quads.clear();
        for(int index = 0; index < barHeights.size(); index++) {
            float domain = 800.0f;
            float cutoff = 240.0f;
            float width = (domain / barHeights.size()) - 0.2f * (domain / barHeights.size());
            float height = barHeights.get(index);
            float x = cutoff + ((domain / barHeights.size()) * index);
            float y = 0.0f;
            Quad quad = new Quad(x, y, width, height, BASE_COLOR.x, BASE_COLOR.y, BASE_COLOR.z, BASE_COLOR.w);
            quads.add(quad);
        }
    }

    public void resetBars() {
        sorti = 0;
        sortj = 0;
        Collections.shuffle(barHeights);
        setupQuads();
    }

}