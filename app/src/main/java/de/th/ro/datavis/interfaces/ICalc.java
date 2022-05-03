package de.th.ro.datavis.interfaces;

import java.math.BigDecimal;

public interface ICalc {

    BigDecimal x_polarToCartesian(BigDecimal xPolar);

    BigDecimal y_polarToCartesian(BigDecimal yPolar);

    BigDecimal z_polarToCartesian(BigDecimal zPolar);


    // Todo Custom Model
    BigDecimal calcIntensity(BigDecimal realPhi, BigDecimal imaginaryPhi, BigDecimal realTheta, BigDecimal imaginaryTheta);

}
