# Force

<iframe src="https://www.desmos.com/calculator/3gkaf5lywv?embed" width="500px" height="500px" style="border: 1px solid #ccc" frameborder=0></iframe>

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
