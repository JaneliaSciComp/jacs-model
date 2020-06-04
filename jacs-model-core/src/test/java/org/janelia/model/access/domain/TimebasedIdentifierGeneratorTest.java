package org.janelia.model.access.domain;

import java.util.List;

import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

public class TimebasedIdentifierGeneratorTest {
    private TimebasedIdentifierGenerator idGenerator;

    @Before
    public void setUp() {
        idGenerator = new TimebasedIdentifierGenerator(0);
    }

    @Test
    public void generateLargeListOfIds() {
        List<Long> idList = idGenerator.generateIdList(16384);
        assertThat(ImmutableSet.copyOf(idList), hasSize(idList.size()));
    }
}