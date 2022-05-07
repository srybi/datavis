package de.th.ro.datavis.interfaces;

import com.google.ar.sceneform.math.Vector3;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import de.th.ro.datavis.util.enums.InterpretationMode;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public interface IInterpreter {
    /**
     * Gives back a Vextor3 List from a File, based on the given inputstream or file.
     * @return a List of Vector3's
     */
    List<Vector3> interpretData(InputStream stream, double scalingFactor, InterpretationMode mode) throws FFSInterpretException;
    List<Vector3> interpretData(File file, double scalingFactor, InterpretationMode mode) throws FFSInterpretException;
}
