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
import java.util.List;

import de.th.ro.datavis.interfaces.IInterpreter;
import de.th.ro.datavis.interpreter.ffs.FFSInterpreter;
import de.th.ro.datavis.util.activity.BaseActivity;
import de.th.ro.datavis.util.enums.InterpretationMode;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public class ARActivity extends BaseActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener {


    private ArFragment arFragment;
    private ViewRenderable viewRenderable;

    private List<Renderable> renderableList = new ArrayList<>();
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
        InterpretationMode mode = InterpretationMode.Linear;
        try {
            mode = InterpretationMode.valueOf(b.getString("interpretationMode"));

        }catch (IllegalArgumentException ignored){

        }
        fileUri = Uri.parse(uriString);

        ffsInterpreter = new FFSInterpreter();

        loadSphereList(mode);
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

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
    }

    private void loadSphereList(InterpretationMode mode){
        List<Vector3> coordinates = null;

        Log.d(TAG, "Start coordinate Loading...");

        try {
            if(fileUri == null){
                File file = new File("/storage/self/primary/Download/20220331_Felddaten_Beispiel.ffs");
                coordinates = ffsInterpreter.interpretData(file, 0.2, mode);
            }else{
                getContentResolver().takePersistableUriPermission(fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                coordinates = ffsInterpreter.interpretData(inputStream, 0.2, mode);
            }

        } catch (FFSInterpretException | FileNotFoundException e) {
            e.printStackTrace();
            return;
        }catch (SecurityException se){
            Toast.makeText(this, "Unable to load the file, due to missing permissions.", Toast.LENGTH_SHORT).show();
            return;
        }


        Log.d(TAG, "Coordinate Loading done");
        Log.d(TAG, "Coordinate List Size: " + coordinates.size());


        float zOffsetAddition = 0.04f; // 1f = Meter
        int repititionCount = 50; // *3

        float sphereRadius = 0.0065f;
        float cubeSize = 0.15f;

        ModelRenderable.builder()
                .setSource(this, Uri.parse("models/datavis_antenna_asm.glb"))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    renderableList.add(model);
                    modelChangeID = model.getId();
                    Log.d(TAG, "Antenna model done");
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });

    if(coordinates != null) {
        List<Vector3> finalCoordinates = coordinates;
        MaterialFactory.makeTransparentWithColor(this, new Color(0.0f,0.0f,1.0f,0.6f))
                .thenAccept(
                        material -> {
                            float zOffset = 0;
                            int i = 0;

                            for (Vector3 vector3 : finalCoordinates) {

                                if (i == 2000){
                                    // do something
                                    // DK: Ab 2550 Crasht mein Gerät
                                    continue;
                                }

                                zOffset += zOffsetAddition;
                                ModelRenderable sphere = ShapeFactory.makeSphere(sphereRadius, vector3, material);
                                renderableList.add(sphere);
                                Log.d(TAG, "Model Done " + i);
                                i++;
                            }
                        });
    }

    }


    private List<Renderable> getRenderList(){
        return new ArrayList<Renderable>();
    }



    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
//        if (model == null ) {
//            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
//            return;
//        }

        Log.d(TAG, "Plane Tab");

        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        processRenderList(anchorNode, renderableList);


    }


    private void processRenderList(AnchorNode anchorNode, List<Renderable> list){
        Log.d(TAG, "Start processing RenderList");
        for (Renderable renderable : list){
            attachReferableToAnchorNode(anchorNode, renderable);
        }
        Log.d(TAG, "Processing RenderList Done");
    }


    private void attachReferableToAnchorNode(AnchorNode anchorNode, Renderable renderable){
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        if(renderable.getId() == modelChangeID){
            model.getScaleController().setMaxScale(0.20f);
            model.getScaleController().setMinScale(0.15f);
        }
        model.setParent(anchorNode);
        model.setRenderable(renderable)
                .animate(true).start();
        model.select();
    }

}
