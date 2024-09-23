package com.dfsek.terra.addons.noise.samplers.noise.simplex;

import com.dfsek.terra.addons.noise.samplers.noise.DerivativeNoiseFunction;
import com.dfsek.terra.addons.noise.samplers.noise.PseudoErosionSampler;
import com.dfsek.terra.api.noise.DerivativeNoiseSampler;

import static com.dfsek.terra.addons.noise.samplers.noise.PseudoErosionSampler.dot;
import static com.dfsek.terra.addons.noise.samplers.noise.PseudoErosionSampler.hash;
import static com.dfsek.terra.addons.noise.samplers.noise.PseudoErosionSampler.hashX;
import static com.dfsek.terra.addons.noise.samplers.noise.PseudoErosionSampler.hashY;


/**
 * Temporary sampler that provides derivatives to test pseudoerosion, should be replaced with
 * derivative versions of existing samplers
 */
public class DerivativeFractal extends DerivativeNoiseFunction {

    private final int heightOctaves;
    private final double heightGain;
    private final double heightLacunarity;
    private final double frequency;

    public DerivativeFractal(int octaves, double gain, double lacunarity, double frequency) {
        this.heightOctaves = octaves;
        this.heightGain = gain;
        this.heightLacunarity = lacunarity;
        this.frequency = frequency;
    }

    private static float[] baseNoise(float px, float py) {
        float ix = (float) Math.floor(px);
        float iy = (float) Math.floor(py);
        float fx = px - ix;
        float fy = py - iy;

        float ux = fx * fx * fx * (fx * (fx * 6.0f - 15.0f) + 10.0f);
        float uy = fy * fy * fy * (fy * (fy * 6.0f - 15.0f) + 10.0f);
        float dux = fx * fx * 30.0f * (fx * (fx - 2.0f) + 1.0f);
        float duy = fy * fy * 30.0f * (fy * (fy - 2.0f) + 1.0f);

        float gan = PseudoErosionSampler.hash(ix, iy);
        float gax = PseudoErosionSampler.hashX(gan);
        float gay = PseudoErosionSampler.hashY(gan);

        float gbn = PseudoErosionSampler.hash(ix + 1, iy);
        float gbx = PseudoErosionSampler.hashX(gbn);
        float gby = PseudoErosionSampler.hashY(gbn);

        float gcn = PseudoErosionSampler.hash(ix, iy + 1);
        float gcx = PseudoErosionSampler.hashX(gcn);
        float gcy = PseudoErosionSampler.hashY(gcn);

        float gdn = PseudoErosionSampler.hash(ix + 1, iy + 1);
        float gdx = PseudoErosionSampler.hashX(gdn);
        float gdy = PseudoErosionSampler.hashY(gdn);

        float va = dot(gax, gay, fx, fy);
        float vb = dot(gbx, gby, fx - 1, fy);
        float vc = dot(gcx, gcy, fx, fy - 1);
        float vd = dot(gdx, gdy, fx - 1, fy - 1);

        float u2x =
            gax + (gbx - gax) * ux + (gcx - gax) * uy + (gax - gbx - gcx + gdx) * ux * uy + dux * (uy * (va - vb - vc + vd) + vb - va);
        float u2y =
            gay + (gby - gay) * ux + (gcy - gay) * uy + (gay - gby - gcy + gdy) * ux * uy + duy * (ux * (va - vb - vc + vd) + vc - va);

        return new float[]{ va + ux * (vb - va) + uy * (vc - va) + ux * uy * (va - vb - vc + vd), u2x, u2y };
    }

    @Override
    public boolean isDifferentiable() {
        return true;
    }

    @Override
    public double[] noised(long seed, double x, double y) {
        x *= frequency;
        y *= frequency;
        double[] out = { 0.0f, 0.0f, 0.0f };
        float heightFreq = 1.0f;
        float heightAmp = 1f;
        float cumAmp = 0.0f;
        for(int i = 0; i < heightOctaves; i++) {
            float[] noise = baseNoise((float) (x * heightFreq), (float) (y * heightFreq));
            out[0] += noise[0] * heightAmp;
            out[1] += noise[1] * heightAmp * heightFreq;
            out[2] += noise[2] * heightAmp * heightFreq;
            cumAmp += heightAmp;
            heightAmp *= heightGain;
            heightFreq *= heightLacunarity;
        }
        out[0] /= cumAmp;
        out[1] /= cumAmp;
        out[2] /= cumAmp;
        return out;
    }

    @Override
    public double[] getNoisedRaw(long seed, double x, double y) {
        return noised(seed, x, y);
    }

    @Override
    public double[] getNoisedRaw(long seed, double x, double y, double z) {
        return noised(seed, x, y, z);
    }

    @Override
    public double getNoiseRaw(long seed, double x, double y) {
        return noised(seed, x, y)[0];
    }

    @Override
    public double getNoiseRaw(long seed, double x, double y, double z) {
        return noised(seed, x, y, z)[0];
    }
}
