package org.janelia.model.access.domain.dao.mongo;

import com.mongodb.MongoGridFSException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.model.Filters;
import com.mongodb.client.gridfs.model.GridFSFile;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.function.Consumer;

import javax.inject.Inject;
/**
 * dao for accessing mongo gridfs buckets; good for storing files
 * larger than 16MB.
 */

public class GridFSMongoDao {
    GridFSBucket gridFS;
    final MongoDatabase mongoDatabase;
    final String defaultBucket = "tiledMicroscope";

    @Inject
    GridFSMongoDao(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
        gridFS = GridFSBuckets.create(mongoDatabase, defaultBucket);
    }

    public void downloadDataBlock(OutputStream output, String id) throws FileNotFoundException{
        try {
            gridFS.downloadToStream(id, output);
        } catch (MongoGridFSException e) {
            throw new FileNotFoundException("Can't find data block " + id + " in GridFS bucket " + defaultBucket);
        }
    }

    public String createDataBlock(InputStream data, String id) {
        return gridFS.uploadFromStream(id, data).toString();
    }

    /**
     * gridFS doesn't overwrite existing data.  It's best to just delete the existing file
     * and upload a new file for an update, to prevent data size from creeping up.
     */
    public void updateDataBlock(InputStream data, String id) {
        deleteDataBlock(id);
        createDataBlock(data, id);
    }

   public void deleteDataBlock(String id) {
       gridFS.find(Filters.eq("filename", id)).forEach(new Consumer<GridFSFile>() {
           @Override
           public void accept(GridFSFile dataBlock) {
               try {
                   gridFS.delete(dataBlock.getObjectId());
               } catch (MongoGridFSException e) {
                   throw new RuntimeException("Problem deleting existing file " + id + " in GridFS bucket " + defaultBucket);
               }

           }
       });
    }
}
