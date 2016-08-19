/*
 * FastMatrixParameter.java
 *
 * Copyright (c) 2002-2015 Alexei Drummond, Andrew Rambaut and Marc Suchard
 *
 * This file is part of BEAST.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * BEAST is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 *  BEAST is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BEAST; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package dr.inference.model;

import dr.xml.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Marc A. Suchard
 */
public class FastMatrixParameter extends CompoundParameter implements MatrixParameterInterface {

    public static final String FAST_MATRIX_PARAMETER = "fastMatrixParameter";
    public static final String ROW_DIMENSION = MatrixParameter.ROW_DIMENSION;
    public static final String COLUMN_DIMENSION = MatrixParameter.COLUMN_DIMENSION;

    public FastMatrixParameter(String id, int rowDimension, int colDimension) {
        super(id);
        singleParameter = new Parameter.Default(rowDimension * colDimension);
        addParameter(singleParameter);

        this.rowDimension = rowDimension;
        this.colDimension = colDimension;
    }

    public Parameter getParameter(int index) {
        if (proxyList == null) {
            proxyList = new ArrayList<ParameterProxy>(colDimension);
            for (int i = 0; i < colDimension; ++i) {
                proxyList.add(new ParameterProxy(this, i));
            }
        }
        return proxyList.get(index);
    }

    class ParameterProxy extends Parameter.Abstract {

        private final int column;
        private final FastMatrixParameter matrix;

        ParameterProxy(FastMatrixParameter matrix, int column) {
            this.matrix = matrix;
//            this.addParameterListener(this.matrix);
            this.column = column;
        }

        @Override
        protected void adoptValues(Parameter source) {
            throw new RuntimeException("Do not call");
        }

        @Override
        public double getParameterValue(int dim) {
            return matrix.getParameterValue(dim, column);
        }

        @Override
        public void setParameterValue(int dim, double value) {
            matrix.setParameterValue(dim, column, value);
        }

        @Override
        public void setParameterValueQuietly(int dim, double value) {
            matrix.setParameterValueQuietly(dim, column, value);
        }

        @Override
        public void setParameterValueNotifyChangedAll(int dim, double value) {
            throw new RuntimeException("Do not call");
        }

        @Override
        public String getParameterName() {
            return getId();
        }

        @Override
        public void addBounds(Bounds<Double> bounds) {
            matrix.getUniqueParameter(0).addBounds(bounds);
        }

        @Override
        public Bounds<Double> getBounds() {
            return matrix.getUniqueParameter(0).getBounds();
        }

        @Override
        public void fireParameterChangedEvent(int index, ChangeType type){
            matrix.fireParameterChangedEvent(index, type);
        }


        @Override
        public void addDimension(int index, double value) {
            throw new RuntimeException("Do not call");
        }

        @Override
        public double removeDimension(int index) {
            throw new RuntimeException("Do not call");
        }

        @Override
        public int getDimension() {
            return rowDimension;
        }
    }

    private final int index(int row, int col) {
        // column-major
        return col * rowDimension + row;
    }

    @Override
    public double getParameterValue(int row, int col) {
        return singleParameter.getParameterValue(index(row, col));
    }

    @Override
    public double[] getParameterValues() {
        double[] destination = new double[getDimension()];
        copyParameterValues(destination, 0);
        return destination;
    }

    @Override
    public void copyParameterValues(double[] destination, int offset) {
        final double[] source = ((Parameter.Default) singleParameter).inspectParameterValues();
        System.arraycopy(source, 0, destination, offset, source.length);
    }

    @Override
    public void setAllParameterValuesQuietly(double[] values, int offset) {
        final double[] destination = ((Parameter.Default) singleParameter).inspectParameterValues();
        System.arraycopy(values, offset, destination, 0, destination.length);
    }

    @Override
    public void setParameterValue(int row, int col, double value) {
        singleParameter.setParameterValue(index(row, col), value);
    }

    @Override
    public void setParameterValueQuietly(int row, int col, double value) {
        singleParameter.setParameterValueQuietly(index(row, col), value);
    }

    @Override
    public void setParameterValueNotifyChangedAll(int row, int col, double value) {
        singleParameter.setParameterValueNotifyChangedAll(index(row, col), value);
    }

    @Override
    public double[] getColumnValues(int col) {
        double[] rtn = new double[rowDimension];
        for (int i = 0; i < rowDimension; ++i) {
            rtn[i] = getParameterValue(i, col);
        }
        return rtn;
    }

    @Override
    public double[][] getParameterAsMatrix() {
        double[][] rtn = new double[rowDimension][colDimension];
        for (int j = 0; j < colDimension; ++j) {
            for (int i = 0; i < rowDimension; ++i) {
                rtn[i][j] = getParameterValue(i, j);
            }
        }
        return rtn;
    }

    @Override
    public int getColumnDimension() {
        return colDimension;
    }

    @Override
    public int getRowDimension() {
        return rowDimension;
    }

    @Override
    public int getParameterCount() {
        return getColumnDimension();
    }

    @Override
    public int getUniqueParameterCount() {
        return 1;
    }

    @Override
    public Parameter getUniqueParameter(int index) {
        return super.getParameter(0);
    }

    private final int rowDimension;
    private final int colDimension;
    private final Parameter singleParameter;

    private List<ParameterProxy> proxyList = null;

    public static XMLObjectParser PARSER = new AbstractXMLObjectParser() {

        public String getParserName() {
            return FAST_MATRIX_PARAMETER;
        }

        public Object parseXMLObject(XMLObject xo) throws XMLParseException {

            final String name = xo.hasId() ? xo.getId() : null;
            final int rowDimension = xo.getIntegerAttribute(ROW_DIMENSION);
            final int colDimension = xo.getIntegerAttribute(COLUMN_DIMENSION);

            FastMatrixParameter matrixParameter = new FastMatrixParameter(name, rowDimension, colDimension);

            return matrixParameter;
        }

        //************************************************************************
        // AbstractXMLObjectParser implementation
        //************************************************************************

        public String getParserDescription() {
            return "A fast matrix parameter constructed from a single parameter.";
        }

        public XMLSyntaxRule[] getSyntaxRules() {
            return rules;
        }

        private final XMLSyntaxRule[] rules = {
                new ElementRule(Parameter.class, 0, Integer.MAX_VALUE),
                AttributeRule.newIntegerRule(ROW_DIMENSION, false),
                AttributeRule.newIntegerRule(COLUMN_DIMENSION, false),
        };

        public Class getReturnType() {
            return FastMatrixParameter.class;
        }
    };
}
