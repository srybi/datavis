package de.th.ro.datavis.interfaces;

import com.google.ar.sceneform.math.Vector3;

import java.util.List;

interface IInterpreter {


    /**
     * Gives back a Vextor3 List from a File, based on the given filepath
     * @param filePath path to a file
     * @return a List of Vector3's
     */
    List<Vector3> interpreteData(String filePath);



}
