package com.aehmttw.clientmod;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class SuperClientRain extends SuperClientEntityRenderer
{
	static ResourceLocation RAIN_TEXTURES;
    private static final ResourceLocation SNOW_TEXTURES = new ResourceLocation("textures/environment/snow.png");
    static boolean isRaining = false;
	@SuppressWarnings("rawtypes")
	private final HashMap rainXCoords = new HashMap();
    @SuppressWarnings("rawtypes")
	private final HashMap rainYCoords = new HashMap();
	Minecraft mc = Minecraft.getMinecraft();
    private final Random random = new Random();
    //private int rainSoundCounter;
	int rendererUpdateCount;
	int test = 16;
	//static int seedX = (int) (Math.random()*1000000000);
	//static int seedY = (int) (Math.random()*1000000000);
	//Random randomX = new Random(seedX);
	//Random randomY = new Random(seedY);

    @SuppressWarnings("unchecked")
	public SuperClientRain(Minecraft mcIn, IResourceManager resourceManagerIn)
    {
		super(mcIn, resourceManagerIn);

		if (SuperClientConfig.rs.custom_texture)
			RAIN_TEXTURES = new ResourceLocation(SuperClientInfo.id,"textures/environment/rain.png");
		else
			RAIN_TEXTURES = new ResourceLocation("textures/environment/rain.png");

		int test2 = 5;
		if (SuperClientConfig.rs.fancy_rain >= 16 || SuperClientConfig.rs.fast_rain >= 16)
		{
			test = 32;
			test2 = 6;
		}
		if (SuperClientConfig.rs.fancy_rain >= 32 || SuperClientConfig.rs.fast_rain >= 32)
		{
			test = 64;
			test2 = 7;
		}
		if (SuperClientConfig.rs.fancy_rain >= 64 || SuperClientConfig.rs.fast_rain >= 64)
		{
			test = 128;
			test2 = 8;
		}
		if (SuperClientConfig.rs.fancy_rain >= 128 || SuperClientConfig.rs.fast_rain >= 128)
		{
			test = 256;
			test2 = 9;
		}

		for (int i = 0; i < test*2; ++i)
        {
            for (int j = 0; j < test*2; ++j)
            {
                double f = (double)(j - test);//
                double f1 = (double)(i - test);//
                double f2 = MathHelper.sqrt(f * f + f1 * f1);//
                this.rainXCoords.put(i << test2 | j, -f1 / f2);
                this.rainYCoords.put(i << test2 | j, f / f2);
            }
        }
	}
    public void updateRenderer()
    {
    	super.updateRenderer();
    	rendererUpdateCount++;
    }
    @Override
    protected void renderRainSnow(float partialTicks)
    {
        net.minecraftforge.client.IRenderHandler renderer = this.mc.world.provider.getWeatherRenderer();
        if (renderer != null)
        {
            renderer.render(partialTicks, this.mc.world, mc);
            return;
        }

        float f = this.mc.world.getRainStrength(partialTicks);

        if (f > 0.0F)
        {
        	isRaining = true;
            this.enableLightmap();
            Entity entity = this.mc.getRenderViewEntity();
            World world = this.mc.world;
            int i = MathHelper.floor(entity.posX);
            int j = MathHelper.floor(entity.posY);
            int k = MathHelper.floor(entity.posZ);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            GlStateManager.disableCull();
            GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.alphaFunc(516, 0.1F);
            double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
            double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
            double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
            int l = MathHelper.floor(d1);
            int i1 = SuperClientConfig.rs.fast_rain;
            int modifier = test;
            if (this.mc.gameSettings.fancyGraphics)
            {
                i1 = SuperClientConfig.rs.fancy_rain;
            }

            int j1 = -1;
            float f1 = (float)this.rendererUpdateCount + partialTicks;
            bufferbuilder.setTranslation(-d0, -d1, -d2);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int k1 = k - i1; k1 <= k + i1; ++k1)
            {
                for (int l1 = i - i1; l1 <= i + i1; ++l1)
                {
                	Random r1 = new Random(k1*7);
                	Random r2 = new Random(l1*13);
                    int i2 = (k1 - k + modifier) * modifier*2 + l1 - i + modifier;
                    double d3 = (double)this.rainXCoords.get(i2) * 0.5D;
                    double d4 = (double)this.rainYCoords.get(i2) * 0.5D;
                	//double d3 = k-k1;
                	//double d4 = i-l1;

                    blockpos$mutableblockpos.setPos(l1, 0, k1);
                    Biome biome = world.getBiome(blockpos$mutableblockpos);

                    if (biome.canRain() || biome.getEnableSnow())
                    {
                        int j2 = world.getPrecipitationHeight(blockpos$mutableblockpos).getY();
                        int k2 = j - i1;
                        int l2 = j + i1;

                        if (k2 < j2)
                        {
                            k2 = j2;
                        }

                        if (l2 < j2)
                        {
                            l2 = j2;
                        }

                        int i3 = j2;

                        if (j2 < l)
                        {
                            i3 = l;
                        }

                        if (k2 != l2)
                        {
                            this.random.setSeed((long)(l1 * l1 * 3121 + l1 * 45238971 ^ k1 * k1 * 418711 + k1 * 13761));
                            blockpos$mutableblockpos.setPos(l1, k2, k1);
                            float f2 = biome.getFloatTemperature(blockpos$mutableblockpos);

                            if (world.getBiomeProvider().getTemperatureAtHeight(f2, j2) >= 0.15F)
                            {
                                if (j1 != 0)
                                {
                                    if (j1 >= 0)
                                    {
                                        tessellator.draw();
                                    }

                                    j1 = 0;
                                    this.mc.getTextureManager().bindTexture(RAIN_TEXTURES);
                                    bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                                }

                                double d5 = -((double)(this.rendererUpdateCount + l1 * l1 * 3121 + l1 * 45238971 + k1 * k1 * 418711 + k1 * 13761 & 31) + (double)partialTicks) / 32.0D * (3.0D + this.random.nextDouble());
                                double d6 = (double)((float)l1 + 0.5F) - entity.posX;
                                double d7 = (double)((float)k1 + 0.5F) - entity.posZ;
                                float f3 = MathHelper.sqrt(d6 * d6 + d7 * d7) / (float)i1;
                                float f4 = ((1.0F - f3 * f3) * 0.5F + 0.5F) * f;
                                blockpos$mutableblockpos.setPos(l1, i3, k1);
                                int j3 = world.getCombinedLight(blockpos$mutableblockpos, 0);
                                int k3 = j3 >> 16 & 65535;
                                int l3 = j3 & 65535;
                                bufferbuilder.pos((double)l1 - d3 + 0.5D + (r1.nextDouble()-0.5)*SuperClientConfig.rs.rain_offset, (double)l2, (double)k1 - d4 + 0.5D + (r2.nextDouble()-0.5)*SuperClientConfig.rs.rain_offset).tex(0.0D, (double)k2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
                                bufferbuilder.pos((double)l1 + d3 + 0.5D + (r1.nextDouble()-0.5)*SuperClientConfig.rs.rain_offset, (double)l2, (double)k1 + d4 + 0.5D + (r2.nextDouble()-0.5)*SuperClientConfig.rs.rain_offset).tex(1.0D, (double)k2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
                                bufferbuilder.pos((double)l1 + d3 + 0.5D + (r1.nextDouble()-0.5)*SuperClientConfig.rs.rain_offset, (double)k2, (double)k1 + d4 + 0.5D + (r2.nextDouble()-0.5)*SuperClientConfig.rs.rain_offset).tex(1.0D, (double)l2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
                                bufferbuilder.pos((double)l1 - d3 + 0.5D + (r1.nextDouble()-0.5)*SuperClientConfig.rs.rain_offset, (double)k2, (double)k1 - d4 + 0.5D + (r2.nextDouble()-0.5)*SuperClientConfig.rs.rain_offset).tex(0.0D, (double)l2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
                            }
                            else
                            {
                                if (j1 != 1)
                                {
                                    if (j1 >= 0)
                                    {
                                        tessellator.draw();
                                    }

                                    j1 = 1;
                                    this.mc.getTextureManager().bindTexture(SNOW_TEXTURES);
                                    bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                                }

                                double d8 = (double)(-((float)(this.rendererUpdateCount & 511) + partialTicks) / 512.0F);
                                double d9 = this.random.nextDouble() + (double)f1 * 0.01D * (double)((float)this.random.nextGaussian());
                                double d10 = this.random.nextDouble() + (double)(f1 * (float)this.random.nextGaussian()) * 0.001D;
                                double d11 = (double)((float)l1 + 0.5F) - entity.posX;
                                double d12 = (double)((float)k1 + 0.5F) - entity.posZ;
                                float f6 = MathHelper.sqrt(d11 * d11 + d12 * d12) / (float)i1;
                                float f5 = ((1.0F - f6 * f6) * 0.3F + 0.5F) * f;
                                blockpos$mutableblockpos.setPos(l1, i3, k1);
                                int i4 = (world.getCombinedLight(blockpos$mutableblockpos, 0) * 3 + 15728880) / 4;
                                int j4 = i4 >> 16 & 65535;
                                int k4 = i4 & 65535;
                                bufferbuilder.pos((double)l1 - d3 + 0.5D, (double)l2, (double)k1 - d4 + 0.5D).tex(0.0D + d9, (double)k2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                                bufferbuilder.pos((double)l1 + d3 + 0.5D, (double)l2, (double)k1 + d4 + 0.5D).tex(1.0D + d9, (double)k2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                                bufferbuilder.pos((double)l1 + d3 + 0.5D, (double)k2, (double)k1 + d4 + 0.5D).tex(1.0D + d9, (double)l2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                                bufferbuilder.pos((double)l1 - d3 + 0.5D, (double)k2, (double)k1 - d4 + 0.5D).tex(0.0D + d9, (double)l2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                            }
                        }
                    }
                }
            }

            if (j1 >= 0)
            {
                tessellator.draw();
            }

            bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);
            this.disableLightmap();

        }
        else
        {
        	isRaining = false;
        }
    }

}