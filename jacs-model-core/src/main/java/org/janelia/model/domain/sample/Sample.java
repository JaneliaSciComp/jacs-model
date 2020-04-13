package org.janelia.model.domain.sample;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.DomainUtils;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.SampleUtils;
import org.janelia.model.domain.interfaces.IsParent;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SAGEAttribute;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchType;
import org.janelia.model.util.ModelStringUtil;

import java.util.*;

/**
 * All the processing results of a particular specimen. Uniqueness of a Sample is determined by a combination 
 * of data set and slide code. A single sample may include many LSMs. For example, it may include images taken 
 * at multiple objectives (e.g. 20x/63x), of different anatomical areas (e.g. Brain/VNC), and of different 
 * tile regions which are stitched together.   
 *
 * Contains references to NeuronFragment objects in the fragment collection.  
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="sample",label="Sample")
@SearchType(key="sample",label="Sample")
public class Sample extends AbstractDomainObject implements IsParent {

    @SAGEAttribute(cvName="light_imagery", termName="age")
    @SearchAttribute(key="age_txt",label="Age",facet="age_s")
    private String age;

    @SAGEAttribute(cvName="light_imagery", termName="data_set")
    @SearchAttribute(key="data_set_txt",label="Data Set",facet="data_set_s")
    private String dataSet;

    @SAGEAttribute(cvName="fly", termName="effector")
    @SearchAttribute(key="effector_txt",label="Effector")
    private String effector;

    @SAGEAttribute(cvName="light_imagery", termName="gender")
    @SearchAttribute(key="gender_txt",label="Gender",facet="gender_s")
    private String gender;

    @SAGEAttribute(cvName="light_imagery", termName="mounting_protocol")
    @SearchAttribute(key="mount_protocol_txt",label="Mounting Protocol")
    private String mountingProtocol;

    @SAGEAttribute(cvName="line_query", termName="organism")
    @SearchAttribute(key="organism_txt",label="Organism")
    private String organism;

    @SAGEAttribute(cvName="line", termName="genotype")
    @SearchAttribute(key="genotype_txt",label="Genotype")
    private String genotype;

    @SAGEAttribute(cvName="line", termName="flycore_id")
    @SearchAttribute(key="flycore_id_i",label="Fly Core Id")
    private Integer flycoreId;

    @SAGEAttribute(cvName="line", termName="flycore_alias")
    @SearchAttribute(key="fcalias_s",label="Fly Core Alias")
    private String flycoreAlias;

    @SAGEAttribute(cvName="line", termName="flycore_lab")
    @SearchAttribute(key="fclab_s",label="Fly Core Lab Id",facet="fclab_s")
    private String flycoreLabId;

    @SAGEAttribute(cvName="line", termName="flycore_landing_site")
    @SearchAttribute(key="fclanding_txt",label="Fly Core Landing Site")
    private String flycoreLandingSite;

    @SAGEAttribute(cvName="line", termName="flycore_permission")
    @SearchAttribute(key="fcpermn_txt",label="Fly Core Permission",facet="fcpermn_s")
    private String flycorePermission;

    @SAGEAttribute(cvName="line", termName="flycore_project")
    @SearchAttribute(key="fcproj_txt",label="Fly Core Project",facet="fcproj_s")
    private String flycoreProject;

    @SAGEAttribute(cvName="line", termName="flycore_project_subcat")
    @SearchAttribute(key="fcsubcat_txt",label="Fly Core Subcategory")
    private String flycorePSubcategory;

    @SAGEAttribute(cvName="light_imagery", termName="imaging_project")
    @SearchAttribute(key="img_proj_txt",label="Imaging Project",facet="img_proj_s")
    private String imagingProject;
    
    @SAGEAttribute(cvName="light_imagery", termName="driver")
    @SearchAttribute(key="driver_txt",label="Driver",facet="driver_s")
    private String driver;

    @SAGEAttribute(cvName="light_imagery", termName="family")
    @SearchAttribute(key="family_txt",label="Image Family",facet="family_s")
    private String imageFamily;
    
    @SAGEAttribute(cvName="image_query", termName="line")
    @SearchAttribute(key="line_txt",label="Line")
    private String line;

    @SAGEAttribute(cvName="light_imagery", termName="vt_line")
    @SearchAttribute(key="vtline_txt",label="VT Line")
    private String vtLine;
    
    @SAGEAttribute(cvName="light_imagery", termName="publishing_name")
    @SearchAttribute(key="pubname_txt",label="Publishing Name")
    private String publishingName;

    @SAGEAttribute(cvName="light_imagery", termName="published_externally")
    @SearchAttribute(key="pubext_b",label="Published Externally")
    private String publishedExternally;
    
    @SAGEAttribute(cvName="light_imagery", termName="slide_code")
    @SearchAttribute(key="slide_code_txt",label="Slide Code")
    private String slideCode;

    @SAGEAttribute(cvName="fly", termName="cross_barcode")
    @SearchAttribute(key="cross_barcode_txt",label="Cross Barcode")
    private Integer crossBarcode;

    @SearchAttribute(key="status_txt",label="Status",facet="status_s")
    private String status;

    @SearchAttribute(key="error_type_txt",label="Error Type",facet="error_type_s")
    private String errorType;

    @SearchAttribute(key="error_op_txt",label="Error Operation",facet="error_op_s")
    private String errorOperation;

    @SearchAttribute(key="error_desc_txt",label="Error Description")
    private String errorDescription;

    @SearchAttribute(key="visited_b",label="Visited")
    private Boolean visited = false;
    
    @SearchAttribute(key="sage_synced_b",label="SAGE Synchronized",facet="sage_synced_b")
    private Boolean sageSynced = false;

    private CompressionStrategy compressionStrategy;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssX")
    @SAGEAttribute(cvName="image_query", termName="create_date")
    @SearchAttribute(key="tmog_dt",label="TMOG Date")
    private Date tmogDate;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssX")
    @SearchAttribute(key="completion_dt",label="Completion Date")
    private Date completionDate;

    private boolean basicPostProcessingSupported = false;
    
    private List<ObjectiveSample> objectiveSamples = new ArrayList<>();

    @SearchAttribute(key="blocked_b",label="Blocked")
    private Boolean blocked = false;

    @SearchAttribute(key="purged_b",label="Purged")
    private Boolean purged = false;

    @SearchAttribute(key="usage_bytes_l",label="Disk Space Usage (Bytes)")
    private Long diskSpaceUsage;

    @SAGEAttribute(cvName="light_imagery", termName="probe_set")
    @SearchAttribute(key="probeset_id_txt",label="Probe Set Identifier")
    private String probeSetIdentifier;

    @SAGEAttribute(cvName="image_query", termName="probe_set_def")
    @SearchAttribute(key="probeset_txt",label="Probe Set")
    private String probeSet;

    @SAGEAttribute(cvName="light_imagery", termName="animal_id")
    @SearchAttribute(key="animal_id_s",label="User's Animal Id")
    private String animalId;

    @SAGEAttribute(cvName="light_imagery", termName="heat_shock_minutes")
    @SearchAttribute(key="heat_shock_minutes_txt",label="Heat Shock Age Minutes")
    private String heatShockMinutes;

    @SearchAttribute(key="published_b",label="Published To Staging")
    private Boolean publishedToStaging = false;

    @JsonProperty
    public List<ObjectiveSample> getObjectiveSamples() {
        return objectiveSamples;
    }

    @JsonProperty
    public void setObjectiveSamples(List<ObjectiveSample> objectiveSamples) {
        if (objectiveSamples==null) throw new IllegalArgumentException("Property cannot be null");
        this.objectiveSamples = objectiveSamples;
        for(ObjectiveSample objectiveSample : objectiveSamples) {
            objectiveSample.setParent(this);
        }
        resortObjectiveSamples();
    }
    
    @JsonIgnore
    public void addObjectiveSample(ObjectiveSample objectiveSample) {
        objectiveSample.setParent(this);
        objectiveSamples.add(objectiveSample);
        resortObjectiveSamples();
    }

    @JsonIgnore
    private void resortObjectiveSamples() {
        Collections.sort(objectiveSamples, new Comparator<ObjectiveSample>() {
            @Override
            public int compare(ObjectiveSample o1, ObjectiveSample o2) {
                return ComparisonChain.start()
                        .compare(o1.getObjective(), o2.getObjective(), Ordering.natural().nullsLast())
                        .result();
            }
        });
    }

    @JsonIgnore
    public void removeObjectiveSample(ObjectiveSample objectiveSample) {
        if (objectiveSamples.remove(objectiveSample)) {
            objectiveSample.setParent(null);
        }
    }

    @JsonIgnore
    public ObjectiveSample getObjectiveSample(String objective) {
        for(ObjectiveSample objectiveSample : objectiveSamples) {
            if (ModelStringUtil.areEqual(objectiveSample.getObjective(),objective)) {
                return objectiveSample;
            }
        }
        return null;
    }
    
    @JsonIgnore
    public List<String> getObjectives() {
        List<String> objectives = new ArrayList<>();
        for(ObjectiveSample objectiveSample : objectiveSamples) {
            objectives.add(objectiveSample.getObjective());
        }
        return objectives;
    }
    
    @JsonIgnore
    public <T extends PipelineResult> List<T> getResultsById(Class<T> resultClass, Long resultEntityId) {
        List<T> results = new ArrayList<>();
        for(ObjectiveSample objectiveSample : getObjectiveSamples()) {
            results.addAll(objectiveSample.getResultsById(resultClass, resultEntityId));
        }
        return results;
    }

    @JsonIgnore
	public List<Reference> getLsmReferences() {
		List<Reference> refs = new ArrayList<>();
        for(ObjectiveSample objectiveSample : getObjectiveSamples()) {
            refs.addAll(objectiveSample.getLsmReferences());
        }
		return Collections.unmodifiableList(refs);
	}

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getDataSet() {
        return dataSet;
    }

    public void setDataSet(String dataSet) {
        this.dataSet = dataSet;
    }

    public String getEffector() {
        return effector;
    }

    public void setEffector(String effector) {
        this.effector = effector;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getPublishingName() {
        return publishingName;
    }

    public void setPublishingName(String publishingNames) {
        this.publishingName = publishingNames;
    }

    public String getPublishedExternally() {
        return publishedExternally;
    }

    public void setPublishedExternally(String publishedExternally) {
        this.publishedExternally = publishedExternally;
    }
    
    public String getSlideCode() {
        return slideCode;
    }

    public void setSlideCode(String slideCode) {
        this.slideCode = slideCode;
    }

    public Integer getCrossBarcode() {
        return crossBarcode;
    }

    public void setCrossBarcode(Integer crossBarcode) {
        this.crossBarcode = crossBarcode;
    }

    public String getMountingProtocol() {
        return mountingProtocol;
    }

    public void setMountingProtocol(String mountingProtocol) {
        this.mountingProtocol = mountingProtocol;
    }

    public String getOrganism() {
        return organism;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }

    public String getGenotype() {
        return genotype;
    }

    public void setGenotype(String genotype) {
        this.genotype = genotype;
    }

    public Integer getFlycoreId() {
        return flycoreId;
    }

    public void setFlycoreId(Integer flycoreId) {
        this.flycoreId = flycoreId;
    }

    public String getFlycoreAlias() {
        return flycoreAlias;
    }

    public void setFlycoreAlias(String flycoreAlias) {
        this.flycoreAlias = flycoreAlias;
    }

    public String getFlycoreProject() {
        return flycoreProject;
    }

    public void setFlycoreProject(String flycoreProject) {
        this.flycoreProject = flycoreProject;
    }

    public String getFlycoreLabId() {
        return flycoreLabId;
    }

    public void setFlycoreLabId(String flycoreLabId) {
        this.flycoreLabId = flycoreLabId;
    }

    public String getFlycoreLandingSite() {
        return flycoreLandingSite;
    }

    public void setFlycoreLandingSite(String flycoreLandingSite) {
        this.flycoreLandingSite = flycoreLandingSite;
    }

    public String getFlycorePermission() {
        return flycorePermission;
    }

    public void setFlycorePermission(String flycorePermission) {
        this.flycorePermission = flycorePermission;
    }

    public String getFlycorePSubcategory() {
        return flycorePSubcategory;
    }

    public void setFlycorePSubcategory(String flycorePSubcategory) {
        this.flycorePSubcategory = flycorePSubcategory;
    }

    public String getImagingProject() {
        return imagingProject;
    }

    public void setImagingProject(String imagingProject) {
        this.imagingProject = imagingProject;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getImageFamily() {
        return imageFamily;
    }

    public void setImageFamily(String imageFamily) {
        this.imageFamily = imageFamily;
    }

    public String getVtLine() {
        return vtLine;
    }

    public void setVtLine(String vtLine) {
        this.vtLine = vtLine;
    }

    public Boolean getVisited() {
        return visited;
    }

    public void setVisited(Boolean visited) {
        this.visited = visited;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorOperation() {
        return errorOperation;
    }

    public void setErrorOperation(String errorOperation) {
        this.errorOperation = errorOperation;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public Boolean getSageSynced() {
		return sageSynced;
	}

	public void setSageSynced(Boolean sageSynced) {
		this.sageSynced = sageSynced;
	}

    @JsonIgnore
    public boolean isSampleSageSynced() {
        return getSageSynced()!=null && getSageSynced();
    }

    public CompressionStrategy getCompressionStrategy() {
        return compressionStrategy;
    }

    public void setCompressionStrategy(CompressionStrategy compressionStrategy) {
        this.compressionStrategy = compressionStrategy;
    }

    @JsonIgnore
    public String getUnalignedCompressionType() {
        if (compressionStrategy==null) return null;
        return compressionStrategy.getUnaligned();
    }

    @JsonIgnore
    public String getAlignedCompressionType() {
        if (compressionStrategy==null) return null;
        return compressionStrategy.getAligned();
    }

    @JsonIgnore
    public String getSeparationCompressionType() {
        if (compressionStrategy==null) return null;
        return compressionStrategy.getSeparation();
    }

    @JsonIgnore
    @SearchAttribute(key="comp_unaligned_txt",label="Compression for Unaligned Stacks")
    public String getUnalignedCompressionLabel() {
        String type = getUnalignedCompressionType();
        return type==null?null:SampleUtils.getCompressionLabel(type);
    }

    @JsonIgnore
    @SearchAttribute(key="comp_aligned_txt",label="Compression for Aligned Stacks")
    public String getAlignedCompressionLabel() {
        String type = getAlignedCompressionType();
        return type==null?null:SampleUtils.getCompressionLabel(type);
    }

    @JsonIgnore
    @SearchAttribute(key="comp_sep_txt",label="Compression for Separations")
    public String getSeparationCompressionLabel() {
        String type = getSeparationCompressionType();
        return type==null?null:SampleUtils.getCompressionLabel(type);
    }

    @JsonIgnore
    public void setUnalignedCompressionType(String compressionType) {
        if (compressionStrategy==null) {
            this.compressionStrategy = new CompressionStrategy();
        }
        compressionStrategy.setUnaligned(compressionType);
    }

    @JsonIgnore
    public void setAlignedCompressionType(String compressionType) {
        if (compressionStrategy==null) {
            this.compressionStrategy = new CompressionStrategy();
        }
        compressionStrategy.setAligned(compressionType);
    }

    @JsonIgnore
    public void setSeparationCompressionType(String compressionType) {
        if (compressionStrategy==null) {
            this.compressionStrategy = new CompressionStrategy();
        }
        compressionStrategy.setSeparation(compressionType);
    }

    public Date getTmogDate() {
        return tmogDate;
    }

    public void setTmogDate(Date tmogDate) {
        this.tmogDate = tmogDate;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public void setBasicPostProcessingSupported(boolean flag) {
        this.basicPostProcessingSupported = flag;
    }

    public boolean isBasicPostProcessingSupported() {
        return basicPostProcessingSupported;
    }

    @JsonIgnore
    public boolean isSampleBlocked() {
        return getBlocked()!=null && getBlocked();
    }
    
    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    @JsonIgnore
    public boolean isSamplePurged() {
        return purged!=null && purged;
    }
    
    public Boolean getPurged() {
        return purged;
    }
    
    public void setPurged(Boolean purged) {
        this.purged = purged;
    }

    @JsonIgnore
    public boolean isSamplePublishedToStaging() {
        return publishedToStaging!=null && publishedToStaging;
    }

    public Boolean getPublishedToStaging() {
        return publishedToStaging;
    }

    public void setPublishedToStaging(Boolean publishedToStaging) {
        this.publishedToStaging = publishedToStaging;
    }

    public Long getDiskSpaceUsage() {
        return diskSpaceUsage;
    }

    public void setDiskSpaceUsage(Long diskSpaceUsage) {
        this.diskSpaceUsage = diskSpaceUsage;
    }

    @JsonIgnore
    @SearchAttribute(key="usage_humans_t",label="Disk Space Usage")
    public String getDiskSpaceUsageForHumans() {
        return diskSpaceUsage==null ? null : DomainUtils.formatBytesForHumans(diskSpaceUsage);
    }

    public String getProbeSetIdentifier() {
        return probeSetIdentifier;
    }

    public void setProbeSetIdentifier(String probeSetIdentifier) {
        this.probeSetIdentifier = probeSetIdentifier;
    }

    public String getProbeSet() {
        return probeSet;
    }

    public void setProbeSet(String probeSet) {
        this.probeSet = probeSet;
    }

    public String getAnimalId() {
        return animalId;
    }

    public void setAnimalId(String animalId) {
        this.animalId = animalId;
    }

    public String getHeatShockMinutes() {
        return heatShockMinutes;
    }

    public void setHeatShockMinutes(String heatShockMinutes) {
        this.heatShockMinutes = heatShockMinutes;
    }
}
