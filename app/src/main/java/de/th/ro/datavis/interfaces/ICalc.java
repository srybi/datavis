package de.th.ro.datavis.interfaces;

import java.math.BigDecimal;

import de.th.ro.datavis.util.constants.InterpretationMode;

public interface ICalc {

    BigDecimal x_polarToCartesian(BigDecimal xPolar, InterpretationMode mode);

    BigDecimal y_polarToCartesian(BigDecimal yPolar, InterpretationMode mode);

    BigDecimal z_polarToCartesian(BigDecimal zPolar, InterpretationMode mode);


    // Todo Custom Model
    BigDecimal calcIntensity(BigDecimal realPhi, BigDecimal imaginaryPhi, BigDecimal realTheta, BigDecimal imaginaryTheta, InterpretationMode mode);

}
