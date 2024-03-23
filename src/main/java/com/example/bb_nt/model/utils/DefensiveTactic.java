package com.example.bb_nt.model.utils;

public enum DefensiveTactic {
    MAN_TO_MAN("ManToMan"),
    TWO_THREE_ZONE("23Zone"),
    THREE_TWO_ZONE("32Zone"),
    ONE_THREE_ONE_ZONE("131Zone"),
    FULL_COURT_PRESS("Press"),
    INSIDE_BOX("InsideBoxAndOne"),
    OUTSIDE_BOX("OutsideBoxAndOne");
    
    private String bbname;

    DefensiveTactic(String name) {
        this.bbname =name;
    }

    public String bbname() {
        return bbname;
    }

    public static DefensiveTactic fromString(String text) {
        for (DefensiveTactic b : DefensiveTactic.values()) {
            if (b.bbname.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
