package de.th.ro.datavis;

import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener {

    private ArFragment arFragment;
    private Renderable model;
    private Renderable sphereBlue;
    private ViewRenderable viewRenderable;

    private List<Renderable> renderableList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getSupportFragmentManager().addFragmentOnAttachListener(this);

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }

        //loadModels();
        //loadSpheres();
        loadSphereList();
    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.arFragment) {
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

    public void loadModels() {
        WeakReference<MainActivity> weakActivity = new WeakReference<>(this);

        // Uri.parse("models/dragon.glb")


        ModelRenderable.builder()
                //.setSource(this, Uri.parse("https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb"))
                .setSource(this, Uri.parse("models/hex.gltf"))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    MainActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.model = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });


        ViewRenderable.builder()
                .setView(this, R.layout.view_model_title)
                .build()
                .thenAccept(viewRenderable -> {
                    MainActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.viewRenderable = viewRenderable;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }


    private void loadSpheres(){

        WeakReference<MainActivity> weakActivity = new WeakReference<>(this);


        MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.RED))
                .thenAccept(
                        material -> {
                            ModelRenderable redSphereRenderable = ShapeFactory.makeSphere(0.1f, new Vector3(0.0f, 0.15f, 0.0f), material);
                            MainActivity activity = weakActivity.get();
                            if (activity != null) {
                                activity.model = redSphereRenderable;
                            }

                        });

        MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.BLUE))
                .thenAccept(
                        material -> {
                            ModelRenderable redSphereRenderable = ShapeFactory.makeSphere(0.2f, new Vector3(0.0f, 0.55f, 0.0f), material);

                            MainActivity activity = weakActivity.get();
                            if (activity != null) {
                                activity.sphereBlue = redSphereRenderable;
                            }

                        });




    }


    private void loadSphereList(){

        WeakReference<MainActivity> weakActivity = new WeakReference<>(this);

        float zOffsetAddition = 0.02f; // 1f = Meter
        int repititionCount = 200; // *3

        MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.RED))
                .thenAccept(
                        material -> {
                            float zOffset = 0;
                            for (int i = 0 ; i < repititionCount ; i++){
                                zOffset += zOffsetAddition;
                                ModelRenderable sphere = ShapeFactory.makeSphere(0.01f, new Vector3(0.0f, 0.0f + zOffset, 0.0f), material);
                                renderableList.add(sphere);
                            }
                        });

        MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.BLUE))
                .thenAccept(
                        material -> {
                            float zOffset = 0;
                            for (int i = 0 ; i < repititionCount ; i++){
                                zOffset += zOffsetAddition;
                                ModelRenderable sphere = ShapeFactory.makeSphere(0.01f, new Vector3(0.1f, 0.0f + zOffset, 0.0f), material);
                                renderableList.add(sphere);
                            }
                        });
        MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.GREEN))
                .thenAccept(
                        material -> {
                            float zOffset = 0;
                            for (int i = 0 ; i < repititionCount ; i++){
                                zOffset += zOffsetAddition;
                                ModelRenderable sphere = ShapeFactory.makeSphere(0.01f, new Vector3(-0.1f, 0.0f + zOffset, 0.0f), material);
                                renderableList.add(sphere);
                            }
                        });



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

        // Create the transformable model and add it to the anchor.
//        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
//        model.setParent(anchorNode);
//        model.setRenderable(this.model)
//                .animate(true).start();
//        model.select();


        for (Renderable renderable : renderableList){

            TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
            model.setParent(anchorNode);
            model.setRenderable(renderable)
                    .animate(true).start();
            model.select();

        }

//        Model Title - not used
//        Node titleNode = new Node();
//        titleNode.setParent(model);
//        titleNode.setEnabled(false);
//        titleNode.setLocalPosition(new Vector3(0.0f, 1.0f, 0.0f));
//        titleNode.setRenderable(viewRenderable);
//        titleNode.setEnabled(true);
    }
}