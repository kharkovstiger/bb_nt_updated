package com.example.bb_nt.model.utils;

public enum OffensiveTactic {
    BASE_OFFENSE("Base"),
    PUSH_THE_BALL("Push"),
    PATIENT("Patient"),
    LOOK_INSIDE("LookInside"),
    LOW_POST("LowPost"),
    MOTION("Motion"),
    RUN_N_GUN("RunAndGun"),
    PRINCETON("Princeton"),
    INSIDE_ISOLATION("InsideIsolation"),
    OUTSIDE_ISOLATION("OutsideIsolation");

    private String bbname;

    OffensiveTactic(String name) {
        this.bbname =name;
    }

    public String bbname() {
        return bbname;
    }

    public static OffensiveTactic fromString(String text) {
        for (OffensiveTactic b : OffensiveTactic.values()) {
            if (b.bbname.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
