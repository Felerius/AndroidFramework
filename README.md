# LinePod: A Mobile Sensemaking Platform for Blind Users
Oliver Schneider, Kevin Reuss, Nico Boeckhoff, Julius Rudolph, Adrian Kuchinke, David Stangl, Robert Kovacs, Patrick Baudisch

## Abstract
Current displays for the blind struggle to display spatial information. To interactively make sense of complex spatial data, users must often resort to text-focused technology, like Braille displays or screen readers. We previously presented Linespace, a sensemaking platform for the blind - a large interactive tactile display using lines as its primitive, where users can interactively feel glyphs, lines, and textures and hear audio feedback.

We now build on Linespace’s initial proof-of-concept with Linepod, a portable, interactive, 2-D spatial tactile display. Linepod interactively prints high-quality tactile lines to swell paper, senses touch input, and provides interactive audio feedback. Linepod is self-contained, able to run on battery power.

We demonstrate Linepod through several demonstrative sensemaking applications, including a tactile web browser, displaying tactile outlines of websites to facilitate screen-reading; a camera application, including filter-based editing; and minesweeper, to show the potential for game-based problem solving.

## Swell paper
![image](https://user-images.githubusercontent.com/10089188/107517009-7caa9380-6bad-11eb-9abb-00dbc5454795.png)
![image](https://user-images.githubusercontent.com/10089188/107517016-803e1a80-6bad-11eb-8fba-a611e4702e37.png)

Linepod uses swell paper, a type of paper that swells up on contact with heat.

By heating up this special kind of paper with a laser, we can create tactile lines that a blind user can feel.

## Implementation

![image](https://user-images.githubusercontent.com/10089188/107516415-ad3dfd80-6bac-11eb-89c9-6909e2acda04.png)
![image](https://user-images.githubusercontent.com/10089188/107516908-5a187a80-6bad-11eb-9526-31b887e5ab4c.png)

Linepod supports an application ecosystem, complete with an API for soware developers.The system is self-contained, connecting wirelessly to apps on a smartphone via bluetooth. The smartphone is used for its speech input and output capabilities aswell as an easy method of aquiring apps without having to access the rasperry pi directly.
