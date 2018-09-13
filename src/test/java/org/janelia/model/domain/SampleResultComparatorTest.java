package org.janelia.model.domain;

import org.janelia.model.domain.sample.LSMSummaryResult;
import org.janelia.model.domain.sample.SampleAlignmentResult;
import org.janelia.model.domain.sample.SamplePostProcessingResult;
import org.janelia.model.domain.sample.SampleProcessingResult;
import org.janelia.model.util.SampleResultComparator;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SampleResultComparatorTest {

    @Test
    public void testOrdering() {

        List results = new ArrayList();

        LSMSummaryResult result1 = new LSMSummaryResult();
        results.add(result1);

        SampleProcessingResult result2 = new SampleProcessingResult();
        result2.setAnatomicalArea("Brain");
        results.add(result2);

        SampleProcessingResult result3 = new SampleProcessingResult();
        result3.setAnatomicalArea("VNC");
        results.add(result3);

        SamplePostProcessingResult result4 = new SamplePostProcessingResult();
        results.add(result4);

        SampleAlignmentResult result5 = new SampleAlignmentResult();
        result5.setRelativeOrder(1);
        results.add(result5);

        SampleAlignmentResult result6 = new SampleAlignmentResult();
        result6.setRelativeOrder(2);
        results.add(result6);

        SampleAlignmentResult result7 = new SampleAlignmentResult();
        result7.setRelativeOrder(3);
        results.add(result7);

        List disordered = new ArrayList();
        disordered.add(result2);
        disordered.add(result1);
        disordered.add(result6);
        disordered.add(result7);
        disordered.add(result5);
        disordered.add(result4);
        disordered.add(result3);
        Collections.sort(disordered, new SampleResultComparator());
        Assert.assertEquals(results, disordered);
    }

    @Test
    public void testOrdering2() {

        List results = new ArrayList();

        SampleProcessingResult result1 = new SampleProcessingResult();
        result1.setAnatomicalArea("Brain");
        results.add(result1);

        SampleProcessingResult result2 = new SampleProcessingResult();
        result2.setAnatomicalArea("Neck");
        results.add(result2);

        SampleProcessingResult result3 = new SampleProcessingResult();
        result3.setAnatomicalArea("VNC");
        results.add(result3);

        SamplePostProcessingResult result4 = new SamplePostProcessingResult();
        results.add(result4);

        SampleAlignmentResult result5 = new SampleAlignmentResult();
        result5.setRelativeOrder(1);
        results.add(result5);

        SampleAlignmentResult result6 = new SampleAlignmentResult();
        result6.setRelativeOrder(2);
        results.add(result6);

        List disordered = new ArrayList();
        disordered.add(result5);
        disordered.add(result3);
        disordered.add(result2);
        disordered.add(result6);
        disordered.add(result1);
        disordered.add(result4);
        Collections.sort(disordered, new SampleResultComparator());
        Assert.assertEquals(results, disordered);
    }

}
