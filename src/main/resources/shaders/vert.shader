#version 450 core

layout (location = 0) in vec4 a_Position;
layout (location = 1) in vec4 a_Color;

uniform mat4 u_MVP;

out vec4 v_Color;
void main(void) {
    gl_Position = u_MVP * a_Position;

    v_Color = a_Color;
}