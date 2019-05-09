package org.janelia.model.domain;

import org.janelia.model.domain.sample.LSMSummaryResult;
import org.janelia.model.domain.sample.NeuronSeparation;
import org.janelia.model.domain.sample.ObjectiveSample;
import org.janelia.model.domain.sample.PipelineError;
import org.janelia.model.domain.sample.Sample;
import org.janelia.model.domain.sample.SampleAlignmentResult;
import org.janelia.model.domain.sample.SamplePipelineRun;
import org.janelia.model.domain.sample.SampleProcessingResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Tests for the Sample domain model. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SampleTests {

    @Test
    public void testSampleModelMethods() throws Exception {
        
        SamplePipelineRun pipelineRun1 = new SamplePipelineRun();
        pipelineRun1.setId(1L);
        LSMSummaryResult lsr1 = new LSMSummaryResult();
        lsr1.setId(10L);
        SampleProcessingResult spr1 = new SampleProcessingResult();
        spr1.setId(11L);
        SampleAlignmentResult sar11 = new SampleAlignmentResult();
        sar11.setId(20L);
        SampleAlignmentResult sar12 = new SampleAlignmentResult();
        sar12.setId(21L);
        pipelineRun1.addResult(lsr1);
        pipelineRun1.addResult(spr1);
        pipelineRun1.addResult(sar11);
        pipelineRun1.addResult(sar12);

        NeuronSeparation ns1 = new NeuronSeparation();
        ns1.setId(100L);
        spr1.addResult(ns1);
        
        Assert.assertEquals(sar12, pipelineRun1.getLatestResult());
        Assert.assertEquals(spr1, pipelineRun1.getLatestProcessingResult());
        Assert.assertEquals(sar12, pipelineRun1.getLatestAlignmentResult());
        Assert.assertEquals(lsr1, pipelineRun1.getLatestResultOfType(LSMSummaryResult.class));
        Assert.assertEquals(Arrays.asList(spr1), pipelineRun1.getSampleProcessingResults());
        Assert.assertEquals(Arrays.asList(sar11, sar12), pipelineRun1.getAlignmentResults());
        Assert.assertEquals(Arrays.asList(spr1), pipelineRun1.getResultsById(SampleProcessingResult.class, spr1.getId()));
        Assert.assertEquals(Arrays.asList(sar11), pipelineRun1.getResultsById(SampleAlignmentResult.class, sar11.getId()));
        Assert.assertEquals(Arrays.asList(ns1), pipelineRun1.getResultsById(NeuronSeparation.class, ns1.getId()));
        Assert.assertEquals(Arrays.asList(lsr1), pipelineRun1.getResultsById(LSMSummaryResult.class, lsr1.getId()));
        
        SamplePipelineRun pipelineRun2 = new SamplePipelineRun();
        pipelineRun2.setId(2L);
        SampleProcessingResult spr2 = new SampleProcessingResult();
        spr2.setId(11L);
        SampleAlignmentResult sar21 = new SampleAlignmentResult();
        sar21.setName("Alignment 21");
        SampleAlignmentResult sar22 = new SampleAlignmentResult();
        sar22.setName("Alignment 22");
        pipelineRun1.addResult(lsr1);
        pipelineRun2.addResult(spr2);
        pipelineRun2.addResult(sar21);
        pipelineRun2.addResult(sar22);
        pipelineRun2.setError(new PipelineError());

        ObjectiveSample objective20xSample = new ObjectiveSample("20x");
        objective20xSample.addRun(pipelineRun1);
        objective20xSample.addRun(pipelineRun2);
        
        Assert.assertEquals(pipelineRun2, objective20xSample.getLatestRun());
        Assert.assertEquals(pipelineRun1, objective20xSample.getLatestSuccessfulRun());
        Assert.assertEquals(sar22, objective20xSample.getLatestResultOfType(SampleAlignmentResult.class));
        Assert.assertEquals(sar21, objective20xSample.getLatestResultOfType(SampleAlignmentResult.class, sar21.getName()));
        Assert.assertEquals(pipelineRun1, objective20xSample.getRunById(1L));
        Assert.assertEquals(pipelineRun2, objective20xSample.getRunById(2L));
        Assert.assertEquals(Arrays.asList(spr1, spr2), objective20xSample.getResultsById(SampleProcessingResult.class, spr1.getId()));
        Assert.assertEquals(Arrays.asList(sar11), objective20xSample.getResultsById(SampleAlignmentResult.class, sar11.getId()));
        Assert.assertEquals(Arrays.asList(ns1), objective20xSample.getResultsById(NeuronSeparation.class, ns1.getId()));
        Assert.assertEquals(Arrays.asList(lsr1, lsr1), objective20xSample.getResultsById(LSMSummaryResult.class, lsr1.getId()));
        
        Sample sample = new Sample();
        sample.setName("Test");
        sample.addObjectiveSample(objective20xSample);

        Assert.assertEquals(1, sample.getObjectiveSamples().size());
        Assert.assertEquals("20x", objective20xSample.getObjective());
        Assert.assertEquals(objective20xSample, sample.getObjectiveSample("20x"));
        Assert.assertEquals(Arrays.asList(spr1, spr2), sample.getResultsById(SampleProcessingResult.class, spr1.getId()));
        Assert.assertEquals(Arrays.asList(sar11), sample.getResultsById(SampleAlignmentResult.class, sar11.getId()));
        Assert.assertEquals(Arrays.asList(ns1), sample.getResultsById(NeuronSeparation.class, ns1.getId()));
        Assert.assertEquals(Arrays.asList(lsr1, lsr1), sample.getResultsById(LSMSummaryResult.class, lsr1.getId()));
    }
       
}
