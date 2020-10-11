package engine;

import logic.Settings;
import logic.Updater;
import logic.UpdaterLogic;

import java.util.ArrayList;

class SimpleUpdater implements Updater {

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

    @Override
    public void updateVelocities(Settings s, UpdaterLogic updaterLogic) {

        // create buffer if necessary
        if (velocitiesBuffer == null || velocitiesBuffer.length != velocities.length) {
            velocitiesBuffer = new float[velocities.length];
        }

        int typeIndex = 0;
        int positionIndex = 0;

        while (positionIndex < positions.length) {

            float[] velocity = updaterLogic.updateVelocity(
                    s, positions, types, typeIndex, velocities[positionIndex], velocities[positionIndex + 1]
            );

            velocitiesBuffer[positionIndex] = velocity[0];
            velocitiesBuffer[positionIndex + 1] = velocity[1];

            typeIndex += 1;
            positionIndex += 2;
        }

        // swap buffer
        float[] h = velocities;
        velocities = velocitiesBuffer;
        velocitiesBuffer = h;
    }

    @Override
    public void updatePositions(Settings s, UpdaterLogic updaterLogic) {

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
        ArrayList<Integer> indices = new ArrayList<>();

        int typeIndex = 0;
        int positionsIndex = 0;

        while (positionsIndex < positions.length) {

            indices.add(typeIndex);

            typeIndex += 1;
            positionsIndex += 2;
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
