![Particle Life](https://raw.githubusercontent.com/quarfzs/particle-life/master/docs/assets/demo.gif)

This is an optimized version of [Jeffrey Ventrella's "Clusters"](http://www.ventrella.com/Clusters/), "a particle microworld with ambiguous entities".
This optimization allows for real-time simulations with **many more particles** and therefore more complex and life-like structures.

This version is (compared to similar projects):
- **faster**: grid-based storage of particles behind the scenes allows for linear complexity *O(n)* instead of quadratic complexity *O(n²)* when updating physics,
- **simpler**: behaviour of particle types (colors) only differs by one number (attraction factor) - the radii etc. are universal,
- **more fascinating**: thousands of particles can form much more life-like structures than only a few hundred.

There are two projects: A Java program, and a Flutter project which includes an Android app and a web version.

# Java Version

The fastest version.

- [Download JAR](https://github.com/quarfzs/particle-life/blob/master/docs/java/particle-life.jar?raw=true) `requires Java to be installed`
- [Source Code](https://github.com/quarfzs/particle-life/tree/master/java)

# Flutter Version

- [Download Android App](https://play.google.com/store) `also fast, and better design`
- [Web Version](web/index.html) `slow`
- [Source Code](https://github.com/quarfzs/particle-life/tree/master/flutter)

# Explanation

Every particle has
- a position
- a velocity
- a color

When a particles comes close enough to another particle, it gets repelled or attracted, based on the current set of rules, stored in a [matrix](matrix). That applied force follows a [simple concept](force).

With these few rules, complex lifelike structures emerge:<br>
[Structure Catalogue](structures)

# Other Projects

Have a look at
- [this video by "CodeParade"](https://www.youtube.com/watch?v=Z_zmZ23grXE) which explains the principle of this simulation.
- [Jeffrey Ventrella's website](http://www.ventrella.com/Clusters/) where you can also download an iOS app.

Related projects:

- [github.com/HackerPoet/Particle-Life](https://github.com/HackerPoet/Particle-Life)
- [github.com/fnky/particle-life](https://github.com/fnky/particle-life)

