package de.th.ro.datavis.interfaces;

import java.math.BigDecimal;

interface ICalc {


    BigDecimal x_polarToKartesien(BigDecimal xPolar);

    BigDecimal y_polarToKartesien(BigDecimal yPolar);

    BigDecimal z_polarToKartesien(BigDecimal zPolar);


    // Todo Custom Model
    BigDecimal intensity(BigDecimal realPhi, BigDecimal imaginaryPhi,  BigDecimal realTheta, BigDecimal imaginaryTheta);




}
