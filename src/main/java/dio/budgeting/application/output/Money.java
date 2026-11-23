package dio.budgeting.application.output;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Conversão de centavos (como fica salvo) para reais (como é exibido). */
public final class Money {
    private Money() {
    }

    public static double toReais(long cents) {
        return BigDecimal.valueOf(cents).movePointLeft(2).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
