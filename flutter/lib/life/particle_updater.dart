import 'dart:math';

import 'matrix.dart';
import 'particle.dart';

class ParticleUpdater {
  var _random = Random();
  
  num rMin;
  num rMax;
  num forceFactor;
  num heat;
  num boxWidth;
  num boxHeight;
  num currentDeltaT;
  num interactionRadiusSquared;
  num oneMinusFrictionDt;

  void setValues(num rMin, num rMax, num forceFactor, num friction, num heat, num boxWidth, num boxHeight, num dt) {
    this.rMin = rMin;
    this.rMax = rMax;
    this.interactionRadiusSquared = rMax * rMax;
    this.forceFactor = forceFactor;
    this.heat = heat;
    this.boxWidth = boxWidth;
    this.boxHeight = boxHeight;
    this.currentDeltaT = dt;
    this.oneMinusFrictionDt = max(0, 1 - friction * currentDeltaT);
  }

  void updateWithRelevant(Particle particle, List<Particle> relevant, Matrix matrix) {

    for (Particle particle2 in relevant) {
      if (particle != particle2) {

        num x2 = particle2.x;
        num y2 = particle2.y;
        
        if (x2 > particle.x) {
          num wrappedX2 = x2 - boxWidth;
          if (particle.x - wrappedX2 < x2 - particle.x) {
            x2 = wrappedX2;
          }
        } else {
          num wrappedX2 = x2 + boxWidth;
          if (wrappedX2 - particle.x < particle.x - x2) {
            x2 = wrappedX2;
          }
        }
        if (y2 > particle.y) {
          num wrappedY2 = y2 - boxHeight;
          if (particle.y - wrappedY2 < y2 - particle.y) {
            y2 = wrappedY2;
          }
        } else {
          num wrappedY2 = y2 + boxHeight;
          if (wrappedY2 - particle.y < particle.y - y2) {
            y2 = wrappedY2;
          }
        }

        // accelerate particle towards particle2
        num dx = x2 - particle.x;
        num dy = y2 - particle.y;
        num d2 = dx * dx + dy * dy;

        if (d2 > 0 && d2 < interactionRadiusSquared) {

          num d = sqrt(d2);

          num a = forceFactor * getForce(d, rMin, rMax, matrix.get(particle.type, particle2.type));
          num factor = a / d * currentDeltaT;
          particle.vx += dx * factor;
          particle.vy += dy * factor;
        }
      }
    }

    // friction force = -v * friction
    particle.vx *= oneMinusFrictionDt;
    particle.vy *= oneMinusFrictionDt;

    // add a little energy
    if (heat > 0) {
      particle.vx += (2 * _random.nextDouble() - 1) * heat;
      particle.vy += (2 * _random.nextDouble() - 1) * heat;
    }
  }

  num getForce(num distance, num rMin, num rMax, num attraction) {
    
    if (distance < rMin) {
      return distance / rMin - 1;
    }

    if (distance < rMax) {
      return attraction * (1 - (2 * distance - rMin - rMax).abs() / (rMax - rMin));
    }

    return 0;
  }
}