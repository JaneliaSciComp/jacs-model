package org.janelia.model.domain;

/**
 * A place for constants.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class DomainConstants {

    public static final String VALUE_PROCESSING = "Processing";
    public static final String VALUE_BLOCKED = "Blocked";
    public static final String VALUE_RETIRED = "Retired";

    public static final String VALUE_COMPRESSION_LOSSLESS = "Lossless";
    public static final String VALUE_COMPRESSION_LOSSLESS_AND_H5J = "Lossless and H5J";
    public static final String VALUE_COMPRESSION_LOSSLESS_AND_H5J_12BIT = "Lossless and H5J (12-bit)";
    public static final String VALUE_COMPRESSION_VISUALLY_LOSSLESS = "Visually Lossless";
    public static final String VALUE_COMPRESSION_VISUALLY_LOSSLESS_12BIT = "Visually Lossless (12-bit)";

    public static final String NO_CONSENSUS = "No Consensus";

    public static final String NAME_DEFAULT_WORKSPACE                = "Home";
    public static final String NAME_SHARED_DATA                      = "Shared Data";
    public static final String NAME_SPLIT_PICKING                    = "Split Picking";
    public static final String NAME_DATA_SETS                        = "Data Sets";
    public static final String NAME_FLY_LINE_RELEASES                = "Fly Line Releases";
    public static final String NAME_ALIGNMENT_BOARDS                 = "Alignment Boards";
    public static final String NAME_RETIRED_DATA                     = "Retired Data";
    public static final String NAME_BLOCKED_DATA                     = "Blocked Data";
    public static final String NAME_COLOR_DEPTH_SEARCHES             = "Color Depth Searches";
    public static final String NAME_COLOR_DEPTH_MASKS                = "Color Depth Masks";

    public static final String PREFERENCE_CATEGORY_SAMPLE_RESULT = "SampleResult";
    public static final String PREFERENCE_CATEGORY_IMAGE_TYPE = "ImageType";
    public static final String PREFERENCE_CATEGORY_TABLE_COLUMNS = "TableColumns";
    public static final String PREFERENCE_CATEGORY_DOMAIN_OBJECT_TITLES = "DomainObjectTitles";
    public static final String PREFERENCE_CATEGORY_DOMAIN_OBJECT_SUBTITLES = "DomainObjectSubtitles";
    public static final String PREFERENCE_CATEGORY_NEURON_SEPARATION_VISIBILITY = "NeuronSepVisibility";
    public static final String PREFERENCE_CATEGORY_KEYBINDS_ONTOLOGY = "Keybind:Ontology:";
    public static final String PREFERENCE_CATEGORY_SORT_CRITERIA = "SortCriteria";
    public static final String PREFERENCE_CATEGORY_MOUSELIGHT = "MouseLight";
    public static final String PREFERENCE_CATEGORY_MOUSELIGHT_TAGS = "MouseLight_Tags";
    public static final String PREFERENCE_CATEGORY_MUST_HAVE_IMAGE = "MustHaveImage";
    public static final String PREFERENCE_NAME_SAMPLE_ROOTS = "SampleRoots";

    public static final String MOUSELIGHT_GROUP_KEY = "group:mouselight";
    public static final String GENERAL_USER_GROUP_KEY = "group:workstation_users";
    
    public static final String ERROR_ONTOLOGY_NAME = "Image Evaluation";
    public static final String ERROR_ONTOLOGY_CATEGORY = "Report";

    public static final String NAME_TM_WORKSPACE_FOLDER = "Workspaces";
    public static final String NAME_TM_SAMPLE_FOLDER = "3D Tile Microscope Samples";

    public static final String FILE_STORE_CENTRAL_DIR_PROP = "FileStore.CentralDir";
    
    public static final String SAMPLE_PIPELINE_LOCK_DESCRIPTION = "Sample Processing Pipeline";
    
}
