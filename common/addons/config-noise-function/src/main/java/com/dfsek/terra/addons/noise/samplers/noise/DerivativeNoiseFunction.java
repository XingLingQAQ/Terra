package com.dfsek.terra.addons.noise.samplers.noise;

import com.dfsek.terra.api.noise.DerivativeNoiseSampler;


public abstract class DerivativeNoiseFunction extends NoiseFunction implements DerivativeNoiseSampler {

    public boolean isDifferentiable() {
        return true;
    }

    public double[] noised(long seed, double x, double y) {
        return getNoisedRaw(seed, x * frequency, y * frequency);
    }


    public double[] noised(long seed, double x, double y, double z) {
        return getNoisedRaw(seed, x * frequency, y * frequency, z * frequency);
    }

    public abstract double[] getNoisedRaw(long seed, double x, double y);

    public abstract double[] getNoisedRaw(long seed, double x, double y, double z);
}
