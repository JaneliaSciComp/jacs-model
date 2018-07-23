package org.janelia.model.access.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.janelia.model.domain.DomainConstants;
import org.janelia.model.domain.enums.AlignmentScoreType;
import org.janelia.model.domain.interfaces.HasAnatomicalArea;
import org.janelia.model.domain.interfaces.HasFileGroups;
import org.janelia.model.domain.interfaces.HasFiles;
import org.janelia.model.domain.sample.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for dealing with Samples, Neuron Fragments, and other related objects.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SampleUtils {

    private static final Logger log = LoggerFactory.getLogger(SampleUtils.class);

    public static String getLabel(PipelineResult result) {
        return result.getParentRun().getParent().getObjective() + " " + result.getName();
    }

    public static boolean equals(PipelineResult o1, PipelineResult o2) {
        if (o1==null || o2==null) return false;
        if (o1.getId()==null || o2.getId()==null) return false;
        return o1.getId().equals(o2.getId());
    }

    /**
     * Given a single Sample and a set of criteria, find results in the Sample matching the criteria. Any of the criteria may be null. Absent criteria, 
     * this method results the latest results that are found, in reverse chronological order.
     * @param sample
     * @param objective
     * @param area
     * @param aligned
     * @param resultClass
     * @param resultName
     * @param groupName
     * @return
     */
    public static List<HasFiles> getMatchingResults(Sample sample, String objective, String area, Boolean aligned, String resultClass, String resultName, String groupName) {

        log.debug("Getting result '{}' from {}",resultName,sample.getName());
        log.trace("  Objective: {}",objective);
        log.trace("  Area: {}",area);
        log.trace("  Aligned: {}",aligned);
        log.trace("  Result class: {}",resultClass);
        log.trace("  Result name: {}",resultName);
        log.trace("  Group name: {}",groupName);

        List<HasFiles> chosenResults = new ArrayList<>();
        if (objective==null) {
            List<ObjectiveSample> objectiveSamples = sample.getObjectiveSamples();
            for(int i=objectiveSamples.size()-1; i>=0; i--) {
                ObjectiveSample objectiveSample = objectiveSamples.get(i);
                log.debug("Testing objective: "+objectiveSample.getObjective());
                chosenResults.addAll(getMatchingResults(objectiveSample, area, aligned, resultClass, resultName, groupName));
            }
        }
        else {
            ObjectiveSample objectiveSample = sample.getObjectiveSample(objective);
            if (objectiveSample!=null) {
                log.debug("Testing objective: "+objectiveSample.getObjective());
                chosenResults.addAll(getMatchingResults(objectiveSample, area, aligned, resultClass, resultName, groupName));
            }
        }
        
        List<HasFiles> finalResults = new ArrayList<>();
        for (HasFiles hasFiles : chosenResults) {
            if (hasFiles.getFiles().isEmpty()) {
                if (hasFiles instanceof HasFileGroups) {
                    // The chosen result doesn't have files itself, but it does have file groups
                    HasFileGroups hasGroups = (HasFileGroups)hasFiles;
                    for(String groupKey : new TreeSet<>(hasGroups.getGroupKeys())) {
                        if (groupName==null || StringUtils.equals(groupName, groupKey)) {
                            finalResults.add(hasGroups.getGroup(groupKey));
                        }
                    }
                }
            }
            else {
                finalResults.add(hasFiles);
            }
        }

        log.debug("Got results: "+finalResults);
        return finalResults;
    }

    public static Collection<HasFiles> getMatchingResults(ObjectiveSample objectiveSample, String area, Boolean aligned, String resultClass, String resultName, String groupName) {

        // Use hash map to de-duplicate denormalized results
        Map<String,HasFiles> chosenResults = new LinkedHashMap<>();
        
        Set<String> validAreas = new HashSet<>();
        Set<String> validTiles = new HashSet<>();
        for (SampleTile tile : objectiveSample.getTiles()) {
            String tileArea = tile.getAnatomicalArea();
            if (area==null || StringUtils.equals(area, tileArea)) {
                validAreas.add(tileArea);
                validTiles.add(tile.getName());
            }
        }

        // Walk pipeline runs backwards to get latest first
        List<SamplePipelineRun> runs = objectiveSample.getPipelineRuns();
        for(int i=runs.size()-1; i>=0; i--) {
            SamplePipelineRun run = runs.get(i);
            log.debug("  Testing run: " + run.getId());

            // Walk results backwards to get latest first
            List<PipelineResult> results = run.getResults();
            for (int j = results.size()-1; j>=0; j--) {
                PipelineResult pipelineResult = results.get(j);
                log.debug("  Testing result: " + pipelineResult.getId());

                boolean isAligned = pipelineResult instanceof SampleAlignmentResult;
                if (aligned==null || aligned==isAligned) {
                    log.debug("    Found result matching align="+aligned);

                    if (resultClass==null || StringUtils.equals(pipelineResult.getClass().getName(), resultClass)) {
                        log.debug("    Found result matching resultClass=" + resultClass);

                        String pipelineResultName = null;
                        if (pipelineResult instanceof SampleAlignmentResult) {
                            SampleAlignmentResult alignmentResult = (SampleAlignmentResult) pipelineResult;
                            if (StringUtils.isBlank(alignmentResult.getAlignmentSpace())) {
                                // If the alignment space is empty, fallback on the result name.
                                // This shouldn't happen, but it does for legacy or broken data.
                                pipelineResultName = pipelineResult.getName();
                            }
                            else {
                                pipelineResultName = alignmentResult.getAlignmentSpace();
                            }
                        }
                        else {
                            pipelineResultName = pipelineResult.getName();
                        }

                        if (resultName == null || StringUtils.equals(pipelineResultName, resultName)) {
                            log.debug("    Found result matching resultName=" + resultName);

                            if (groupName == null) {
                                if (area == null) {
                                    chosenResults.put(pipelineResult.getId().toString(), pipelineResult);
                                }
                                else if (pipelineResult instanceof HasAnatomicalArea) {
                                    HasAnatomicalArea aaResult = (HasAnatomicalArea) pipelineResult;
                                    if (StringUtils.equals(area, aaResult.getAnatomicalArea())) {
                                        chosenResults.put(pipelineResult.getId().toString(), pipelineResult);
                                    }
                                }
                                else if (pipelineResult instanceof SamplePostProcessingResult) {
                                    HasFileGroups hasGroups = (HasFileGroups) pipelineResult;
                                    for (FileGroup fileGroup : hasGroups.getGroups()) {
                                        String key = fileGroup.getKey(); // Could be an area or a tile name
                                        if (validAreas.contains(key) || validTiles.contains(key)) {
                                            chosenResults.put(pipelineResult.getId() + "~" + key, fileGroup);
                                        }
                                    }
                                }
                            }
                            else if (pipelineResult instanceof HasFileGroups) {
                                HasFileGroups hasGroups = (HasFileGroups) pipelineResult;
                                HasFiles hasFiles = hasGroups.getGroup(groupName);
                                if (hasFiles != null) {
                                    log.debug("    Found group: " + groupName);
                                    chosenResults.put(pipelineResult.getId() + "~" + groupName, hasFiles);
                                }
                            }
                        }
                    }
                }
            }
            
            if (!chosenResults.isEmpty()) {
                // We only add the matching results from a single run
                break;
            }
        }
        return chosenResults.values();
    }
    
    public static PipelineResult getResultContainingNeuronSeparation(Sample sample, NeuronFragment neuronFragment) {
        return getNeuronSeparation(sample, neuronFragment, PipelineResult.class);
    }

    public static NeuronSeparation getNeuronSeparation(Sample sample, NeuronFragment neuronFragment) {
        return getNeuronSeparation(sample, neuronFragment, NeuronSeparation.class);
    }

    public static <T extends PipelineResult> T getNeuronSeparation(Sample sample, NeuronFragment neuronFragment, Class<T> returnClazz) {

        if (neuronFragment==null) return null;

        for(ObjectiveSample objectiveSample : sample.getObjectiveSamples()) {

            List<SamplePipelineRun> runs = objectiveSample.getPipelineRuns();
            for(int i=runs.size()-1; i>=0; i--) {
                SamplePipelineRun run = runs.get(i);

                if (run!=null && run.getResults()!=null) {
                    for(PipelineResult result : run.getResults()) {
                        if (result!=null && result.getResults()!=null) {
                            for(PipelineResult secondaryResult : result.getResults()) {
                                if (secondaryResult!=null && secondaryResult instanceof NeuronSeparation) {
                                    NeuronSeparation separation = (NeuronSeparation)secondaryResult;
                                    if (separation.getFragmentsReference().getReferenceId().equals(neuronFragment.getSeparationId())) {
                                        return returnClazz.equals(NeuronSeparation.class) ? (T)separation : (T)result;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    public static Map<AlignmentScoreType, String> getLatestAlignmentScores(Sample sample) {

        Map<AlignmentScoreType, String> scores = new HashMap<>();

        for(ObjectiveSample objectiveSample : sample.getObjectiveSamples()) {
            for (SamplePipelineRun run : objectiveSample.getPipelineRuns()) {
                if (run != null && run.getResults() != null) {
                    for (SampleAlignmentResult alignment : run.getResultsOfType(SampleAlignmentResult.class)) {
                        scores.putAll(alignment.getScores());
                    }
                }
            }
        }

        return scores;
    }

    public static Collection<String> getAlignmentSpaces(Collection<Sample> samples) {

        Set<String> alignmentSpaces = new TreeSet<>();

        for (Sample sample : samples) {
            for (ObjectiveSample objectiveSample : sample.getObjectiveSamples()) {
                for (SamplePipelineRun samplePipelineRun : objectiveSample.getPipelineRuns()) {
                    for (SampleAlignmentResult sampleAlignmentResult : samplePipelineRun.getAlignmentResults()) {
                        alignmentSpaces.add(sampleAlignmentResult.getAlignmentSpace());
                    }
                }
            }
        }

        return alignmentSpaces;
    }

    public static String getUnalignedCompression(DataSet dataSet, Sample sample) {

        String compression = null;
        if (sample!=null) {
            compression = sample.getUnalignedCompressionType();
        }

        if (compression==null && dataSet!=null) {
            compression = dataSet.getUnalignedCompressionType();
        }

        if (compression==null) {
            compression = DomainConstants.VALUE_COMPRESSION_VISUALLY_LOSSLESS;
        }

        return compression;
    }

    public static String getAlignedCompression(DataSet dataSet, Sample sample) {

        String compression = null;
        if (sample!=null) {
            compression = sample.getAlignedCompressionType();
        }

        if (compression==null && dataSet!=null) {
            compression = dataSet.getAlignedCompressionType();
        }

        if (compression==null) {
            compression = DomainConstants.VALUE_COMPRESSION_VISUALLY_LOSSLESS;
        }

        return compression;
    }

    public static String getSeparationCompression(DataSet dataSet, Sample sample) {

        String compression = null;
        if (sample!=null) {
            compression = sample.getSeparationCompressionType();
        }

        if (compression==null && dataSet!=null) {
            compression = dataSet.getSeparationCompressionType();
        }

        if (compression==null) {
            compression = DomainConstants.VALUE_COMPRESSION_LOSSLESS;
        }

        return compression;
    }

    public static boolean isLossless(String compression) {
        return (compression.equals(DomainConstants.VALUE_COMPRESSION_LOSSLESS_AND_H5J) || compression.equals(DomainConstants.VALUE_COMPRESSION_LOSSLESS));
    }

    public static boolean isVisuallyLossless(String compression) {
        return (compression.equals(DomainConstants.VALUE_COMPRESSION_VISUALLY_LOSSLESS) || compression.equals(DomainConstants.VALUE_COMPRESSION_VISUALLY_LOSSLESS_AND_PBD));
    }
}
