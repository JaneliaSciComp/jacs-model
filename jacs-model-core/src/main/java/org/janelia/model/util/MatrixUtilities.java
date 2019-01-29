package org.janelia.model.util;

import Jama.Matrix;

/**
 * Convenience class for matrix operations.
 *
 * @author fosterl
 */
public class MatrixUtilities {
    public static final int EXPECTED_MATRIX_SIZE = 4;
    public static final String ROW_SEP = ";";
    public static final String COL_SEP = ",";
    public static final int X_OFFS = 0;
    public static final int Y_OFFS = 1;
    public static final int Z_OFFS = 2;

    /**
     * Turns some Jama matrix into a flat string, suitable for plugging into
     * our database model.
     * 
     * @see #deserializeMatrix(java.lang.String, java.lang.String) 
     * @param matrix what to flatten
     * @param matrixName for reporting purposes.
     * @return 
     */
    public static String serializeMatrix( Matrix matrix, String matrixName) {
        StringBuilder rtnVal = new StringBuilder();
        if (matrix == null
                || matrix.getRowDimension() != EXPECTED_MATRIX_SIZE
                || matrix.getColumnDimension() != EXPECTED_MATRIX_SIZE) {
            System.err.println("Serialization of " + matrixName + " failed.  Unexpected dimensions.");
            return null;
        }

        double[][] matrixArr = matrix.getArray();
        for (double[] row : matrixArr) {
            if (rtnVal.length() > 0) {
                rtnVal.append(ROW_SEP);
            }
            int colCount = 0;
            for (double col : row) {
                if (colCount > 0) {
                    rtnVal.append(COL_SEP);
                }
                rtnVal.append(col);
                colCount++;
            }
        }

        return rtnVal.toString();
    }
    
    /**
     * Takes some serialized matrix (flat string), and turns it back into a
     * Jama matrix.
     * 
     * @see #serializeMatrix(Jama.Matrix, java.lang.String) 
     * @param matrixString
     * @param matrixName
     * @return 
     */
    public static Matrix deserializeMatrix( String matrixString, String matrixName ) {
        Matrix rtnVal = null;
        String[] rowMatrixStr = matrixString.split(ROW_SEP);
        int x = 0;
        int y = 0;
        double[][] accumulator = new double[EXPECTED_MATRIX_SIZE][EXPECTED_MATRIX_SIZE];
        if (rowMatrixStr.length == 4) {
            for (String row : rowMatrixStr) {
                String[] columnStr = row.split(COL_SEP);
                x = 0;
                try {
                    for (String column : columnStr) {
                        double colDouble = Double.parseDouble(column);
                        accumulator[y][x] = colDouble;
                        x++;
                    }
                } catch (NumberFormatException nfe) {
                    // NOTE: this class is serialized.  Therefore, it cannot carry logger.
                    System.err.println("Serialized value " + columnStr[x] + " at position " + x + "," + y + " of matrix " + matrixName + "value {" + matrixString + "}, could not be deserialized.");
                }
                y++;
            }
            rtnVal = new Matrix(accumulator);
        } else {
            // NOTE: this class is serialized.  Therefore, it cannot carry logger.
            System.err.println("Serialized matrix: " + matrixName + "value {" + matrixString + "}, could not be deserialized.");
        }
        return rtnVal;

    }
    
    public static String createSerializableMicronToVox(double[] voxelMicrometers, int[] origin) {
        Matrix micronToVoxMatrix = buildMicronToVox(voxelMicrometers, origin);
        String micronToVoxString = MatrixUtilities.serializeMatrix(micronToVoxMatrix, "Micron to Voxel Matrix");
        return micronToVoxString;
    }

    public static Matrix buildMicronToVox(double[] voxelMicrometers, int[] origin) {
        double[][] micronToVoxArr = new double[][]{
            {1.0 / voxelMicrometers[X_OFFS], 0.0, 0.0, -origin[X_OFFS]},
            {0.0, 1.0 / voxelMicrometers[Y_OFFS], 0.0, -origin[Y_OFFS]},
            {0.0, 0.0, 1.0 / voxelMicrometers[Z_OFFS], -origin[Z_OFFS]},
            {0.0, 0.0, 0.0, 1.0}
        };
        Matrix micronToVoxMatrix = new Matrix(micronToVoxArr);
        return micronToVoxMatrix;
    }

    public static String createSerializableVoxToMicron(double[] voxelMicrometers, int[] origin) {
        Matrix voxToMicronMatrix = buildVoxToMicron(voxelMicrometers, origin);
        String voxToMicronString = MatrixUtilities.serializeMatrix(voxToMicronMatrix, "Voxel to Micron Matrix");
        return voxToMicronString;
    }

    public static Matrix buildVoxToMicron(double[] voxelMicrometers, int[] origin) {
        double[][] voxToMicronArr = new double[][]{
            {voxelMicrometers[X_OFFS], 0.0, 0.0, origin[X_OFFS] * voxelMicrometers[X_OFFS]},
            {0.0, voxelMicrometers[Y_OFFS], 0.0, origin[Y_OFFS] * voxelMicrometers[Y_OFFS]},
            {0.0, 0.0, voxelMicrometers[Z_OFFS], origin[Z_OFFS] * voxelMicrometers[Z_OFFS]},
            {0.0, 0.0, 0.0, 1.0}
        };
        Matrix voxToMicronMatrix = new Matrix(voxToMicronArr);
        return voxToMicronMatrix;
    }

    
}
