package de.th.ro.datavis.interfaces;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import de.th.ro.datavis.interpreter.ffs.FFSIntensityColor;
import de.th.ro.datavis.models.Sphere;
import de.th.ro.datavis.util.enums.InterpretationMode;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public interface IInterpreter {
    /**
     * Gives back a Vextor3 List from a File, based on the given inputstream or file.
     * @return a List of Vector3's
     */
    List<Sphere> interpretData(InputStream stream, double scalingFactor, InterpretationMode mode) throws FFSInterpretException;
    List<Sphere> interpretData(File file, double scalingFactor, InterpretationMode mode) throws FFSInterpretException;
    List<Sphere> interpretDataAsStream(Stream<String> stream, double scalingFactor, int samples, InterpretationMode mode) throws FFSInterpretException;
    FFSIntensityColor mapToColor(double intensity);

}
