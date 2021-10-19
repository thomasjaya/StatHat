package apitools.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class renderUtil {

    /*
    OpenGL is black magic
    */

    public static void renderLivingLabel(RenderWorldLastEvent evt, Entity entityIn, String str, double x, double y, double z, Color colour, Boolean shadow)
    {
        FontRenderer fontrenderer = Minecraft.getMinecraft().fontRendererObj;
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        Entity thePlayer = Minecraft.getMinecraft().thePlayer;


        double entityX = (entityIn.prevPosX - thePlayer.prevPosX) + ((entityIn.posX - thePlayer.posX) - (entityIn.prevPosX - thePlayer.prevPosX))*evt.partialTicks;
        double entityY = (entityIn.prevPosY - thePlayer.prevPosY) + ((entityIn.posY - thePlayer.posY) - (entityIn.prevPosY - thePlayer.prevPosY))*evt.partialTicks;
        double entityZ = (entityIn.prevPosZ - thePlayer.prevPosZ) + ((entityIn.posZ - thePlayer.posZ) - (entityIn.prevPosZ - thePlayer.prevPosZ))*evt.partialTicks;


        double f = 1.6F;
        double f1 = 0.016666668F * f;
        GlStateManager.pushMatrix();


        GlStateManager.disableCull();
        GlStateManager.enableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorMaterial();


        GlStateManager.translate(
                entityX + x,
                entityY + y + (double)entityIn.height,
                entityZ + z
        );


        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-f1, -f1, f1);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        int i = 0;


        int j = fontrenderer.getStringWidth(str) / 2;
        GlStateManager.disableTexture2D();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos((double)(-j - 1), (double)(-1 + i), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldrenderer.pos((double)(-j - 1), (double)(8 + i), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldrenderer.pos((double)(j + 1), (double)(8 + i), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldrenderer.pos((double)(j + 1), (double)(-1 + i), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();

        fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, 553648127);

        GlStateManager.enableDepth();

        if(shadow){
            GlStateManager.depthMask(false);
            fontrenderer.drawStringWithShadow(str, -fontrenderer.getStringWidth(str) / 2, i, colour.getRGB());
        } else{
            GlStateManager.depthMask(true);
            fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, colour.getRGB());
        }
        GlStateManager.depthMask(true);


        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableLighting();
        GlStateManager.enableAlpha();

        GlStateManager.popMatrix();

        RenderHelper.disableStandardItemLighting();
    }



}
