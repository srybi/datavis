package de.th.ro.datavis;


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
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.interfaces.IObserver;
import de.th.ro.datavis.interpreter.ffs.FFSIntensityColor;
import de.th.ro.datavis.interpreter.ffs.FFSInterpreter;
import de.th.ro.datavis.interpreter.ffs.FFSService;
import de.th.ro.datavis.models.AtomicField;
import de.th.ro.datavis.models.MetaData;
import de.th.ro.datavis.models.Sphere;
import de.th.ro.datavis.ui.bottomSheet.BottomSheet;
import de.th.ro.datavis.ui.bottomSheet.BottomSheetHandler;
import de.th.ro.datavis.util.activity.BaseActivity;
import de.th.ro.datavis.util.constants.IntentConst;
import de.th.ro.datavis.util.enums.InterpretationMode;
import de.th.ro.datavis.util.renderable.RenderableBuilder;

import androidx.appcompat.widget.Toolbar;


public class ARActivity extends BaseActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener,
        IObserver {
    private final String TAG = "ARActivity";

    private ArFragment arFragment;

    private FFSService ffsService;
    private double maxIntensity = -1;
    private float scalingFactor = 1;
    private boolean ffsAvailable;

    private BottomSheet bottomSheet;
    private BottomSheetHandler bottomSheetHandler;
    private GestureDetector gestureDetector;

    private RenderableBuilder renderableBuilder;
    private AnchorNode anchorNode;
    private TransformableNode middleNode;

    private int antennaId;
    private String antennaURI;
    private String antennaFileName;

    private AppDatabase db;

    private LiveData<List<MetaData>> sqlQueryMetadata;
    private Observer<List<MetaData>> sqlMetadataObs;


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
        initSetup();
    }

    /**
     * initSetup does the following:
     *      - Get All frequencies and tilts of the given antenna (if existent)
     *      - (Initialize the BottomSheet and its handling)
     *      - Build all needed Renderables
     */
    private void initSetup() {

        ffsService = new FFSService(new FFSInterpreter(), this);

        List<Double> frequencies = ffsService.FrequenciesForAntenna(antennaId, ffsService.TiltForAntenna(antennaId), InterpretationMode.Logarithmic);
        List<Double> tilts;
        ffsAvailable = frequencies.size() != 0;
        //only initialize bottom sheet, if there is a ffs data to manipulate
        if(ffsAvailable){
            tilts = ffsService.TiltsForAntenna(antennaId, frequencies.get(0), InterpretationMode.Logarithmic);
            bottomSheet = new BottomSheet(this, frequencies, tilts, antennaId);
            bottomSheetHandler = new BottomSheetHandler(bottomSheet, findViewById(R.id.visualCueBottomSheet));
            gestureDetector = new GestureDetector(this, bottomSheetHandler);

        }else{
            Toast.makeText(
                    this, getString(R.string.toastFFSempty), Toast.LENGTH_LONG).show();
        }

        renderableBuilder = new RenderableBuilder(getApplicationContext(), antennaURI, antennaFileName);
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

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
    }

    /**
     * Loads all Spheres from the database, using the natural primary key
     * @param mode - Interpretation mode
     * @param frequency - Frequency
     * @param tilt - Tilt
     * @return List of all Spheres with the given natural primary key
     */
    private List<Sphere> loadCoordinates(InterpretationMode mode, double frequency, double tilt){
        List<Sphere> coordinates;
        Log.d(TAG, "Start coordinate Loading...");

        try {
            AtomicField field = ffsService.getSpheresByPrimaryKey(antennaId, frequency, tilt, mode);
            if(field == null)
                return null;
            maxIntensity = field.maxIntensity;
            coordinates = field.spheres;

        } catch(Exception e){
            Log.e(TAG, "loadCoordinates: " + e.getMessage() );
            Toast.makeText(this, getString(R.string.toast404Coordinates), Toast.LENGTH_SHORT).show();
            return new ArrayList<>();
        }

        Log.d(TAG, "Coordinate Loading done");
        Log.d(TAG, "Coordinate List Size: " + coordinates.size());
        return coordinates;
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

        processRenderList(false);


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

    /**
     * Gets all the renderables from the builder and starts the displaying process
     * @param forceDefault - boolean value, if the default antenna model should be use
     */
    private void processRenderList(boolean forceDefault){
        Log.d(TAG, "Start processing RenderList");

        //forceDefault = true, is only used if the first try of rendering failed.
        //After the first try the middleNode is already existing.
        if(!forceDefault){
            middleNode = new TransformableNode(arFragment.getTransformationSystem());
            middleNode.setParent(anchorNode);
        }

        Map<String, Renderable> list = renderableBuilder.getRenderables(forceDefault);
        attachAntennaToAnchorNode(middleNode, list.get("antenne"));

        if(ffsAvailable){
            List<Sphere> cords = loadCoordinates(bottomSheet.getMode(), bottomSheet.getFrequency(), bottomSheet.getTilt());
            processCordList(middleNode, list, cords);
        }
        Log.d(TAG, "Processing RenderList Done");
    }

    /**
     * This method assigns a colored sphere for every coordinate point, depending on its intensity
     * @param middle - Node where all spheres get attached to
     * @param list - List of all colour sphere renderables
     * @param cords - List of all found coordinates
     */
    private void processCordList(TransformableNode middle, Map<String, Renderable> list, List<Sphere> cords){
        scalingFactor = calcScalingFactor(maxIntensity);
        Log.d(TAG, "ScalingFactor " + scalingFactor);

        if (cords == null){
            Toast.makeText(this, getString(R.string.toastFreqTiltMismatch) , Toast.LENGTH_LONG).show();
            return;
        }

        for(Sphere s : cords){
            FFSIntensityColor intensityColor = ffsService.mapToColor(s.getIntensity(), maxIntensity);
            attachSphereToAnchorNode(middle, list.get(intensityColor.getName()+"Sphere"), s, scalingFactor);
        }
    }

    /**
     * Displays antenna model in AR
     * @param middle - Node where the antenna is attached to
     * @param renderable - antenna model
     */
    private void attachAntennaToAnchorNode(TransformableNode middle, Renderable renderable) {
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.getScaleController().setMaxScale(0.20f);
        model.getScaleController().setMinScale(0.15f);
        model.setParent(middle);

        try{
            model.setRenderable(renderable)
                    .animate(true).start();
        }catch(Exception e) {
            Log.e(TAG, "attachAntennaToAnchorNode: failed because of corrupt glb file");
            Toast.makeText(this, getString(R.string.toastCorruptAntenna), Toast.LENGTH_LONG).show();
            middle.removeChild(model);
            processRenderList(true);
        }
    }

    /**
     * Displays a single Sphere in AR
     * @param middleNode - Node where sphere is attached to
     * @param renderable - Sphere
     * @param sphere - Coordinates of the Sphere
     * @param scalingFactor - scalingFactor
     */
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
        return (float) (target / maxIntensity);
    }

    /**
     * Method of the interface IObserver. Used if the BottomSheet has changed
     */
    @Override
    public void update() {
        Log.d(TAG, "update: the bottomsheet called an update");
        removeEverything();
        updateFFSCreatingData();

        //Metadaten werden neu geladen
        sqlQueryMetadata = db.metadataDao().findAll_Background(antennaId,bottomSheet.getFrequency(),bottomSheet.getTilt());
        sqlQueryMetadata.removeObservers(this);
        createMetaDataObserver();

        processRenderList(false);
    }

    private void removeEverything(){
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


    //NEED TO BE REFACTOR
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

    //NEED TO BE REFACTOR
    private void createMetaDataObserver(){
        sqlMetadataObs  = this::updateMetadata;
        try {
            sqlQueryMetadata.observe(this, sqlMetadataObs);
        } catch (NullPointerException e) {
            Log.d(TAG, "Couldn't create observer:" + e.getMessage());
        }
    }

    /**
     *  TOP LEFT Updates Metadata
     */

    //NEED TO BE REFACTOR
    private void updateMetadata(List<MetaData> changeMetaData){
        for(MetaData m: changeMetaData) {
            int resID = this.getResources().getIdentifier(("field_" + m.getType()), "id", this.getPackageName());
            try {
                TextView textView = findViewById(resID);
                textView.setVisibility(View.VISIBLE);
                switch (m.getType()) {
                    case "HHPBW_deg":
                        String hhpbw=  getResources().getString(R.string.HHPBW_deg,m.getValue());
                        textView.setText(hhpbw);
                        break;
                    case "VHPBW_deg":
                        String vhpbw =  getResources().getString(R.string.VHPBW_deg,m.getValue());
                        textView.setText(vhpbw);
                        break;
                    case "Directivity_dBi":
                        String directivity =  getResources().getString(R.string.Directivity_dBi,m.getValue());
                        textView.setText(directivity);
                        break;
                    default:
                        textView.setText(m.getValue());
                        break;
                }

                Log.d(TAG, "TextView " + textView + " updated to: " + m.getValue());
            } catch (Exception e) {
                Log.d(TAG,"Could not populated Metadata: "+e.getMessage());
            }
        }
    }

    /**
     *  TOP RIGHT: Updates Frequency, Tilt and ViewMode textviews
     */

    //NEED TO BE REFACTOR
    private void updateFFSCreatingData(){
        TextView tvFreq = findViewById(R.id.field_Frequency);
        TextView tvTilt = findViewById(R.id.field_Tilt);
        TextView tvViewMode = findViewById(R.id.field_ViewMode);

        String freqString = getString(R.string.label_frequency, bottomSheet.getFrequency());
        tvFreq.setText(freqString);

        String tiltString = getString(R.string.label_tilt, bottomSheet.getTilt());
        tvTilt.setText(tiltString);

        if(bottomSheet.getMode().name().equals("Linear")){
            tvViewMode.setText(getResources().getString(R.string.linearView));
        } else tvViewMode.setText(getResources().getString(R.string.logView));
    }

}
