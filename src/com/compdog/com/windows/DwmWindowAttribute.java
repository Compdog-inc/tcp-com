package com.compdog.com.windows;

public enum DwmWindowAttribute {
    NCRenderingEnabled(1),
    NCRenderingPolicy(2),
    TransitionsForceDisabled(3),
    AllowNCPaint(4),
    CaptionButtonBounds(5),
    NonClientRtlLayout(6),
    ForceIconicRepresentation(7),
    Flip3DPolicy(8),
    ExtendedFrameBounds(9),
    HasIconicBitmap(10),
    DisallowPeek(11),
    ExcludedFromPeek(12),
    Cloak(13),
    Cloaked(14),
    FreezeRepresentation(15),
    PassiveUpdateMode(16),
    UseHostBackdropBrush(17),
    UseImmersiveDarkMode(20),
    WindowCornerPreference(33),
    BorderColor(34),
    CaptionColor(35),
    TextColor(36),
    VisibleFrameBorderThickness(37),
    SystemBackdropType(38),
    Last(39);

    private final int id;
    DwmWindowAttribute(int id) { this.id = id; }
    public int getValue() { return id; }
}