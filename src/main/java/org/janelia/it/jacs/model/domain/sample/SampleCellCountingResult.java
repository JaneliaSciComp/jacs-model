package org.janelia.it.jacs.model.domain.sample;

/**
 * The result of running the cell counting algorithm on a sample.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SampleCellCountingResult extends PipelineResult {

    private Integer cellCount;

    public Integer getCellCount() {
        return cellCount;
    }

    public void setCellCount(Integer cellCount) {
        this.cellCount = cellCount;
    }
}
