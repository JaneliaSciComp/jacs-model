package org.janelia.model.util;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import org.janelia.model.domain.interfaces.HasAnatomicalArea;
import org.janelia.model.domain.sample.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Sorts the results within a sample pipeline run according to a consistent ordering:
 * 1) pipeline steps are sorted according to stage and complexity
 * 2) results within a single step are sorted by anatomical area
 * 3) results within a single step and area are sorted by their relative ordering (e.g. for multiple alignments)
 * 4) if there is any ambiguity left, the creation dates are used (this is just future proofing)
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SampleResultComparator implements Comparator<PipelineResult> {

    private static final List<Class<?>> classOrdering = Arrays.asList(
            LSMSummaryResult.class,
            SampleProcessingResult.class,
            SamplePostProcessingResult.class,
            SampleAlignmentResult.class
    );

    @Override
    public int compare(PipelineResult o1, PipelineResult o2) {

        int c1 = classOrdering.indexOf(o1.getClass());
        int c2 = classOrdering.indexOf(o2.getClass());

        String area1 = null;
        if (o1 instanceof HasAnatomicalArea) {
            area1 = ((HasAnatomicalArea) o1).getAnatomicalArea();
        }

        String area2 = null;
        if (o2 instanceof HasAnatomicalArea) {
            area2 = ((HasAnatomicalArea) o2).getAnatomicalArea();
        }

        return ComparisonChain.start()
                .compare(c1, c2, Ordering.natural())
                .compare(area1, area2, Ordering.natural().nullsLast())
                .compare(o1.getRelativeOrder(), o2.getRelativeOrder(), Ordering.natural().nullsFirst())
                .compare(o1.getCreationDate(), o2.getCreationDate(), Ordering.natural().nullsFirst())
                .result();
    }
}
