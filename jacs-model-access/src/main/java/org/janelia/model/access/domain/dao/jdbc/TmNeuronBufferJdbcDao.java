package org.janelia.model.access.domain.dao.jdbc;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import org.apache.commons.collections4.CollectionUtils;
import org.janelia.model.access.domain.dao.TmNeuronBufferDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TmNeuronBufferJdbcDao implements TmNeuronBufferDao {

    private static final Logger LOG = LoggerFactory.getLogger(TmNeuronBufferJdbcDao.class);

    private DataSource dataSource;

    @Inject
    public TmNeuronBufferJdbcDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createNeuronWorkspacePoints(Long neuronId, Long workspaceId, InputStream neuronPoints) {
        PreparedStatement pstmt = null;
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            String insertStmt = "insert into tmNeuron (id, tm_workspace_id, protobuf_value) values (?, ?, ?)";
            pstmt = conn.prepareStatement(insertStmt);

            pstmt.setLong(1, neuronId);
            pstmt.setLong(2, workspaceId);
            pstmt.setBinaryStream(3, neuronPoints);

            LOG.debug("Insert Neuron buffers: {}, {}, {}", insertStmt, neuronId, workspaceId);

            int rows = pstmt.executeUpdate();
            if (rows < 1) {
                throw new IllegalStateException("Could not insert points for neuron " + neuronId + " in workspace " + workspaceId);
            }
            conn.commit();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    @Override
    public void deleteNeuronPoints(Long neuronId) {
        PreparedStatement pstmt = null;
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            String deleteSql = "delete from tmNeuron where id=?";
            pstmt = conn.prepareStatement(deleteSql);

            pstmt.setLong(1, neuronId);

            LOG.debug("Delete Neuron buffers: {}, {}", deleteSql, neuronId);

            int rows = pstmt.executeUpdate();
            if (rows < 1) {
                throw new IllegalStateException("Could not delete points for neuron " + neuronId);
            }
            conn.commit();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    @Override
    public Map<Long, InputStream> streamNeuronPointsByWorkspaceId(Set<Long> neuronIds, Long workspaceId) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Map<Long, InputStream> neuronStreams = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            ImmutableList.Builder<String> whereBuilder = ImmutableList.builder();
            whereBuilder.add("tm_workspace_id = ?");
            if (CollectionUtils.isNotEmpty(neuronIds)) {
                whereBuilder.add("id in (" + Joiner.on(',').join(Collections.nCopies(neuronIds.size(), '?')) + ")");
            }
            String selectSql = "select id, protobuf_value from tmNeuron where " + String.join(" and " , whereBuilder.build());
            pstmt = conn.prepareStatement(selectSql);

            int fieldIndex = 1;
            pstmt.setLong(fieldIndex++, workspaceId);
            if (CollectionUtils.isNotEmpty(neuronIds)) {
                for (Long neuronId : neuronIds) pstmt.setLong(fieldIndex++, neuronId);
            }

            LOG.debug("Neuron buffers query: {}, {}, {}", selectSql, workspaceId, neuronIds);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                Long neuronId = rs.getLong(1);
                InputStream neuronPoints = rs.getBinaryStream(2);
                if (neuronPoints != null) neuronStreams.put(neuronId, neuronPoints);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception ignore) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception ignore) {
                }
            }
        }
        return neuronStreams;
    }

    @Override
    public void updateNeuronWorkspacePoints(Long neuronId, Long workspaceId, InputStream neuronPoints) {
        PreparedStatement pstmt = null;
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            String updateSql = "update tmNeuron set tm_workspace_id=?, protobuf_value=? where id=?";
            pstmt = conn.prepareStatement(updateSql);

            pstmt.setLong(1, workspaceId);
            pstmt.setBinaryStream(2, neuronPoints);
            pstmt.setLong(3, neuronId);

            LOG.debug("Insert Neuron buffers: {}, {}, {}", updateSql, neuronId, workspaceId);

            int rows = pstmt.executeUpdate();
            if (rows < 1) {
                throw new IllegalStateException("Could not update points for neuron " + neuronId + " in workspace " + workspaceId);
            }
            conn.commit();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception ignore) {
                }
            }
        }
    }
}
