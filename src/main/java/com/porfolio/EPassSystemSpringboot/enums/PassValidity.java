package com.porfolio.EPassSystemSpringboot.enums;

import lombok.Getter;

@Getter
public enum PassValidity {
    ONE_MONTH(1),
    THREE_MONTH(3),
    SIX_MONTH(6);

    private final int months;

    PassValidity(int months) {
        this.months = months;
    }

}
