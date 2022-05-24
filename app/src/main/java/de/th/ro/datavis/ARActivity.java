package de.th.ro.datavis;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.utilities.ChangeId;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.th.ro.datavis.interfaces.IInterpreter;
import de.th.ro.datavis.interpreter.ffs.FFSInterpreter;
import de.th.ro.datavis.models.Sphere;
import de.th.ro.datavis.util.activity.BaseActivity;
import de.th.ro.datavis.util.enums.InterpretationMode;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public class ARActivity extends BaseActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener {


    private ArFragment arFragment;
    private InterpretationMode mode = InterpretationMode.Linear;
    private Map<String, Renderable> renderableList = new HashMap<>();
    private ChangeId modelChangeID;
    private IInterpreter ffsInterpreter;
    private Uri fileUri;
    private String TAG = "myTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "StartUp ARActivity");
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().addFragmentOnAttachListener(this);

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.mainFragment, ArFragment.class, null)
                        .commit();
            }
        }

        Bundle b = getIntent().getExtras();
        String uriString = b.getString("fileUri");
        try {
            mode = InterpretationMode.valueOf(b.getString("interpretationMode"));

        }catch (IllegalArgumentException ignored){

        }
        fileUri = Uri.parse(uriString);

        ffsInterpreter = new FFSInterpreter();


        buildAntennaModel();
        buildSpheres();
    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.mainFragment) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this);
            arFragment.setOnViewCreatedListener(this);
            arFragment.setOnTapArPlaneListener(this);
        }
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
    }

    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        arFragment.setOnViewCreatedListener(null);
        arSceneView.getPlaneRenderer().setShadowReceiver(false);

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
    }

    private void buildAntennaModel(){
        ModelRenderable.builder()
                .setSource(this, Uri.parse("models/datavis_antenna_asm.glb"))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    renderableList.put("antenne", model);
                    modelChangeID = model.getId();
                    Log.d(TAG, "Antenna model done");
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    private void buildSpheres(){
        //red
        MaterialFactory.makeTransparentWithColor(this, new Color(1f, 0f, 0f))
                .thenAccept(
                        material -> {
                            ModelRenderable sphere = ShapeFactory.makeSphere(0.0065f, new Vector3(), material);
                            renderableList.put("redSphere", sphere);
                                    });
        //orange
        MaterialFactory.makeTransparentWithColor(this, new Color(0.9f, 0.47f, 0.23f))
                .thenAccept(
                        material -> {
                            ModelRenderable sphere = ShapeFactory.makeSphere(0.0065f, new Vector3(), material);
                            renderableList.put("orangeSphere", sphere);
                        });
        //yellow
        MaterialFactory.makeTransparentWithColor(this, new Color(1f, 0.95f, 0.02f))
                .thenAccept(
                        material -> {
                            ModelRenderable sphere = ShapeFactory.makeSphere(0.0065f, new Vector3(), material);
                            renderableList.put("yellowSphere", sphere);
                        });
        //green
        MaterialFactory.makeTransparentWithColor(this, new Color(0.49f, 1f, 0f))
                .thenAccept(
                        material -> {
                            ModelRenderable sphere = ShapeFactory.makeSphere(0.0065f, new Vector3(), material);
                            renderableList.put("greenSphere", sphere);
                        });
        //babyBlue
        MaterialFactory.makeTransparentWithColor(this, new Color(0.23f, 0.93f, 0.9f))
                .thenAccept(
                        material -> {
                            ModelRenderable sphere = ShapeFactory.makeSphere(0.0065f, new Vector3(), material);
                            renderableList.put("babyBlueSphere", sphere);
                        });
        //blue
        MaterialFactory.makeTransparentWithColor(this, new Color(0f, 1f, 0.96f))
                .thenAccept(
                        material -> {
                            ModelRenderable sphere = ShapeFactory.makeSphere(0.0065f, new Vector3(), material);
                            renderableList.put("blueSphere", sphere);
                        });
    }

    private List<Sphere> loadCoordinates(InterpretationMode mode){
        List<Sphere> coordinates = null;
        Log.d(TAG, "Start coordinate Loading...");

        try {
            if(fileUri == null){
                File file = new File("/storage/self/primary/Download/20220331_Felddaten_Beispiel.ffs");
                coordinates = ffsInterpreter.interpretData(file, 0.2, mode);
            }else{
                getContentResolver().takePersistableUriPermission(fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                coordinates = ffsInterpreter.interpretData(inputStream, 0.4, mode);
            }

        } catch (FFSInterpretException | FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }catch (SecurityException se){
            Toast.makeText(this, "Unable to load the file, due to missing permissions.", Toast.LENGTH_SHORT).show();
            return null;
        }

        Log.d(TAG, "Coordinate Loading done");
        Log.d(TAG, "Coordinate List Size: " + coordinates.size());
        return coordinates;
    }


    private List<Renderable> getRenderList(){
        return new ArrayList<Renderable>();
    }



    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        Log.d(TAG, "Plane Tab");

        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        processRenderList(anchorNode, renderableList, loadCoordinates(mode));
    }


    private void processRenderList(AnchorNode anchorNode, Map<String, Renderable> list, List<Sphere> coords){
        Log.d(TAG, "Start processing RenderList");
        attachAntennaToAnchorNode(anchorNode, list.get("antenne"));
        int i = 0;
        for(Sphere s : coords){
            int color = ffsInterpreter.mapToColor(s.getIntensity());
            switch (color){
                case 1:
                    attachSphereToAnchorNode(anchorNode, list.get("redSphere"), s);
                    break;
                case 2:
                    attachSphereToAnchorNode(anchorNode, list.get("orangeSphere"), s);
                    break;
                case 3:
                    attachSphereToAnchorNode(anchorNode, list.get("yellowSphere"), s);
                    break;
                case 4:
                    attachSphereToAnchorNode(anchorNode, list.get("greenSphere"), s);
                    break;
                case 5:
                    attachSphereToAnchorNode(anchorNode, list.get("babyBlueSphere"), s);
                    break;
                case 6:
                    attachSphereToAnchorNode(anchorNode, list.get("blueSphere"), s);
                    break;
            }
            i++;
            Log.d(TAG, "processRenderList: proccessing #" + i);
        }
        Log.d(TAG, "Processing RenderList Done");
    }

    private void attachAntennaToAnchorNode(AnchorNode anchorNode, Renderable renderable) {
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.getScaleController().setMaxScale(0.20f);
        model.getScaleController().setMinScale(0.15f);
        model.setParent(anchorNode);
        model.setRenderable(renderable)
                .animate(true).start();
        model.select();
    }


    private void attachSphereToAnchorNode(AnchorNode anchorNode, Renderable renderable, Sphere sphere){
        float yOffset = 0.1f;
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setLocalPosition(new Vector3((float) sphere.getX(), (float) sphere.getY() + yOffset, (float) sphere.getZ()));
        model.setParent(anchorNode);
        model.setRenderable(renderable)
                .animate(true).start();
        model.select();
    }

}
