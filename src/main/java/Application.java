import Algorithms.BubbleSort;
import Algorithms.QuickSort;
import Algorithms.SelectionSort;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.enums.ImGuiCond;
import imgui.gl3.ImGuiImplGl3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.*;

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
    public static String WINDOW_TITLE = "Sorting Visualizer";

    // Sorting
    private static HashMap<Integer, Vector4f> colors = new HashMap<>();

    private boolean sorting = false;
    private boolean bubble = false;
    private boolean selection = false;
    private boolean quick = false;

    // GUI
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    final ImGuiIO io = ImGui.getIO();
    private final int[] winWidth = new int[1];
    private final int[] winHeight = new int[1];
    private final int[] fbWidth = new int[1];
    private final int[] fbHeight = new int[1];

    private final double[] mousePosX = new double[1];
    private final double[] mousePosY = new double[1];

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
        ImGui.createContext();
        ImGui.styleColorsDark();
        io.setIniFilename(null);
        io.setBackendPlatformName("imgui_java_impl_glfw");
        io.setBackendRendererName("imgui_java_impl_lwjgl");
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
            if(key == GLFW_KEY_Q && action == GLFW_RELEASE) {
                if(!sorting) {
                    sorting = true;
                    quick = true;
                }
            }
        });

        glfwSetMouseButtonCallback(window, (w, button, action, mods) -> {
            final boolean[] mouseDown = new boolean[5];

            mouseDown[0] = button == GLFW_MOUSE_BUTTON_1 && action != GLFW_RELEASE;
            mouseDown[1] = button == GLFW_MOUSE_BUTTON_2 && action != GLFW_RELEASE;
            mouseDown[2] = button == GLFW_MOUSE_BUTTON_3 && action != GLFW_RELEASE;
            mouseDown[3] = button == GLFW_MOUSE_BUTTON_4 && action != GLFW_RELEASE;
            mouseDown[4] = button == GLFW_MOUSE_BUTTON_5 && action != GLFW_RELEASE;

            io.setMouseDown(mouseDown);

            if (!io.getWantCaptureMouse() && mouseDown[1]) {
                ImGui.setWindowFocus(null);
            }
        });

        glfwSetScrollCallback(window, (w, xOffset, yOffset) -> {
            io.setMouseWheelH(io.getMouseWheelH() + (float) xOffset);
            io.setMouseWheel(io.getMouseWheel() + (float) yOffset);
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
        barHeights = new ArrayList<>();
        int numBars = random.nextInt(96) + 5;
        for(int index = 0; index < numBars; index++) {
            barHeights.add(Math.round(random.nextFloat() * 500.0f));
        }

        // Setup Quads
        quads = new ArrayList<>();
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

        // Simulate Bubble Sort
        int[] data = new int[barHeights.size()];
        int increment = 0;
        for(int i : barHeights) {
            data[increment] = i;
            increment++;
        }

        BubbleSort bubbleSort = new BubbleSort(data);
        bubbleSort.sort();
        ArrayList<ArrayList<int[]>> bubbleSimulation = bubbleSort.getSimulation();
        int bubbleFrame = 0;

        // Simulate Selection Sort
        data = new int[barHeights.size()];
        increment = 0;
        for(int i : barHeights) {
            data[increment] = i;
            increment++;
        }

        SelectionSort selectionSort = new SelectionSort(data);
        selectionSort.sort();
        ArrayList<ArrayList<int[]>> selectionSimulation = selectionSort.getSimulation();
        int selectionFrame = 0;

        // Simulate Quick Sort
        data = new int[barHeights.size()];
        increment = 0;
        for(int i : barHeights) {
            data[increment] = i;
            increment++;
        }

        QuickSort quickSort = new QuickSort(data);
        quickSort.sort(0, data.length-1);
        quickSort.complete();
        ArrayList<ArrayList<int[]>> quickSimulation = quickSort.getSimulation();
        int quickFrame = 0;

        // GUI
        imGuiGl3.init();
        double time = 0;

        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            if(sorting) {
                // Bubble Sort
                if(bubble) {
                    if(bubbleFrame < bubbleSimulation.size()) {
                        setupQuads(bubbleSimulation.get(bubbleFrame));
                        bubbleFrame++;
                    } else {
                        bubble = false;
                        sorting = false;
                        bubbleFrame = 0;
                    }
                }
                // Selection Sort
                else if(selection) {
                    if(selectionFrame < selectionSimulation.size()) {
                        setupQuads(selectionSimulation.get(selectionFrame));
                        selectionFrame++;
                    } else {
                        selection = false;
                        sorting = false;
                        selectionFrame = 0;
                    }
                }
                // Quick Sort
                else if(quick) {
                    if(quickFrame < quickSimulation.size()) {
                        setupQuads(quickSimulation.get(quickFrame));
                        // printFrame(quickSimulation.get(quickFrame), quickFrame);
                        quickFrame++;
                    } else {
                        quick = false;
                        sorting = false;
                        quickFrame = 0;
                    }
                }
            }

            // VBO (Vertex Buffer Object)
            Vertex[] vertices;
            for (Quad quad : quads) {
                quad.updatePosition();
                vertices = quad.getVertices();
                for (Vertex vertex : vertices) {
                    vboBuffer.put(vertex.getXYZW());
                    vboBuffer.put(vertex.getRGBA());
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

            // GUI
            final double currentTime = glfwGetTime();
            final double deltaTime = (time > 0) ? (currentTime - time) : 1f / 60f;
            time = currentTime;
            glfwGetWindowSize(window, winWidth, winHeight);
            glfwGetFramebufferSize(window, fbWidth, fbHeight);
            glfwGetCursorPos(window, mousePosX, mousePosY);
            io.setDisplaySize(winWidth[0], winHeight[0]);
            io.setDisplayFramebufferScale((float) fbWidth[0] / winWidth[0], (float) fbHeight[0] / winHeight[0]);
            io.setMousePos((float) mousePosX[0], (float) mousePosY[0]);
            io.setDeltaTime((float) deltaTime);
            ImGui.newFrame();

            ImGui.setNextWindowSize(275, 135, ImGuiCond.Once);
            ImGui.setNextWindowPos(25, 25, ImGuiCond.Once);
            ImGui.begin("Controls");
            if(ImGui.button("Bubble Sort", 125f, 30f)) {
                if(!sorting) {
                    sorting = true;
                    bubble = true;
                }
            }
            ImGui.sameLine(0f, -1f);
            if(ImGui.button("Selection Sort", 125f, 30f)) {
                if(!sorting) {
                    sorting = true;
                    selection = true;
                }
            }
            /**if(ImGui.button("Reset", 125f, 30f)) {
                if(!sorting) {
                    resetBars();
                }
            }
            ImGui.sameLine(0f, -1f);**/
            if(ImGui.button("Quick Sort", 125f, 30f)) {
                if(!sorting) {
                    sorting = true;
                    quick = true;;
                }
            }
            /**if(ImGui.button("Randomise Speed", 125f, 30f)) {
                if(!sorting) {
                    barHeights = new ArrayList<>();
                    numBars = random.nextInt(96) + 5;
                    for(int index = 0; index < numBars; index++) {
                        barHeights.add(Math.round(random.nextFloat() * 500.0f));
                    }
                    resetBars();
            }**/

            ImGui.end();

            ImGui.render();
            imGuiGl3.render(ImGui.getDrawData());

            // Clear the VBO
            vboBuffer.clear();

            shaderHandler.unBindProgram();

            glfwSwapBuffers(window); // swap the color buffers
            glfwPollEvents();
        }
        imGuiGl3.dispose();
        ImGui.destroyContext();
    }

    public void setupQuads() {
        quads = new ArrayList<>();
        for(int index = 0; index < barHeights.size(); index++) {
            float domain = 800.0f;
            float cutoff = 240.0f;
            float width = (domain / barHeights.size()) - 0.2f * (domain / barHeights.size());
            float height = barHeights.get(index);
            float x = cutoff + ((domain / barHeights.size()) * index);
            float y = 0.0f;
            Quad quad = new Quad(x, y, width, height, colors.get(0));
            quads.add(quad);
        }
    }

    public void setupQuads(ArrayList<int[]> frame) {
        quads = new ArrayList<>();
        for(int index = 0; index < frame.size(); index++) {
            float domain = 800.0f;
            float cutoff = 240.0f;
            float width = (domain / frame.size()) - 0.2f * (domain / frame.size());
            float height = frame.get(index)[0];
            float x = cutoff + ((domain / frame.size()) * index);
            float y = 0.0f;
            Quad quad = new Quad(x, y, width, height, colors.get(frame.get(index)[1]));
            quads.add(quad);
        }
    }

    public void resetBars() {
        Collections.shuffle(barHeights);
        setupQuads();
    }

    private void printFrame(ArrayList<int[]> frame, int num) {
        System.out.print("Frame " + num + ": ");
        for(int[] i : frame) {
            System.out.print(i[0] + " ");
        }
        System.out.print("\n");
    }

    private void printData(int[] data) {
        System.out.print("Data: ");
        for(int i : data) {
            System.out.print(i + " ");
        }
        System.out.print("\n");
    }


    public static void main(String[] args) {
        colors.put(0, new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
        colors.put(1, new Vector4f(1.0f, 1.0f, 0.0f, 1.0f));
        colors.put(2, new Vector4f(0.5f, 0.0f, 0.5f, 1.0f));
        colors.put(3, new Vector4f(1.0f, 0.0f, 0.0f, 1.0f));
        new Application().run();
    }

}