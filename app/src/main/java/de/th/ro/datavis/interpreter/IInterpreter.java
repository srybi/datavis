package de.th.ro.datavis.interpreter;

import android.util.Pair;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.stream.Stream;

import de.th.ro.datavis.models.AtomicField;
import de.th.ro.datavis.models.Result;
import de.th.ro.datavis.util.constants.InterpretationMode;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public interface IInterpreter {
    /**
     * Gives back a Vextor3 List from a File, based on the given inputstream or file.
     * @return a List of Vector3's
     */
    Result<Pair<ArrayList<AtomicField>, ArrayList<AtomicField>>> interpretData(InputStream stream, double scalingFactor, double tiltValue, int antennaId) throws FFSInterpretException;

    Result<Pair<ArrayList<AtomicField>, ArrayList<AtomicField>>> interpretData(File file, double scalingFactor, double tiltValue, int antennaId) throws FFSInterpretException;

    Result<AtomicField> interpretDataAsStream(Stream<String> stream, double scalingFactor, InterpretationMode mode) throws FFSInterpretException;

}
