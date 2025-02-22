package com.rui.basic.app.basic.domain.enums;

public enum IntermediaryState {
    OPENING(1, "APERTURA", false),
    DILIGENTED(2, "DILIGENCIADO", false),
    TO_COMPLEMENT(3, "POR COMPLEMENTAR", false),
    COMPLEMENTED(4, "COMPLEMENTADO", false),
    APPROVED(5, "APROBADO", false),
    REVIEWED(6, "REVISADO", false),
    UPDATED(7, "ACTUALIZADO", false),
    RETIRED(8, "RETIRADO", true),
    DESISTED(9, "DESISTIDO", true),
    PENALIZED(10, "PENALIZADO", true),
    RENEWAL(11, "RENOVACION", false);
    

    private final Integer state;
    private final String name;
    private final boolean finalized;

    IntermediaryState(Integer state, String name, boolean finalized) {
        this.state = state;
        this.name = name;
        this.finalized = finalized;
    }

    public Integer getState() {
        return state;
    }

    public String getName() {
        return name;
    }

    public boolean isFinalized() {
        return finalized;
    }

    public static IntermediaryState fromState(Integer state) {
        for (IntermediaryState value : IntermediaryState.values()) {
            if (value.getState().equals(state)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid state: " + state);
    }
}
