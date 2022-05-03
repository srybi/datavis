package de.th.ro.datavis;

import android.os.Bundle;
import android.view.MotionEvent;

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
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import de.th.ro.datavis.interfaces.IInterpreter;
import de.th.ro.datavis.interpreter.ffs.FFSInterpreter;
import de.th.ro.datavis.util.activity.BaseActivity;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public class ARActivity extends BaseActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener {


    private ArFragment arFragment;
    private ViewRenderable viewRenderable;

    private List<Renderable> renderableList = new ArrayList<>();

    private IInterpreter ffsInterpreter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getSupportFragmentManager().addFragmentOnAttachListener(this);

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.mainFragment, ArFragment.class, null)
                        .commit();
            }
        }

        ffsInterpreter = new FFSInterpreter();

        loadSphereList();
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

    private void loadSphereList(){
        File file = new File("/storage/self/primary/Download/20220331_Felddaten_Beispiel.ffs");
        List<Vector3> coordinates = null;
        try {
            coordinates = ffsInterpreter.interpretData(file);
        } catch (FFSInterpretException e) {
            e.printStackTrace();
        }


        float zOffsetAddition = 0.04f; // 1f = Meter
        int repititionCount = 50; // *3

        float sphereRadius = 0.01f;
        float cubeSize = 0.15f;

        MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.MAGENTA))
                .thenAccept(
                        material -> {

                            Vector3 size = new Vector3(cubeSize/2, cubeSize*3, cubeSize/2);
                            Vector3 position = new Vector3(0f,0f + cubeSize/2,0f);

                            ModelRenderable cube = ShapeFactory.makeCube(size, position, material);
                            renderableList.add(cube);

                        });

if(coordinates != null) {
    List<Vector3> finalCoordinates = coordinates;
    MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.RED))
            .thenAccept(
                    material -> {
                        float zOffset = 0;
                        for (Vector3 vector3 : finalCoordinates) {
                            zOffset += zOffsetAddition;
                            ModelRenderable sphere = ShapeFactory.makeSphere(sphereRadius, vector3, material);
                            renderableList.add(sphere);
                        }
                    });

}

    }



    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
//        if (model == null ) {
//            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
//            return;
//        }

        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());


        for (Renderable renderable : renderableList){

            TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
            model.setParent(anchorNode);
            model.setRenderable(renderable)
                    .animate(true).start();
            model.select();

        }

    }

}
