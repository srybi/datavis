package de.th.ro.datavis.util.renderable;

import android.content.Context;

import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import java.util.ArrayList;
import java.util.List;

public class DataVisRenderableUtil {


    public List<Renderable> loadSphereTower(Context context){

        float zOffsetAddition = 0.04f; // 1f = Meter
        int repititionCount = 50; // *3

        float sphereRadius = 0.01f;

        List<Renderable> renderableList = new ArrayList<>();

        MaterialFactory.makeOpaqueWithColor(context, new Color(android.graphics.Color.BLUE))
                .thenAccept(
                        material -> {
                            float zOffset = 0;
                            for (int i = 0 ; i < repititionCount ; i++){
                                zOffset += zOffsetAddition;
                                ModelRenderable sphere = ShapeFactory.makeSphere(sphereRadius, new Vector3(-0.1f, 0.0f + zOffset, 0.0f), material);
                                renderableList.add(sphere);
                            }
                        });

        return renderableList;

    }

}
