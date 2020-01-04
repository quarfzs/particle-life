# Force

Click here to play with the force graph:
[![Force Graph](assets/desmos-graph.png)](force-graph)

## Implementation

```dart
double getForce(double distance, double rMin, double rMax, double attraction) {

  if (distance < rMin) {
    return distance / rMin - 1;
  }
  
  if (distance < rMax) {
    return attraction * (1 - (2 * distance - rMin - rMax).abs() / (rMax - rMin));
  }
  
  return 0;
}
```
