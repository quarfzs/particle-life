# Particle Life

![Particle Life](https://raw.githubusercontent.com/quarfzs/particle-life/master/docs/assets/demo.gif)

## What is this?

This is an optimized version of [Jeffrey Ventrella's "Clusters"](http://www.ventrella.com/Clusters/), "a particle microworld with ambiguous entities".
This optimization allows for real-time simulations with **many more particles** and therefore more complex and life-like structures.

This version is (compared to similar projects):
- **faster**: grid-based storage of particles behind the scenes allows for linear complexity `O(n)` instead of quadratic complexity `O(n²)` when updating physics,
- **simpler**: behaviour of particle types (colors) only differs by one number (attraction factor) - the radii etc. are universal,
- **more fascinating**: thousands of particles can form much more **life-like** structures than only a few hundred.

## Repository Structure

There are two projects: A Java program, and a Flutter project which includes an Android app and a web version.
Their source code is in the respective folders `java/` and `flutter/`.

## Ready to use

Built versions are hosted under [quarfzs.github.io/particle-life](https://quarfzs.github.io/particle-life).

## Documentation & other Projects

Documentation and a general explanation of Particle Life:<br>
[quarfzs.github.io/particle-life](https://quarfzs.github.io/particle-life)

Related projects:

- [github.com/HackerPoet/Particle-Life](https://github.com/HackerPoet/Particle-Life)
- [github.com/fnky/particle-life](https://github.com/fnky/particle-life)

