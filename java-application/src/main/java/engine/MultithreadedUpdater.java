package engine;

import logic.Settings;
import logic.Updater;
import logic.UpdaterLogic;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

class MultithreadedUpdater implements Updater {

    private ExecutorService executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

    private int[] types;
    private float[] positions;
    private float[] velocities;

    @Override
    public void setTypes(int[] types) {
        this.types = types;
    }

    @Override
    public void setPositions(float[] positions) {
        this.positions = positions;
    }

    @Override
    public void setVelocities(float[] velocities) {
        this.velocities = velocities;
    }

    @Override
    public int[] getTypes() {
        return types;
    }

    @Override
    public float[] getPositions() {
        return positions;
    }

    @Override
    public float[] getVelocities() {
        return velocities;
    }

    private float[] positionsBuffer;
    private float[] velocitiesBuffer;

    private int nx;
    private int ny;
    private float containerSizeX;
    private float containerSizeY;
    private Container[] containers;

    private static class Container {

        ArrayList<Integer> indices;

        Container(int initialCapacity) {
            this.indices = new ArrayList<>(initialCapacity);
        }
    }

    private void createContainers() {

        containers = new Container[nx * ny];
        int initialCapacity = 2 * types.length / containers.length;

        for (int i = 0; i < containers.length; i++) {
            containers[i] = new Container(initialCapacity);
        }
    }

    private void clearContainers() {
        for (Container container : containers) {
            container.indices.clear();
        }
    }

    private void fillContainers() {
        int typeIndex = 0;
        int positionIndex = 0;

        while (positionIndex < positions.length) {

            float x = positions[positionIndex];
            float y = positions[positionIndex + 1];

            int containerX = (int) Math.floor(x / containerSizeX);
            int containerY = (int) Math.floor(y / containerSizeY);
            
            containerX = clip(containerX, 0, nx - 1);
            containerY = clip(containerY, 0, ny - 1);

            containers[getContainerIndex(containerX, containerY)].indices.add(typeIndex);

            typeIndex += 1;
            positionIndex += 2;
        }
    }
    
    private int getContainerIndex(int containerX, int containerY) {
        return nx * containerY + containerX;
    }

    private ArrayList<Container> getNeighborContainers(int containerX, int containerY,
                                                       int radiusX, int radiusY,
                                                       boolean includeCenter, boolean wrap) {

        int centerContainerIndex = getContainerIndex(containerX, containerY);

        ArrayList<Container> relevantContainers = new ArrayList<>(includeCenter ? 9 : 8);

        int minContainerX = containerX - radiusX;
        int minContainerY = containerY - radiusY;
        int maxContainerX = containerX + radiusX;
        int maxContainerY = containerY + radiusY;

        if (wrap) {

            for (int cx = minContainerX; cx <= maxContainerX; cx++) {
                for (int cy = minContainerY; cy <= maxContainerY; cy++) {
                    int containerIndex = getContainerIndex(modulo(cx, nx), modulo(cy, ny));
                    if (includeCenter || containerIndex != centerContainerIndex) {
                        relevantContainers.add(containers[containerIndex]);
                    }
                }
            }

        } else {

            minContainerX = clip(minContainerX, 0, nx - 1);
            maxContainerX = clip(maxContainerX, 0, nx - 1);
            minContainerY = clip(minContainerY, 0, ny - 1);
            maxContainerY = clip(maxContainerY, 0, ny - 1);

            for (int cx = minContainerX; cx <= maxContainerX; cx++) {
                for (int cy = minContainerY; cy <= maxContainerY; cy++) {
                    int containerIndex = getContainerIndex(cx, cy);
                    if (includeCenter || containerIndex != centerContainerIndex) {
                        relevantContainers.add(containers[containerIndex]);
                    }
                }
            }
        }

        return relevantContainers;
    }

    private int modulo(int a, int b) {
        return ((a % b) + b) % b;
    }
    
    private int clip(int val, int min, int max) {
        if (val < min) {
            return min;
        }
        if (val > max) {
            return max;
        }
        return val;
    }

    @Override
    public void updateVelocities(Settings settings, UpdaterLogic updaterLogic) {

        final Settings s = settings.clone();  // UI thread could change settings

        // create containers if necessary
        nx = (int) Math.floor(s.getRangeX() / s.getRMax());
        ny = (int) Math.floor(s.getRangeY() / s.getRMax());
        containerSizeX = s.getRangeX() / nx;
        containerSizeY = s.getRangeY() / ny;
        if (containers == null || nx * ny != containers.length) {
            createContainers();
        } else {
            clearContainers();
        }

        fillContainers();

        // create buffer if necessary
        if (velocitiesBuffer == null || velocitiesBuffer.length != velocities.length) {
            velocitiesBuffer = new float[velocities.length];
        }


        CountDownLatch countDownLatch = new CountDownLatch(containers.length);

        int containerX = 0;
        int containerY = 0;
        for (Container container : containers) {

            final int finalContainerX = containerX;
            final int finalContainerY = containerY;

            executor.submit(() -> {

                ArrayList<Container> relevantContainers = getNeighborContainers(
                        finalContainerX, finalContainerY, 1, 1,
                        false, s.isWrap()
                );
                ArrayList<Integer> relevantIndices = new ArrayList<>(container.indices);  // regarded particles come first
                relevantContainers.forEach(c -> relevantIndices.addAll(c.indices));

                int[] relevantTypes = new int[relevantIndices.size()];
                float[] relevantPositions = new float[relevantIndices.size() * 2];

                int typeIndex = 0;
                int positionIndex = 0;
                for (int index : relevantIndices) {
                    int index2 = 2 * index;

                    relevantTypes[typeIndex] = types[index];

                    relevantPositions[positionIndex] = positions[index2];
                    relevantPositions[positionIndex + 1] = positions[index2 + 1];

                    typeIndex += 1;
                    positionIndex += 2;
                }


                int relevantIndex = 0;
                for (int index : container.indices) {
                    int index2 = index * 2;

                    float[] velocity = updaterLogic.updateVelocity(
                            s, relevantPositions, relevantTypes,
                            relevantIndex, velocities[index2], velocities[index2 + 1]
                    );

                    velocitiesBuffer[index2] = velocity[0];
                    velocitiesBuffer[index2 + 1] = velocity[1];

                    relevantIndex++;
                }

                countDownLatch.countDown();
            });


            // step to next container
            containerX++;
            if (containerX == nx) {
                containerX = 0;
                containerY++;
            }
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // swap buffer
            float[] h = velocities;
            velocities = velocitiesBuffer;
            velocitiesBuffer = h;
        }
    }

    @Override
    public void updatePositions(Settings settings, UpdaterLogic updaterLogic) {

        final Settings s = settings.clone();  // UI thread could change settings

        // create buffer if necessary
        if (positionsBuffer == null || positionsBuffer.length != positions.length) {
            positionsBuffer = new float[positions.length];
        }

        int positionIndex = 0;

        while (positionIndex < positions.length) {

            float[] position = updaterLogic.updatePosition(
                    s,
                    positions[positionIndex], positions[positionIndex + 1],
                    velocities[positionIndex], velocities[positionIndex + 1]
            );

            positionsBuffer[positionIndex] = position[0];
            positionsBuffer[positionIndex + 1] = position[1];

            positionIndex += 2;
        }

        // swap buffer
        float[] h = positions;
        positions = positionsBuffer;
        positionsBuffer = h;
    }

    @Override
    public int[] getRelevant(float x, float y, float radius, boolean wrap) {

        int containerX = (int) Math.floor(x / containerSizeX);
        int containerY = (int) Math.floor(y / containerSizeY);
        int radiusX = (int) Math.ceil(radius / containerSizeX);
        int radiusY = (int) Math.ceil(radius / containerSizeY);
        getNeighborContainers(containerX, containerY, radiusX, radiusY, true, wrap);

        ArrayList<Integer> indices = new ArrayList<>();

        int typeIndex = 0;
        int positionIndex = 0;

        while (positionIndex < positions.length) {

            indices.add(typeIndex);

            typeIndex += 1;
            positionIndex += 2;
        }

        // make array
        int n = indices.size();
        int[] arr = new int[n];
        int i = 0;
        for (int integer : indices) {
            arr[i] = integer;
            i++;
        }
        return arr;
    }
}
