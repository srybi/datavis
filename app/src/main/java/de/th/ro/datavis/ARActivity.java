package de.th.ro.datavis;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

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
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.interfaces.IInterpreter;
import de.th.ro.datavis.interfaces.IObserver;
import de.th.ro.datavis.interpreter.ffs.FFSIntensityColor;
import de.th.ro.datavis.interpreter.ffs.FFSService;
import de.th.ro.datavis.models.AtomicField;
import de.th.ro.datavis.models.MetaData;
import de.th.ro.datavis.models.Sphere;
import de.th.ro.datavis.ui.bottomSheet.BottomSheet;
import de.th.ro.datavis.ui.bottomSheet.BottomSheetHandler;
import de.th.ro.datavis.util.FileProviderDatavis;
import de.th.ro.datavis.util.activity.BaseActivity;
import de.th.ro.datavis.util.constants.IntentConst;
import de.th.ro.datavis.util.constants.MetadataType;
import de.th.ro.datavis.util.enums.InterpretationMode;
import androidx.appcompat.widget.Toolbar;


public class ARActivity extends BaseActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener,
        IObserver {


    private ArFragment arFragment;
    private Map<String, Renderable> renderableList = new HashMap<>();

    private IInterpreter ffsInterpreter;
    private FFSService ffsService;

    private BottomSheet bottomSheet;
    private BottomSheetHandler bottomSheetHandler;
    private GestureDetector gestureDetector;
    private AnchorNode anchorNode;
    private TransformableNode middleNode;
    private int antennaId;
    private String antennaURI;
    private String antennaFileName;
    private String TAG = "ARActivity";
    private boolean ffsAvailable;
    private AppDatabase db;

    private LiveData<List<MetaData>> sqlQueryMetadata;
    private Observer<List<MetaData>> sqlMetadataObs;

    private double maxIntensity = -1;
    private float scalingFactor = 1;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "StartUp ARActivity");
        setContentView(R.layout.activity_ar);
        getSupportFragmentManager().addFragmentOnAttachListener(this);

        //get toolbar and set default back button
        Toolbar toolbar = findViewById(R.id.toolbar_ar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getInstance(this);

        //set fragment
        if (Sceneform.isSupported(this)) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.arFragment, ArFragment.class, null)
                    .commit();
        }

        if (savedInstanceState == null) {
            savedInstanceState = getIntent().getExtras();
        }
        antennaId = savedInstanceState.getInt("antennaId");
        antennaURI = savedInstanceState.getString("antennaURI");
        antennaFileName = savedInstanceState.getString(IntentConst.INTENT_EXTRA_ANTENNA_FILENAME);
        initalSetup(antennaId, antennaURI, antennaFileName);
    }



    private void initalSetup(int internalAntennaId, String internalAntennaURI, String internalAntennaFileName) {

        ffsService = new FFSService(ffsInterpreter, this);



        List<Double> frequencies = ffsService.FrequenciesForAntenna(internalAntennaId, ffsService.TiltForAntenna(internalAntennaId), InterpretationMode.Logarithmic);
        List<Double> tilts;
        ffsAvailable = frequencies.size() != 0;
        //only initialize bottom sheet, if there is a ffs data to manipulate
        if(ffsAvailable){
            tilts = ffsService.TiltsForAntenna(internalAntennaId, frequencies.get(0), InterpretationMode.Logarithmic);
            bottomSheet = new BottomSheet(this, frequencies, tilts, internalAntennaId);
            bottomSheetHandler = new BottomSheetHandler(bottomSheet, findViewById(R.id.visualCueBottomSheet));
            gestureDetector = new GestureDetector(this, bottomSheetHandler);

        }else{
            Toast.makeText(
                    this, "No ffs data to display!", Toast.LENGTH_LONG).show();
        }


        buildAntennaModel();
        buildSpheres();
        Log.d(TAG, "onCreate: " + renderableList.size());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(outState);
        // Save our own state now
        outState.putInt("antennaId", antennaId);
        outState.putString("antennaURI", antennaURI);
        outState.putString(IntentConst.INTENT_EXTRA_ANTENNA_FILENAME, antennaFileName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)  {
        getMenuInflater().inflate(R.menu.menu_ar, menu);

        MenuItem itemSettings = menu.findItem(R.id.app_settings);

        itemSettings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //open bottom sheet
                bottomSheetHandler.showBottomSheet();
                return false;
            }
        });
        return true;
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
        if (arSceneView.hasTrackedPlane())
            findViewById(R.id.visualCue).setVisibility(View.VISIBLE);

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
    }

    private void buildAntennaModel(){
        Log.d(TAG, "buildAntennaModel: "+ antennaURI);

        if (antennaFileName == null || antennaFileName.equals("datavis_default")){
            // antennaFileName == null -> No Antenna chosen -> Use Default Antenna
            buildDefaultModel();
            return;
        }

        Uri antennaUri = FileProviderDatavis.getURIForAntenna(getApplicationContext(), antennaFileName);
        if (antennaUri == null){

            Toast.makeText(this,  "File not Found: " + antennaFileName + " -> Building Default Antenna", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Antenna URI: File not Found: " + antennaFileName);
            buildDefaultModel();
            return;
        }

            Log.d(TAG, "buildAntennaModel: "+ antennaURI);
            ModelRenderable.builder()
                    .setSource(this, antennaUri)
                    .setIsFilamentGltf(true)
                    .setAsyncLoadEnabled(true)
                    .build()
                    .thenAccept(model -> {
                        renderableList.put("antenne", model);
                        Log.d(TAG, "Antenna model done");
                    })
                    .exceptionally(throwable -> {
                        throwable.printStackTrace();
                        Log.d(TAG, "buildAntennaModel: Failed to build antenna model" + throwable.getMessage());
                        buildDefaultModel();
                        return null;
                    });
    }

    private void buildDefaultModel(){
            Log.d(TAG, "building default model");
            ModelRenderable.builder()
                    .setSource(this, Uri.parse("models/datavis_antenna_asm.glb"))
                    .setIsFilamentGltf(true)
                    .setAsyncLoadEnabled(true)
                    .build()
                    .thenAccept(model -> {
                        renderableList.put("antenne", model);
                        handleCorruptGLB(model);
                        Log.d(TAG, "Antenna model done");
                    }).exceptionally(throwable -> {
                         throwable.printStackTrace();
                         Log.d(TAG, "buildAntennaModel: Failed to build default model" + throwable.getMessage());
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
        Log.d(TAG, "buildSpheres: done");
    }

    private List<Sphere> loadCoordinates(InterpretationMode mode, double frequency, double tilt){
        List<Sphere> coordinates = null;
        Log.d(TAG, "Start coordinate Loading...");

        try {
            AtomicField field = ffsService.getSpheresByPrimaryKey(antennaId, frequency, tilt, mode);
            if(field == null)
                return null;
            maxIntensity = field.maxIntensity;
            coordinates = field.spheres;

        } catch(Exception e){
            Log.e(TAG, "loadCoordinates: " + e.getMessage() );
            Toast.makeText(this, "Unable to load the coordinates from the database.", Toast.LENGTH_SHORT).show();
            return new ArrayList<Sphere>();
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

        findViewById(R.id.visualCue).setVisibility(View.GONE);

        if (anchorNode != null){
            // Antenna is already Placed
            return;
        }

        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        //saving the anchorNode for removing later
        this.anchorNode = anchorNode;
        anchorNode.setParent(arFragment.getArSceneView().getScene());
        List<Sphere> list = new ArrayList<>();

        if(ffsAvailable){
            list = loadCoordinates(bottomSheet.getMode(), bottomSheet.getFrequency(), bottomSheet.getTilt());
        }

        //If List is null -> No corresponding atomic field was found
        if(list != null)
            processRenderList(anchorNode, renderableList, list);
        else
            Toast.makeText(this, "No Spheres found", Toast.LENGTH_SHORT).show();


        if(ffsAvailable){
            //Initializes Metadata
            Log.d(TAG, "onTapPlane: Initilizing metadata");
            try { readMetaDataFromDB(); } catch (Exception e) { e.printStackTrace();}
            createMetaDataObserver();
            bottomSheetHandler.makeCueVisible(true);
            bottomSheet.subscribe(this);
            updateFFSCreatingData();
        }

    }

    private void processRenderList(AnchorNode anchorNode, Map<String, Renderable> list, List<Sphere> coords){
        Log.d(TAG, "Start processing RenderList");
        attachAntennaToAnchorNode(anchorNode, list.get("antenne"));

        middleNode = new TransformableNode(arFragment.getTransformationSystem());
        middleNode.setParent(anchorNode);

        if(ffsAvailable){
            proccessCoordList(middleNode, list, coords);
        }
        Log.d(TAG, "Processing RenderList Done");
    }

    private void proccessCoordList(TransformableNode middle,  Map<String, Renderable> list, List<Sphere> coords){
        scalingFactor = calcScalingFactor(maxIntensity);
        Log.d(TAG, "ScalingFactor " + scalingFactor);
        int i = 0;

        if (coords == null){
            Toast.makeText(this, "Keine Coordinaten für diese Kombination von Frequenz und Tilt vorhanden" , Toast.LENGTH_LONG).show();
            return;
        }

        for(Sphere s : coords){
            FFSIntensityColor intensityColor = ffsService.mapToColor(s.getIntensity(), maxIntensity);
            attachSphereToAnchorNode(middle, list.get(intensityColor.getName()+"Sphere"), s, scalingFactor);
            i++;
        }
    }

    private void attachAntennaToAnchorNode(AnchorNode anchorNode, Renderable renderable) {
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.getScaleController().setMaxScale(0.20f);
        model.getScaleController().setMinScale(0.15f);
        model.setParent(anchorNode);

        try{
            model.setRenderable(renderable)
                    .animate(true).start();
        }catch(Exception e ){
            Log.e(TAG, "attachAntennaToAnchorNode: failed because of corrupt glb file");
            Toast.makeText(this, "Found corrupt .glb file! Displaying default antenna", Toast.LENGTH_LONG).show();
            anchorNode.removeChild(model);
            renderableList.put("antenne", renderable);
            buildDefaultModel();

        }finally {
            model.select();
        }
    }

    private void handleCorruptGLB(Renderable renderable){
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.getScaleController().setMaxScale(0.1f);
        model.getScaleController().setMinScale(0.05f);
        model.setParent(anchorNode);
        model.setRenderable(renderable)
                .animate(true).start();
                model.select();
    }


    private void attachSphereToAnchorNode(TransformableNode middleNode, Renderable renderable, Sphere sphere, float scalingFactor){
        float yOffset = 0.1f;
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setLocalPosition(new Vector3((float) sphere.getX() * scalingFactor, (float) sphere.getY() * scalingFactor + yOffset, (float) sphere.getZ() * scalingFactor));
        model.setParent(middleNode);
        model.setRenderable(renderable)
                .animate(true).start();
        model.select();
    }

    /**
     * This calculates a Scaling Factor in reference to the given maxIntensity.
     * @param maxIntensity maxIntensity of the SphereList
     * @return a ScalingFactor based on the maxIntensity
     */
    private float calcScalingFactor(double maxIntensity){
        if(maxIntensity == -1){
            Log.d(TAG, "calcScalingFactor: There is no ffs data");
            return -1 ;
        }

        float target = 0.5f;
        float factor = (float) (target / maxIntensity);

        return factor;
    }

    //Observer Pattern
    @Override
    public void update() {
        Log.d(TAG, "update: the bottomsheet called an update");
        deleteAllSpheres();
        updateFFSCreatingData();

        //Metadaten werden neu geladen
        sqlQueryMetadata = db.metadataDao().findAll_Background(antennaId,bottomSheet.getFrequency(),bottomSheet.getTilt());
        sqlQueryMetadata.removeObservers(this);
        createMetaDataObserver();

        processRenderList(anchorNode, renderableList, loadCoordinates(bottomSheet.getMode(), bottomSheet.getFrequency(), bottomSheet.getTilt()));
    }

    private void deleteAllSpheres(){
        anchorNode.removeChild(middleNode);
    }

    //Used for gestureDetection
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(ffsAvailable){
            this.gestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }
    //Used for gestureDetection
    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        if(ffsAvailable){
            this.gestureDetector.onTouchEvent(event);
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * Methods to display and update Metadata:
     * readMetaDataFromDB() fetches a List of all available Metadata for a certain AntennaID, frequency and tilt
     * createMetaDataObserver() assigns an Observer to check for updates in the LiveData
     * updateMetaData() on a change goes through the List of available Metadata and for each tries to find a TextView to update
     *      this is done by String matching the [Metadata type] to the [TextView ID]
     */
    private void readMetaDataFromDB(){
        sqlQueryMetadata = db.metadataDao().findAll_Background(antennaId,bottomSheet.getFrequency(),bottomSheet.getTilt());
        Log.d(TAG, "sqlQueryMetadata built "+sqlQueryMetadata.toString());
    }

    private void createMetaDataObserver(){
        sqlMetadataObs  = changeMetaData -> { updateMetadata(changeMetaData);};
        try {
            sqlQueryMetadata.observe(this, sqlMetadataObs);
        } catch (NullPointerException e) {}
    }

    /**
     *  TOP LEFT Updates Metadata
     */

    private void updateMetadata(List<MetaData> changeMetaData){
        for(MetaData m: changeMetaData) {
            int resID = this.getResources().getIdentifier(("field_" + m.getType()), "id", this.getPackageName());
            try {
                TextView textView = findViewById(resID);
                textView.setVisibility(View.VISIBLE);
                if(m.getType().equals("HHPBW_deg"))
                {
                    textView.setText(getResources().getString(R.string.HHPBW_deg)+" "+ m.getValue()+"°");
                } else if(m.getType().equals("VHPBW_deg")) {
                    textView.setText(getResources().getString(R.string.VHPBW_deg) + " " + m.getValue() + "°");
                } else if(m.getType().equals("Directivity_dBi")){
                    textView.setText(getResources().getString(R.string.Directivity_dBi) + " " + m.getValue() + "dBi");
                } else textView.setText(m.getValue());

                Log.d(TAG, "TextView " + textView.toString() + " updated to: " + m.getValue());
            } catch (Exception e) {
            }
        }
    }

    /**
     *  TOP RIGHT: Updates Frequency, Tilt and ViewMode textviews
     */

    private void updateFFSCreatingData(){
        TextView tvFreq = findViewById(R.id.field_Frequency);
        TextView tvTilt = findViewById(R.id.field_Tilt);
        TextView tvViewMode = findViewById(R.id.field_ViewMode);

        tvFreq.setText(getResources().getString(R.string.label_frequency)+" "+bottomSheet.getFrequency());
        tvTilt.setText(getResources().getString(R.string.label_tilt)+" "+bottomSheet.getTilt());
        if(bottomSheet.getMode().name().equals("Linear")){
            tvViewMode.setText(getResources().getString(R.string.linearView));
        } else tvViewMode.setText(getResources().getString(R.string.logView));
    }

}
