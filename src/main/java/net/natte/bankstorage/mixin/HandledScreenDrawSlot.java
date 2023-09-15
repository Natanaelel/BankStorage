// package net.natte.bankstorage.mixin;

// import org.spongepowered.asm.mixin.Mixin;
// import org.spongepowered.asm.mixin.Shadow;
// import org.spongepowered.asm.mixin.gen.Accessor;
// import org.spongepowered.asm.mixin.injection.Redirect;
// import org.spongepowered.asm.mixin.injection.At;

// import net.minecraft.client.gui.DrawContext;
// import net.minecraft.client.gui.screen.ingame.HandledScreen;
// import net.minecraft.screen.slot.Slot;
// import net.natte.bankstorage.inventory.BankSlot;

// @Mixin(HandledScreen.class)
// public abstract class HandledScreenDrawSlot {

//     // @Shadow
//     // @Accessor
//     // abstract void drawSlot(DrawContext drawContext, Slot slot);

//     @Redirect(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V"))
//     void renderSlotRedirect(DrawContext drawContext, Slot slot) {
//         if (slot instanceof BankSlot bankSlot) {
//             System.out.println("yes");
//             bankSlot.render(drawContext);
//         } else {
//             System.out.println("no");
//             ((HandledScreen)(Object)this).drawSlot(drawContext, slot);
//         }
//     }

// }
