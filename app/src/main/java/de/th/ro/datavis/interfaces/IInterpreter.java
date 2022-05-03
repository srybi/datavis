package de.th.ro.datavis.interfaces;

import com.google.ar.sceneform.math.Vector3;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public interface IInterpreter {
    /**
     * Gives back a Vextor3 List from a File, based on the given filepath
     * @param file
     * @return a List of Vector3's
     */
    List<Vector3> interpretData(File file) throws FFSInterpretException;
}
