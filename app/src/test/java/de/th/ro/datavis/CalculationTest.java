package de.th.ro.datavis;

import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import static de.th.ro.datavis.util.constants.InterpretationMode.Linear;
import static de.th.ro.datavis.util.constants.InterpretationMode.Logarithmic;

import de.th.ro.datavis.interpreter.calc.Calc;
import de.th.ro.datavis.models.FFSLine;

/**
 * Testing the functionality of Calc and FFSInterpreter
 * Some accuracy tests as well
 */
public class CalculationTest {

    private static final String LOG_TAG = "CalcTest";

    //Intensity: 0.0369459474
    private FFSLine testLine1 = new FFSLine(0.000,14.000,2.612473e-02,0.000000e+00,2.612473e-02,0.000000e+00);
    //Intensity: 0.0005237067 X: -0.0003285696 Y: -0.0003779761 Z: 0.0001531170
    private FFSLine testLine2 = new FFSLine(229.000,73.000,3.703166e-04,0.000000e+00,3.703166e-04,0.000000e+00);
    //Intensity: 0.0049164102 Log Intensity: -2.30835188791424216 Polar X: -0.0030845182453 Log X:180°
    private FFSLine testLine3 = new FFSLine(360.000,180.000,3.476427e-03,0.000000e+00,3.476427e-03,0.000000e+00);
    //Intensity: 2
    private FFSLine testLineA1 = new FFSLine(1,1,1,1,1,1);
    //Intensity: 1.4142135623
    private FFSLine testLineA2 = new FFSLine(0,1,0,1,0,1);
    private FFSLine testLineA3 = new FFSLine(0,0,0,0,0,0);

    private FFSLine plusLine = new FFSLine(0,90,1.646985e-01,-6.423237e-02,7.819997e-02,2.736012e-01);


    @Test
    public void GetLineParameters(){
    assertEquals(2.612473e-02,testLine1.getReTheta(),0);
    assertEquals(0.02612473,testLine1.getReTheta(),0);
    assertEquals(6.28318530718,testLine3.getPhiRadians(),0.0001);
    assertEquals(2*Math.PI,testLine3.getPhiRadians(),0.00000000001);
    assertEquals(1.27409,testLine2.getThetaRadians(),0.001);
    }

    @Test
    public void Intensity_Calculation_Correct(){
        assertEquals(0.0369459474, Calc.calcIntensity(testLine1, Linear),0.00001);
        assertEquals(0.0005237067, Calc.calcIntensity(testLine2, Linear),0.00001);
        assertEquals(2, Calc.calcIntensity(testLineA1, Linear),0.00001);
        assertEquals(1.4142135623, Calc.calcIntensity(testLineA2, Linear),0.00001);
        assertEquals(0, Calc.calcIntensity(testLineA3, Linear),0.00001);
    }
    @Test
    public void PlusMinus(){
        System.out.println(Math.pow(-2,2));
        assertEquals(0.334998, Calc.calcIntensity(plusLine,Linear),0.0001);
        assertEquals(-0.2964662097598077, Calc.calcIntensity(plusLine,Logarithmic),0.0001);
        assertEquals(0.33499887344214724,Calc.x_polarToCartesian(plusLine, Linear),0.0001);
        System.out.println("x:" + Calc.x_polarToCartesian(plusLine,Logarithmic));
        assertEquals(0, Calc.y_polarToCartesian(plusLine, Linear),0.0001);
        assertEquals(0, Calc.z_polarToCartesian(plusLine, Linear),0.0001);
    }

    @Test
    public void Coord_Calculation_Correct() {
        assertEquals(-0.0003285696,Calc.x_polarToCartesian(testLine2, Linear),0.00001);
        assertEquals(-0.0003779761,Calc.y_polarToCartesian(testLine2, Linear),0.00001);
        assertEquals( 0.0001531170,Calc.z_polarToCartesian(testLine2, Linear),0.00001);
        assertNotEquals(0.0001531170,Calc.z_polarToCartesian(testLine2, Linear),0.00000000001); //Genauigkeit
        //SIN(180°) not exactly 0; COS(90°) neither
        assertEquals(0,Calc.x_polarToCartesian(testLine3, Linear),0.00000000001);

    }
    //TODO: Tests of List import

    /*
    @Test
    public void TestFFSBeispielData() throws FFSInterpretException {
        IInterpreter ffsInterpreter = new FFSInterpreter();
        List<Sphere> c = new LinkedList<Sphere>();
        File f = new File("file:///C:/Users/Tassilo/Downloads/20220331_Felddaten_Beispiel.ffs");
        c = ffsInterpreter.interpretData(f, 0.2, Linear);
        //Log.d(LOG_TAG, "Interpretation finished");
        System.out.println(c.size());
    }
    */


}