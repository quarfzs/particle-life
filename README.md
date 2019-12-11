# Particle Life

This is an optimized version of [Jeffrey Ventrella's "Clusters"](http://www.ventrella.com/Clusters/), "a particle microworld with ambiguous entities".
This optimization allows for real-time simulations with **many more particles** and therefore more complex and life-like structures.

This version is (compared to similar projects):
- **simpler**: behaviour of particle types (colors) only differs by one number (attraction factor) - the radii etc. are universal,
- **faster**: grid-based storage of particles behind the scenes allows for linear comlexity *O(n)* instead of quadratic complexity *O(n²)* when updating physics,
- **and therefore more fascinating**: thousands of particles can form much more life-like structures than only a few hundred.

# Usage

## Setup
- check out / download this project
- open it in your IDE
- if not done automatically: install dependencies with gradle
- build project and run `Main` class

## Controls
- Press "o" to open the settings menu.
- Press "f" to zoom in (and out) on a certain group of particles while hovering over it with the cursor.
- Press "r" to create a new world with new rules (or use the "new world" button in the settings menu).
- Press "s" to randomly redistribute the particles without changing the rules (or use the "stir up" button in the settings menu).
- Drag with the cursor to move particles.
- Press "esc" to exit.

# Explanation

Have a look at
- [this video by "CodeParade"](https://www.youtube.com/watch?v=Z_zmZ23grXE) which explains the principle of this simulation.
- [Jeffrey Ventrella's website](http://www.ventrella.com/Clusters/) where you can also download an iOS app.

# Related Projects

- https://github.com/HackerPoet/Particle-Life
- https://github.com/fnky/particle-life

# Matrix Representation

Since the behaviour of particle types here only differs by one number - the attraction factor -, a specific set of rules for *n* particle types can be represented by exactly one *n*-by-*n* matrix. This matrix can be displayed by opening the settings menu and selecting "draw matrix".

Here, the **row** specifies the **reacting** particle type. The **column** specifies the **other** particle type which the particle type is reacting to. The number in that cell is the **attraction factor** for that relation.

> Example: To find out how red particles react to violet particles, look in the **first row** (red color) and the **last column** (violet color). If there is a positive number, red particles will move toward violet particles when nearby. If there is a negative number, red particles will be repelled by violet particles when nearby.
> This doesn't say anything about the behaviour of violet particles though.

Naturally, the main diagonal specifies how much a particle type is attracted to its own kind.

# Probable Future Improvements

- better user interface
- parallelization
