package de.th.ro.datavis;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
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
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
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
import de.th.ro.datavis.interfaces.IObserver;
import de.th.ro.datavis.interpreter.ffs.FFSIntensityColor;
import de.th.ro.datavis.interpreter.ffs.FFSInterpreter;
import de.th.ro.datavis.models.AtomicField;
import de.th.ro.datavis.models.Result;
import de.th.ro.datavis.models.Sphere;
import de.th.ro.datavis.ui.bottomSheet.BottomSheet;
import de.th.ro.datavis.ui.bottomSheet.BottomSheetHandler;
import de.th.ro.datavis.util.activity.BaseActivity;
import de.th.ro.datavis.util.enums.InterpretationMode;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public class ARActivity extends BaseActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener,
        IObserver {


    private ArFragment arFragment;
    private Map<String, Renderable> renderableList = new HashMap<>();
    private IInterpreter ffsInterpreter;
    private BottomSheet bottomSheet;
    private GestureDetector gestureDetector;
    private AnchorNode anchorNode;
    private TransformableNode middleNode;
    private Uri fileUri;
    private String TAG = "myTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "StartUp ARActivity");
        setContentView(R.layout.activity_ar);
        getSupportFragmentManager().addFragmentOnAttachListener(this);

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }

        bottomSheet = new BottomSheet(this);
        gestureDetector = new GestureDetector(this, new BottomSheetHandler(bottomSheet));

        Bundle b = getIntent().getExtras();
        String uriString = b.getString("fileUri");
        fileUri = Uri.parse(uriString);

        ffsInterpreter = new FFSInterpreter();


        buildAntennaModel();
        buildSpheres();
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
                    Log.d(TAG, "Antenna model done");
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    private void buildSpheres(){
        for(FFSIntensityColor intensityColor : FFSIntensityColor.values()){
            MaterialFactory.makeTransparentWithColor(this, intensityColor.getColor())
                    .thenAccept(
                            material -> {
                                ModelRenderable sphere = ShapeFactory.makeSphere(0.0065f, new Vector3(), material);
                                renderableList.put(intensityColor.getName() + "Sphere", sphere);
                            });
        }
    }

    private List<Sphere> loadCoordinates(InterpretationMode mode){
        List<Sphere> coordinates = null;
        Log.d(TAG, "Start coordinate Loading...");

        try {
            if(fileUri == null){
                File file = new File("/storage/self/primary/Download/20220331_Felddaten_Beispiel.ffs");
                //Result<AtomicField> result = ffsInterpreter.interpretData(file, 0.2, bottomSheet.getMode());
                //coordinates = result.getData().spheres;
            }else{
                getContentResolver().takePersistableUriPermission(fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                //Result<AtomicField> result = ffsInterpreter.interpretData(inputStream, 0.4, bottomSheet.getMode());
                //coordinates = result.getData().spheres;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch(SecurityException se){
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
        //saving the anchorNode for removing later
        this.anchorNode = anchorNode;
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        processRenderList(anchorNode, renderableList, loadCoordinates(bottomSheet.getMode()));
        bottomSheet.subscribe(this);
    }


    private void processRenderList(AnchorNode anchorNode, Map<String, Renderable> list, List<Sphere> coords){
        Log.d(TAG, "Start processing RenderList");
        attachAntennaToAnchorNode(anchorNode, list.get("antenne"));

        middleNode = new TransformableNode(arFragment.getTransformationSystem());
        middleNode.setParent(anchorNode);
        int i = 0;
        for(Sphere s : coords){
            FFSIntensityColor intensityColor = ffsInterpreter.mapToColor(s.getIntensity());
            attachSphereToAnchorNode(middleNode, list.get(intensityColor.getName()+"Sphere"), s);
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


    private void attachSphereToAnchorNode(TransformableNode middleNode, Renderable renderable, Sphere sphere){
        float yOffset = 0.1f;
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setLocalPosition(new Vector3((float) sphere.getX(), (float) sphere.getY() + yOffset, (float) sphere.getZ()));
        model.setParent(middleNode);
        model.setRenderable(renderable)
                .animate(true).start();
        model.select();
    }

    //Observer Pattern
    @Override
    public void update() {
        Log.d(TAG, "update: the bottomsheet called an update");

        deleteAllSheres();
        processRenderList(anchorNode, renderableList, loadCoordinates(bottomSheet.getMode()));
    }

    private void deleteAllSheres(){
        anchorNode.removeChild(middleNode);
    }

    //Used for gestureDetection
    @Override
    public boolean onTouchEvent(MotionEvent event){
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    //Used for gestureDetection
    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        this.gestureDetector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

}
