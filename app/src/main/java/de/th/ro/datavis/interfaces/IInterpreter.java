package de.th.ro.datavis.interfaces;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.stream.Stream;

import de.th.ro.datavis.interpreter.ffs.FFSIntensityColor;
import de.th.ro.datavis.models.AtomicField;
import de.th.ro.datavis.models.Result;
import de.th.ro.datavis.util.enums.InterpretationMode;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public interface IInterpreter {
    /**
     * Gives back a Vextor3 List from a File, based on the given inputstream or file.
     * @return a List of Vector3's
     */
    Result<ArrayList<AtomicField>> interpretData(InputStream stream, double scalingFactor, InterpretationMode mode) throws FFSInterpretException;
    Result<ArrayList<AtomicField>> interpretData(File file, double scalingFactor, InterpretationMode mode) throws FFSInterpretException;
    Result<AtomicField> interpretDataAsStream(Stream<String> stream, double scalingFactor, InterpretationMode mode) throws FFSInterpretException;

    FFSIntensityColor mapToColor(double intensity);

}
