package de.th.ro.datavis.util.renderable;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import java.util.HashMap;
import java.util.Map;

import de.th.ro.datavis.util.constants.FFSIntensityColor;
import de.th.ro.datavis.util.FileProviderDatavis;

public class RenderableBuilder {

    private final String TAG = "RenderableBuilder";

    private Context context;
    private String antennaURI;
    private String antennaFileName;

    private Map<String, Renderable> renderableSpheres;
    private Renderable givenAntenna;
    private Renderable defaultAntenna;

    public RenderableBuilder(Context ctx,String antennaURI, String antennaFileName){
        this.context=ctx;
        this.antennaURI = antennaURI;
        this.antennaFileName = antennaFileName;
        this.renderableSpheres = new HashMap<>();
        build();

    }

    private boolean checkGivenAntennaModel(){
        if (antennaFileName == null){
            Log.d(TAG, "checkGivenAntennaModel: No antenna model given");
            // antennaFileName == null -> No Antenna chosen -> Use Default Antenna
            return false;
        }
        if(antennaFileName.equals("datavis_default")){
            Log.d(TAG, "checkGivenAntennaModel: Using default model");
            return false;
        }
        Uri antennaUri = FileProviderDatavis.getURIForAntenna(context, antennaFileName);
        if (antennaUri == null){
            Log.d(TAG, "checkGivenAntennaModel: Antenna URI: File not Found( " + antennaFileName + ")");
            buildDefaultModel();
            return false;
        }
        return true;
    }

    private void buildAntennaModel(){
        Log.d(TAG, "buildAntennaModel: "+ antennaURI);
        Uri antennaUri = FileProviderDatavis.getURIForAntenna(context, antennaFileName);
        ModelRenderable.builder()
                .setSource(context, antennaUri)
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    givenAntenna = model;
                    Log.d(TAG, "Antenna model done");
                })
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
    }

    private void buildDefaultModel(){
        Log.d(TAG, "building default model");
        ModelRenderable.builder()
                .setSource(context, Uri.parse("models/datavis_antenna_asm.glb"))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    defaultAntenna = model;
                    Log.d(TAG, "Antenna model done");
                }).exceptionally(throwable -> {
                        throwable.printStackTrace();
                        Log.d(TAG, "buildAntennaModel: Failed to build default model" + throwable.getMessage());
                        return null;
        });
    }

    private void buildSpheres(){
        for(FFSIntensityColor intensityColor : FFSIntensityColor.values()){
            MaterialFactory.makeTransparentWithColor(context, intensityColor.getColor())
                    .thenAccept(
                            material -> {
                                ModelRenderable sphere = ShapeFactory.makeSphere(0.0065f, new Vector3(), material);
                                renderableSpheres.put(intensityColor.getName() + "Sphere", sphere);
                            });
        }
        Log.d(TAG, "buildSpheres: done");
    }

    private void build(){
        if(checkGivenAntennaModel()){
            buildAntennaModel();
        }
        buildDefaultModel();
        buildSpheres();
    }

    public Map<String, Renderable> getRenderables(boolean forceDefault){
        Map<String, Renderable> resultMap = renderableSpheres;
        if(checkGivenAntennaModel() && !forceDefault){
            resultMap.put("antenne", givenAntenna);
        }else{
            resultMap.put("antenne", defaultAntenna);
        }
        return resultMap;
    }

}
