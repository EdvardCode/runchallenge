package com.edvardcode.runchallenge.tracker;

public class RunTracker {
    private double startX;
    private double startZ;
    private double lastX;
    private double lastZ;
    private double totalDistance = 0;
    private double requiredDistance = 10000;
    private boolean challengeActive = false;
    private boolean completed = false;

    public void startChallenge(double requiredDistance) {
        this.requiredDistance = requiredDistance;
        this.challengeActive = true;
        this.completed = false;
        this.startX = 0;
        this.startZ = 0;
        this.lastX = 0;
        this.lastZ = 0;
        this.totalDistance = 0;
    }

    public void updatePosition(double currentX, double currentZ) {
        if (!challengeActive || completed) return;

        if (lastX == 0 && lastZ == 0) {
            lastX = currentX;
            lastZ = currentZ;
            return;
        }

        double deltaX = currentX - lastX;
        double deltaZ = currentZ - lastZ;
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (startX != 0 || startZ != 0) {
            double toStartDist = Math.sqrt(
                    Math.pow(currentX - startX, 2) +
                            Math.pow(currentZ - startZ, 2)
            );
            double prevToStartDist = Math.sqrt(
                    Math.pow(lastX - startX, 2) +
                            Math.pow(lastZ - startZ, 2)
            );

            if (toStartDist < prevToStartDist) {
                totalDistance = Math.max(0, totalDistance - distance);
            } else {
                totalDistance += distance;
            }
        } else {
            totalDistance += distance;
        }

        lastX = currentX;
        lastZ = currentZ;

        if (totalDistance >= requiredDistance) {
            completed = true;
            challengeActive = false;
        }
    }

    public double getProgress() {
        return Math.min(totalDistance / requiredDistance, 1.0);
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isActive() {
        return challengeActive;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public double getRequiredDistance() {
        return requiredDistance;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartZ() {
        return startZ;
    }

    public void setStartPoint(double x, double z) {
        this.startX = x;
        this.startZ = z;
    }

    public void stopChallenge() {
        this.challengeActive = false;
    }
}